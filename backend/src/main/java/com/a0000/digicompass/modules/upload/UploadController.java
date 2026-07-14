package com.a0000.digicompass.modules.upload;

import com.a0000.digicompass.common.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/upload")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/image")
    public ApiResponse<UploadService.UploadResult> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "imageUsage", required = false) String imageUsage
    ) {
        return ApiResponse.success(uploadService.uploadImage(file, productName, imageUsage));
    }
}
