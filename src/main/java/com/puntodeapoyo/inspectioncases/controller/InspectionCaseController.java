package com.puntodeapoyo.inspectioncases.controller;

import java.time.LocalDate;
import java.util.List;

import com.puntodeapoyo.inspectioncases.dto.CreateInspectionCaseRequest;
import com.puntodeapoyo.inspectioncases.dto.InspectionCaseResponse;
import com.puntodeapoyo.inspectioncases.dto.InspectionCaseSearchCriteria;
import com.puntodeapoyo.inspectioncases.model.InspectionCasePriority;
import com.puntodeapoyo.inspectioncases.model.InspectionCaseStatus;
import com.puntodeapoyo.inspectioncases.service.InspectionCaseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InspectionCaseController {

    private final InspectionCaseService inspectionCaseService;

    public InspectionCaseController(InspectionCaseService inspectionCaseService) {
        this.inspectionCaseService = inspectionCaseService;
    }

    @PostMapping("/api/public/inspection-cases")
    @ResponseStatus(HttpStatus.CREATED)
    public InspectionCaseResponse register(@Valid @RequestBody CreateInspectionCaseRequest request) {
        return inspectionCaseService.register(request);
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
}
