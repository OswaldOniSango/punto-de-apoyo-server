package com.puntodeapoyo.inspectioncases.controller;

import com.puntodeapoyo.inspectioncases.dto.CreateInspectionCaseRequest;
import com.puntodeapoyo.inspectioncases.dto.InspectionCaseResponse;
import com.puntodeapoyo.inspectioncases.service.InspectionCaseRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/inspection-cases")
public class PublicInspectionCaseController {

    private final InspectionCaseRegistrationService inspectionCaseRegistrationService;

    public PublicInspectionCaseController(InspectionCaseRegistrationService inspectionCaseRegistrationService) {
        this.inspectionCaseRegistrationService = inspectionCaseRegistrationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InspectionCaseResponse register(@Valid @RequestBody CreateInspectionCaseRequest request) {
        return inspectionCaseRegistrationService.register(request);
    }
}
