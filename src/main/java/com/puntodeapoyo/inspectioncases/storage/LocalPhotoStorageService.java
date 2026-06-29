package com.puntodeapoyo.inspectioncases.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalPhotoStorageService implements PhotoStorageService {

    private final Path uploadDir;

    public LocalPhotoStorageService(@Value("${app.storage.upload-dir}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public StoredPhoto store(String trackingCode, MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "photo" : file.getOriginalFilename()
        );
        String extension = extensionFrom(originalFileName);
        String storedFileName = UUID.randomUUID() + extension;
        Path caseDir = uploadDir.resolve("inspection-cases").resolve(trackingCode).normalize();
        Path target = caseDir.resolve(storedFileName).normalize();

        if (!target.startsWith(caseDir)) {
            throw new IllegalArgumentException("Nombre de archivo invalido");
        }

        try {
            Files.createDirectories(caseDir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new UncheckedIOException("No se pudo guardar la foto", exception);
        }

        return new StoredPhoto(
                originalFileName,
                "/uploads/inspection-cases/" + trackingCode + "/" + storedFileName,
                file.getContentType(),
                file.getSize()
        );
    }

    private String extensionFrom(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index).toLowerCase(Locale.ROOT);
    }
}
