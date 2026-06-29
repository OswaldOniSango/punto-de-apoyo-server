package com.puntodeapoyo.inspectioncases.service.impl;

import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.puntodeapoyo.common.PhoneNormalizer;
import com.puntodeapoyo.inspectioncases.dto.CaseAssignmentResponse;
import com.puntodeapoyo.inspectioncases.dto.CreateInspectionCaseRequest;
import com.puntodeapoyo.inspectioncases.dto.InspectionCaseResponse;
import com.puntodeapoyo.inspectioncases.dto.InspectionCaseSearchCriteria;
import com.puntodeapoyo.inspectioncases.dto.PhotoEvidenceResponse;
import com.puntodeapoyo.inspectioncases.model.CaseAssignment;
import com.puntodeapoyo.inspectioncases.model.InspectionCase;
import com.puntodeapoyo.inspectioncases.model.PhotoEvidence;
import com.puntodeapoyo.inspectioncases.model.InspectionCaseStatus;
import com.puntodeapoyo.inspectioncases.repository.CaseAssignmentRepository;
import com.puntodeapoyo.inspectioncases.repository.InspectionCaseRepository;
import com.puntodeapoyo.inspectioncases.repository.InspectionCaseRepository.CreateInspectionCaseCommand;
import com.puntodeapoyo.inspectioncases.repository.PhotoEvidenceRepository;
import com.puntodeapoyo.inspectioncases.repository.PhotoEvidenceRepository.CreatePhotoEvidenceCommand;
import com.puntodeapoyo.inspectioncases.service.InspectionCaseService;
import com.puntodeapoyo.inspectioncases.storage.PhotoStorageService;
import com.puntodeapoyo.inspectioncases.storage.StoredPhoto;
import com.puntodeapoyo.users.model.InternalUser;
import com.puntodeapoyo.users.model.UserRole;
import com.puntodeapoyo.users.model.UserStatus;
import com.puntodeapoyo.users.repository.InternalUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InspectionCaseServiceImpl implements InspectionCaseService {

    private static final String TRACKING_PREFIX = "VZ";
    private static final int TRACKING_NUMBER_WIDTH = 8;
    private static final int MAX_PHOTOS = 10;
    private static final long MAX_PHOTO_SIZE_BYTES = 10 * 1024 * 1024;

    private final InspectionCaseRepository inspectionCaseRepository;
    private final PhotoEvidenceRepository photoEvidenceRepository;
    private final CaseAssignmentRepository caseAssignmentRepository;
    private final InternalUserRepository internalUserRepository;
    private final PhotoStorageService photoStorageService;

    public InspectionCaseServiceImpl(
            InspectionCaseRepository inspectionCaseRepository,
            PhotoEvidenceRepository photoEvidenceRepository,
            CaseAssignmentRepository caseAssignmentRepository,
            InternalUserRepository internalUserRepository,
            PhotoStorageService photoStorageService
    ) {
        this.inspectionCaseRepository = inspectionCaseRepository;
        this.photoEvidenceRepository = photoEvidenceRepository;
        this.caseAssignmentRepository = caseAssignmentRepository;
        this.internalUserRepository = internalUserRepository;
        this.photoStorageService = photoStorageService;
    }

    @Override
    @Transactional
    public InspectionCaseResponse register(CreateInspectionCaseRequest request) {
        return register(request, List.of());
    }

    @Override
    @Transactional
    public InspectionCaseResponse register(CreateInspectionCaseRequest request, List<MultipartFile> photos) {
        List<MultipartFile> normalizedPhotos = normalizePhotos(photos);
        validatePhotos(normalizedPhotos);

        int year = Year.now().getValue();
        long trackingNumber = inspectionCaseRepository.nextTrackingNumber(year);
        String trackingCode = formatTrackingCode(year, trackingNumber);

        InspectionCase inspectionCase = inspectionCaseRepository.create(new CreateInspectionCaseCommand(
                trackingCode,
                normalizeRequired(request.applicantName()),
                PhoneNormalizer.normalize(request.applicantPhone()),
                normalizeOptional(request.applicantEmail()),
                normalizeRequired(request.address()),
                normalizeOptional(request.city()),
                normalizeOptional(request.stateRegion()),
                normalizeRequired(request.description()),
                request.latitude(),
                request.longitude(),
                request.priority(),
                InspectionCaseStatus.PENDIENTE
        ));

        List<PhotoEvidenceResponse> photoResponses = storePhotos(
                inspectionCase.id(),
                inspectionCase.trackingCode(),
                normalizedPhotos,
                null,
                true
        );

        return InspectionCaseResponse.from(inspectionCase, photoResponses);
    }

    @Override
    public List<InspectionCaseResponse> search(InspectionCaseSearchCriteria criteria) {
        List<InspectionCase> cases = inspectionCaseRepository.search(criteria);
        Map<Long, List<PhotoEvidenceResponse>> photosByCaseId = photosByCaseId(cases);
        Map<Long, List<CaseAssignmentResponse>> assignmentsByCaseId = assignmentsByCaseId(cases);

        return cases.stream()
                .map(inspectionCase -> InspectionCaseResponse.from(
                        inspectionCase,
                        photosByCaseId.getOrDefault(inspectionCase.id(), List.of()),
                        assignmentsByCaseId.getOrDefault(inspectionCase.id(), List.of())
                ))
                .toList();
    }

    @Override
    public InspectionCaseResponse findPublicStatus(String trackingCode, String applicantPhone) {
        String normalizedTrackingCode = normalizeRequired(trackingCode);
        String normalizedApplicantPhoneDigits = PhoneNormalizer.digitsOnly(applicantPhone);

        if (normalizedTrackingCode == null || normalizedTrackingCode.isBlank()
                || normalizedApplicantPhoneDigits == null || normalizedApplicantPhoneDigits.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe enviar codigo de caso y telefono");
        }

        return inspectionCaseRepository
                .findByTrackingCodeAndApplicantPhoneDigits(normalizedTrackingCode, normalizedApplicantPhoneDigits)
                .map(inspectionCase -> InspectionCaseResponse.from(
                        inspectionCase,
                        photoEvidenceRepository.findByCaseId(inspectionCase.id()).stream()
                                .map(PhotoEvidenceResponse::from)
                                .toList()
                ))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso no encontrado"));
    }

    @Override
    @Transactional
    public List<PhotoEvidenceResponse> addPhotos(Long caseId, Long uploadedByUserId, List<MultipartFile> photos) {
        InspectionCase inspectionCase = inspectionCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso no encontrado"));
        List<MultipartFile> normalizedPhotos = normalizePhotos(photos);
        if (normalizedPhotos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe subir al menos una imagen");
        }
        validatePhotos(normalizedPhotos);
        validateTotalPhotoLimit(caseId, normalizedPhotos.size());

        return storePhotos(
                inspectionCase.id(),
                inspectionCase.trackingCode(),
                normalizedPhotos,
                uploadedByUserId,
                false
        );
    }

    @Override
    @Transactional
    public InspectionCaseResponse assignEngineers(Long caseId, Long assignedByUserId, List<Long> engineerIds) {
        inspectionCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso no encontrado"));

        List<Long> normalizedEngineerIds = normalizeEngineerIds(engineerIds);
        if (normalizedEngineerIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe enviar al menos un ingeniero");
        }

        validateEngineers(normalizedEngineerIds);
        caseAssignmentRepository.createMany(caseId, normalizedEngineerIds, assignedByUserId);
        inspectionCaseRepository.updateStatus(caseId, InspectionCaseStatus.ASIGNADO);

        return findInternalCase(caseId);
    }

    @Override
    @Transactional
    public InspectionCaseResponse removeEngineerAssignment(Long caseId, Long engineerId) {
        inspectionCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso no encontrado"));

        if (!caseAssignmentRepository.deleteByCaseIdAndEngineerId(caseId, engineerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asignacion no encontrada");
        }

        if (caseAssignmentRepository.countByCaseId(caseId) == 0) {
            inspectionCaseRepository.updateStatus(caseId, InspectionCaseStatus.PENDIENTE);
        }

        return findInternalCase(caseId);
    }

    private List<PhotoEvidenceResponse> storePhotos(
            Long caseId,
            String trackingCode,
            List<MultipartFile> photos,
            Long uploadedByUserId,
            boolean publicUpload
    ) {
        List<PhotoEvidenceResponse> responses = new ArrayList<>();
        for (MultipartFile photo : photos) {
            StoredPhoto storedPhoto = photoStorageService.store(trackingCode, photo);
            PhotoEvidence photoEvidence = photoEvidenceRepository.create(new CreatePhotoEvidenceCommand(
                    caseId,
                    uploadedByUserId,
                    publicUpload,
                    storedPhoto.fileName(),
                    storedPhoto.fileUrl(),
                    storedPhoto.contentType(),
                    storedPhoto.sizeBytes()
            ));
            responses.add(PhotoEvidenceResponse.from(photoEvidence));
        }
        return responses;
    }

    private Map<Long, List<PhotoEvidenceResponse>> photosByCaseId(List<InspectionCase> cases) {
        List<Long> caseIds = cases.stream().map(InspectionCase::id).toList();
        Map<Long, List<PhotoEvidenceResponse>> result = new LinkedHashMap<>();

        for (PhotoEvidence photoEvidence : photoEvidenceRepository.findByCaseIds(caseIds)) {
            result.computeIfAbsent(photoEvidence.caseId(), ignored -> new ArrayList<>())
                    .add(PhotoEvidenceResponse.from(photoEvidence));
        }
        return result;
    }

    private Map<Long, List<CaseAssignmentResponse>> assignmentsByCaseId(List<InspectionCase> cases) {
        List<Long> caseIds = cases.stream().map(InspectionCase::id).toList();
        Map<Long, List<CaseAssignmentResponse>> result = new LinkedHashMap<>();

        for (CaseAssignment assignment : caseAssignmentRepository.findByCaseIds(caseIds)) {
            result.computeIfAbsent(assignment.caseId(), ignored -> new ArrayList<>())
                    .add(CaseAssignmentResponse.from(assignment));
        }
        return result;
    }

    private InspectionCaseResponse findInternalCase(Long caseId) {
        InspectionCase inspectionCase = inspectionCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso no encontrado"));
        List<PhotoEvidenceResponse> photos = photoEvidenceRepository.findByCaseId(caseId).stream()
                .map(PhotoEvidenceResponse::from)
                .toList();
        List<CaseAssignmentResponse> assignments = caseAssignmentRepository.findByCaseId(caseId).stream()
                .map(CaseAssignmentResponse::from)
                .toList();
        return InspectionCaseResponse.from(inspectionCase, photos, assignments);
    }

    private List<Long> normalizeEngineerIds(List<Long> engineerIds) {
        if (engineerIds == null) {
            return List.of();
        }

        Set<Long> seen = new HashSet<>();
        List<Long> normalized = new ArrayList<>();
        for (Long engineerId : engineerIds) {
            if (engineerId != null && seen.add(engineerId)) {
                normalized.add(engineerId);
            }
        }
        return normalized;
    }

    private void validateEngineers(List<Long> engineerIds) {
        for (Long engineerId : engineerIds) {
            InternalUser engineer = internalUserRepository.findById(engineerId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Ingeniero no encontrado: " + engineerId
                    ));
            if (engineer.role() != UserRole.ENGINEER) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El usuario " + engineerId + " no tiene rol ENGINEER"
                );
            }
            if (engineer.status() != UserStatus.ACTIVE) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El ingeniero " + engineerId + " no esta activo"
                );
            }
        }
    }

    private List<MultipartFile> normalizePhotos(List<MultipartFile> photos) {
        if (photos == null) {
            return List.of();
        }
        return photos.stream()
                .filter(photo -> photo != null && !photo.isEmpty())
                .toList();
    }

    private void validatePhotos(List<MultipartFile> photos) {
        if (photos.size() > MAX_PHOTOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pueden subir mas de 10 imagenes");
        }

        for (MultipartFile photo : photos) {
            if (photo.getSize() > MAX_PHOTO_SIZE_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cada imagen debe pesar maximo 10 MB");
            }
            if (photo.getContentType() == null || !photo.getContentType().startsWith("image/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten archivos de imagen");
            }
        }
    }

    private void validateTotalPhotoLimit(Long caseId, int newPhotoCount) {
        int existingPhotoCount = photoEvidenceRepository.findByCaseId(caseId).size();
        if (existingPhotoCount + newPhotoCount > MAX_PHOTOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El caso no puede tener mas de 10 imagenes");
        }
    }

    private String formatTrackingCode(int year, long trackingNumber) {
        return String.format("%s-%d-%0" + TRACKING_NUMBER_WIDTH + "d", TRACKING_PREFIX, year, trackingNumber);
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
