package com.puntodeapoyo.inspectioncases.service.impl;

import java.time.Year;
import java.util.List;

import com.puntodeapoyo.common.PhoneNormalizer;
import com.puntodeapoyo.inspectioncases.dto.CreateInspectionCaseRequest;
import com.puntodeapoyo.inspectioncases.dto.InspectionCaseResponse;
import com.puntodeapoyo.inspectioncases.dto.InspectionCaseSearchCriteria;
import com.puntodeapoyo.inspectioncases.model.InspectionCase;
import com.puntodeapoyo.inspectioncases.model.InspectionCaseStatus;
import com.puntodeapoyo.inspectioncases.repository.InspectionCaseRepository;
import com.puntodeapoyo.inspectioncases.repository.InspectionCaseRepository.CreateInspectionCaseCommand;
import com.puntodeapoyo.inspectioncases.service.InspectionCaseService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InspectionCaseServiceImpl implements InspectionCaseService {

    private static final String TRACKING_PREFIX = "VZ";
    private static final int TRACKING_NUMBER_WIDTH = 8;

    private final InspectionCaseRepository inspectionCaseRepository;

    public InspectionCaseServiceImpl(InspectionCaseRepository inspectionCaseRepository) {
        this.inspectionCaseRepository = inspectionCaseRepository;
    }

    @Override
    @Transactional
    public InspectionCaseResponse register(CreateInspectionCaseRequest request) {
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

        return InspectionCaseResponse.from(inspectionCase);
    }

    @Override
    public List<InspectionCaseResponse> search(InspectionCaseSearchCriteria criteria) {
        return inspectionCaseRepository.search(criteria).stream()
                .map(InspectionCaseResponse::from)
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
                .map(InspectionCaseResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso no encontrado"));
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
