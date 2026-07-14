package com.a0000.digicompass.modules.upload;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadService {

    UploadResult uploadImage(MultipartFile file);

    UploadResult uploadImage(MultipartFile file, String productName, String imageUsage);

    record UploadResult(String url, String objectName, String originalFilename, ImageAuditResult audit) {}

    record ImageAuditResult(
            Integer width,
            Integer height,
            Double aspectRatio,
            String status,
            String summary,
            List<String> warnings,
            List<String> suggestions,
            String recommendedUsage
    ) {}
}
