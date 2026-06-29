package com.puntodeapoyo.inspectioncases.service;

public interface InspectionCasePdfService {

    byte[] generateInspectionReport(Long caseId, Long currentUserId, String currentUserRole);
}
