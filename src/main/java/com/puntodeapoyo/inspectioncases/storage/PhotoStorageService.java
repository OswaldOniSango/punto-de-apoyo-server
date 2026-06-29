package com.puntodeapoyo.inspectioncases.storage;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoStorageService {

    StoredPhoto store(String trackingCode, MultipartFile file);
}
