package com.puntodeapoyo.inspectioncases.service;

import java.util.List;

import com.puntodeapoyo.inspectioncases.dto.CreateInspectionCaseRequest;
import com.puntodeapoyo.inspectioncases.dto.InspectionCaseResponse;
import com.puntodeapoyo.inspectioncases.dto.InspectionCaseSearchCriteria;
import com.puntodeapoyo.inspectioncases.dto.PhotoEvidenceResponse;
import org.springframework.web.multipart.MultipartFile;

public interface InspectionCaseService {

    InspectionCaseResponse register(CreateInspectionCaseRequest request);

    InspectionCaseResponse register(CreateInspectionCaseRequest request, List<MultipartFile> photos);

    InspectionCaseResponse findPublicStatus(String trackingCode, String applicantPhone);

    List<PhotoEvidenceResponse> addPhotos(Long caseId, Long uploadedByUserId, List<MultipartFile> photos);

    List<InspectionCaseResponse> search(InspectionCaseSearchCriteria criteria);
}
