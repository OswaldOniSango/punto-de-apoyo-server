package com.puntodeapoyo.inspectioncases.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntodeapoyo.inspectioncases.dto.*;
import com.puntodeapoyo.inspectioncases.model.InspectionCasePriority;
import com.puntodeapoyo.inspectioncases.model.InspectionCaseStatus;
import com.puntodeapoyo.inspectioncases.service.InspectionCasePdfService;
import com.puntodeapoyo.inspectioncases.service.InspectionCaseService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class InspectionCaseController {

    private final InspectionCaseService inspectionCaseService;
    private final InspectionCasePdfService inspectionCasePdfService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public InspectionCaseController(
            InspectionCaseService inspectionCaseService,
            InspectionCasePdfService inspectionCasePdfService,
            ObjectMapper objectMapper,
            Validator validator
    ) {
        this.inspectionCaseService = inspectionCaseService;
        this.inspectionCasePdfService = inspectionCasePdfService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(
            value = "/api/public/inspection-cases",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public InspectionCaseResponse register(@Valid @RequestBody CreateInspectionCaseRequest request) {
        return inspectionCaseService.register(request);
    }

    @PostMapping(
            value = "/api/public/inspection-cases",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public InspectionCaseResponse registerWithPhotos(
            @RequestPart("case") String casePayload,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
            @RequestPart(value = "photo", required = false) List<MultipartFile> photo
    ) {
        CreateInspectionCaseRequest request = parseCasePayload(casePayload);
        validateCaseRequest(request);
        return inspectionCaseService.register(request, mergePhotos(photos, photo));
    }

    @GetMapping("/api/public/inspection-cases/status")
    public InspectionCaseResponse findPublicStatus(
            @RequestParam String trackingCode,
            @RequestParam String phone
    ) {
        return inspectionCaseService.findPublicStatus(trackingCode, phone);
    }

    @PostMapping(
            value = "/api/inspection-cases/{id}/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR', 'ENGINEER')")
    @ResponseStatus(HttpStatus.CREATED)
    public List<PhotoEvidenceResponse> addPhotos(
            @PathVariable Long id,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
            @RequestPart(value = "photo", required = false) List<MultipartFile> photo,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return inspectionCaseService.addPhotos(id, jwt.getClaim("user_id"), mergePhotos(photos, photo));
    }

    @PostMapping("/api/inspection-cases/{id}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public InspectionCaseResponse assignEngineers(
            @PathVariable Long id,
            @Valid @RequestBody AssignEngineersRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return inspectionCaseService.assignEngineers(id, jwt.getClaim("user_id"), request.engineerIds());
    }

    @DeleteMapping("/api/inspection-cases/{id}/assignments/{engineerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public InspectionCaseResponse removeEngineerAssignment(
            @PathVariable Long id,
            @PathVariable Long engineerId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return inspectionCaseService.removeEngineerAssignment(id, engineerId, jwt.getClaim("user_id"));
    }

    @PatchMapping("/api/inspection-cases/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public InspectionCaseResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInspectionCaseStatusRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return inspectionCaseService.updateAssignedCaseStatus(
                id,
                jwt.getClaim("user_id"),
                jwt.getClaimAsString("role"),
                request.status()
        );
    }

    @PostMapping(
            value = "/api/inspection-cases/{id}/technical-observations",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    @ResponseStatus(HttpStatus.CREATED)
    public TechnicalObservationResponse createTechnicalObservation(
            @PathVariable Long id,
            @Valid @RequestBody CreateTechnicalObservationRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return inspectionCaseService.createTechnicalObservation(
                id,
                jwt.getClaim("user_id"),
                jwt.getClaimAsString("role"),
                request,
                List.of()
        );
    }

    @PostMapping(
            value = "/api/inspection-cases/{id}/technical-observations",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    @ResponseStatus(HttpStatus.CREATED)
    public TechnicalObservationResponse createTechnicalObservationWithPhotos(
            @PathVariable Long id,
            @RequestPart("observation") String observationPayload,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
            @RequestPart(value = "photo", required = false) List<MultipartFile> photo,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CreateTechnicalObservationRequest request = parseObservationPayload(observationPayload);
        validateTechnicalObservationRequest(request);
        return inspectionCaseService.createTechnicalObservation(
                id,
                jwt.getClaim("user_id"),
                jwt.getClaimAsString("role"),
                request,
                mergePhotos(photos, photo)
        );
    }

    @GetMapping(
            value = "/api/inspection-cases/{id}/inspection-report.pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR', 'ENGINEER')")
    public ResponseEntity<byte[]> downloadInspectionReport(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        byte[] pdf = inspectionCasePdfService.generateInspectionReport(
                id,
                jwt.getClaim("user_id"),
                jwt.getClaimAsString("role")
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("inspection-case-" + id + ".pdf")
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/api/inspection-cases")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR', 'ENGINEER')")
    public List<InspectionCaseResponse> search(
            @RequestParam(required = false) String trackingCode,
            @RequestParam(required = false) InspectionCaseStatus status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) InspectionCasePriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDate
    ) {
        return inspectionCaseService.search(new InspectionCaseSearchCriteria(
                trackingCode,
                status,
                city,
                priority,
                createdDate
        ));
    }

    private CreateInspectionCaseRequest parseCasePayload(String casePayload) {
        try {
            return objectMapper.readValue(casePayload, CreateInspectionCaseRequest.class);
        } catch (JsonProcessingException exception) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El campo case debe ser un JSON valido",
                    exception
            );
        }
    }

    private void validateCaseRequest(CreateInspectionCaseRequest request) {
        Set<ConstraintViolation<CreateInspectionCaseRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private CreateTechnicalObservationRequest parseObservationPayload(String observationPayload) {
        try {
            return objectMapper.readValue(observationPayload, CreateTechnicalObservationRequest.class);
        } catch (JsonProcessingException exception) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El campo observation debe ser un JSON valido",
                    exception
            );
        }
    }

    private void validateTechnicalObservationRequest(CreateTechnicalObservationRequest request) {
        Set<ConstraintViolation<CreateTechnicalObservationRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private List<MultipartFile> mergePhotos(List<MultipartFile> photos, List<MultipartFile> photo) {
        List<MultipartFile> merged = new ArrayList<>();
        if (photos != null) {
            merged.addAll(photos);
        }
        if (photo != null) {
            merged.addAll(photo);
        }
        return merged;
    }
}
