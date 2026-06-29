package com.puntodeapoyo.inspectioncases.service;

import java.util.List;

import com.puntodeapoyo.inspectioncases.dto.CreateInspectionCaseRequest;
import com.puntodeapoyo.inspectioncases.dto.InspectionCaseResponse;
import com.puntodeapoyo.inspectioncases.dto.InspectionCaseSearchCriteria;

public interface InspectionCaseService {

    InspectionCaseResponse register(CreateInspectionCaseRequest request);

    InspectionCaseResponse findPublicStatus(String trackingCode, String applicantPhone);

    List<InspectionCaseResponse> search(InspectionCaseSearchCriteria criteria);
}
