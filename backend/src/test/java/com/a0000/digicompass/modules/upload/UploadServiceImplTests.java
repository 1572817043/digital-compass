package com.a0000.digicompass.modules.upload;

import static org.assertj.core.api.Assertions.assertThat;

import com.aliyun.oss.model.ObjectMetadata;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class UploadServiceImplTests {

    @Test
    void buildObjectMetadataKeepsImageReadableInBrowser() {
        UploadServiceImpl uploadService = new UploadServiceImpl(new OssConfig());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "phone.jpg",
                "image/jpeg",
                new byte[] {1, 2, 3}
        );

        ObjectMetadata metadata = uploadService.buildObjectMetadata(file);

        assertThat(metadata.getContentType()).isEqualTo("image/jpeg");
        assertThat(metadata.getContentLength()).isEqualTo(3);
        assertThat(metadata.getContentDisposition()).isEqualTo("inline; filename=\"phone.jpg\"");
    }

    @Test
    void inspectImageQualityReportsUsableMainImage() throws Exception {
        UploadServiceImpl uploadService = new UploadServiceImpl(new OssConfig());
        MockMultipartFile file = pngFile("iphone-16-pro-main.png", 1200, 900);

        UploadService.ImageAuditResult result = uploadService.inspectImageQuality(file, "iPhone 16 Pro", "MAIN");

        assertThat(result.width()).isEqualTo(1200);
        assertThat(result.height()).isEqualTo(900);
        assertThat(result.status()).isEqualTo("PASS");
        assertThat(result.summary()).contains("适合作为主图");
        assertThat(result.suggestions()).contains("可绑定为主图");
    }

    @Test
    void inspectImageQualityWarnsForSmallAwardLikeImage() throws Exception {
        UploadServiceImpl uploadService = new UploadServiceImpl(new OssConfig());
        MockMultipartFile file = pngFile("dxomark-score.png", 500, 500);

        UploadService.ImageAuditResult result = uploadService.inspectImageQuality(file, "荣耀 Magic6 Pro", "MAIN");

        assertThat(result.status()).isEqualTo("REVIEW");
        assertThat(result.warnings()).anyMatch(item -> item.contains("建议尺寸"));
        assertThat(result.warnings()).anyMatch(item -> item.contains("评测") || item.contains("奖项"));
        assertThat(result.suggestions()).contains("建议人工复核后再设为主图");
    }

    private MockMultipartFile pngFile(String filename, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(width / 4, height / 4, width / 2, height / 2);
        graphics.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return new MockMultipartFile("file", filename, "image/png", out.toByteArray());
    }
}
