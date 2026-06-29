package com.puntodeapoyo.inspectioncases.service;

import com.puntodeapoyo.inspectioncases.dto.CreateInspectionCaseRequest;
import com.puntodeapoyo.inspectioncases.dto.InspectionCaseResponse;

public interface InspectionCaseRegistrationService {

    InspectionCaseResponse register(CreateInspectionCaseRequest request);
}
