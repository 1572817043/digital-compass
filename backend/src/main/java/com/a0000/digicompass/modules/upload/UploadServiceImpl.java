package com.a0000.digicompass.modules.upload;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadServiceImpl implements UploadService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024;
    private static final DateTimeFormatter PATH_FMT = DateTimeFormatter.ofPattern("yyyy/MM");

    private final OssConfig ossConfig;

    public UploadServiceImpl(OssConfig ossConfig) {
        this.ossConfig = ossConfig;
    }

    @Override
    public UploadResult uploadImage(MultipartFile file) {
        return uploadImage(file, null, null);
    }

    @Override
    public UploadResult uploadImage(MultipartFile file, String productName, String imageUsage) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的文件");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("只允许上传 jpg、jpeg、png、webp 格式的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("图片大小不能超过 5MB");
        }
        if (ossConfig.getAccessKeyId() == null || ossConfig.getAccessKeyId().isBlank()) {
            throw new IllegalStateException("OSS 密钥未配置，无法上传文件");
        }
        ImageAuditResult audit = inspectImageQuality(file, productName, imageUsage);

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String datePath = LocalDate.now().format(PATH_FMT);
        String objectName = "products/" + datePath + "/" + UUID.randomUUID() + extension;

        OSS ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );

        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream, buildObjectMetadata(file));
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        } finally {
            ossClient.shutdown();
        }

        String url = "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectName;
        return new UploadResult(url, objectName, originalFilename, audit);
    }

    ImageAuditResult inspectImageQuality(MultipartFile file, String productName, String imageUsage) {
        List<String> warnings = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        Integer width = null;
        Integer height = null;
        Double aspectRatio = null;

        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                warnings.add("图片尺寸读取失败，请人工确认图片是否完整");
            } else {
                width = image.getWidth();
                height = image.getHeight();
                aspectRatio = height > 0 ? Math.round((width * 1.0 / height) * 100.0) / 100.0 : null;
                if (width < 800 || height < 800) {
                    warnings.add("图片低于建议尺寸 800x800，列表和详情页可能不够清晰");
                }
                if (aspectRatio != null && (aspectRatio < 0.75 || aspectRatio > 1.35)) {
                    warnings.add("图片宽高比不适合作为主图，建议裁剪为 1:1 或 4:3");
                }
            }
        } catch (Exception e) {
            warnings.add("图片读取失败，请人工确认文件是否损坏");
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        if (filename.contains("dxomark") || filename.contains("score") || filename.contains("rank")
                || filename.contains("award") || filename.contains("评测") || filename.contains("奖")) {
            warnings.add("文件名疑似评测、跑分或奖项图，不建议直接作为主图");
        }

        boolean mainUsage = imageUsage == null || imageUsage.isBlank() || "MAIN".equalsIgnoreCase(imageUsage);
        if (warnings.isEmpty()) {
            suggestions.add(mainUsage ? "可绑定为主图" : "可作为图集图片");
        } else if (mainUsage) {
            suggestions.add("建议人工复核后再设为主图");
        } else {
            suggestions.add("可作为图集候选，主图建议另选干净产品图");
        }
        if (productName != null && !productName.isBlank()) {
            suggestions.add("请人工确认图片主体与「" + productName.trim() + "」一致");
        }

        String status = warnings.isEmpty() ? "PASS" : "REVIEW";
        String recommendedUsage = warnings.isEmpty() && mainUsage ? "MAIN" : "GALLERY";
        String summary = warnings.isEmpty()
                ? (mainUsage ? "图片尺寸和比例符合规范，适合作为主图。" : "图片尺寸和比例符合规范，可作为图集。")
                : "图片需要人工复核后再使用。";
        return new ImageAuditResult(width, height, aspectRatio, status, summary, warnings, suggestions, recommendedUsage);
    }

    ObjectMetadata buildObjectMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.isBlank()) {
            String safeFilename = originalFilename.replace("\"", "");
            metadata.setContentDisposition("inline; filename=\"" + safeFilename + "\"");
        }

        return metadata;
    }
}
