package com.puntodeapoyo.inspectioncases.storage;

public record StoredPhoto(
        String fileName,
        String fileUrl,
        String contentType,
        Long sizeBytes
) {
}
