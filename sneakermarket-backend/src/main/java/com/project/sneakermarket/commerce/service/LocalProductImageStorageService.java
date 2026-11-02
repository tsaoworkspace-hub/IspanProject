package com.project.sneakermarket.commerce.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.project.sneakermarket.common.exception.BusinessException;
import com.project.sneakermarket.common.security.ImageUploadValidator;

@Service
public class LocalProductImageStorageService implements ProductImageStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp");

    private static final String URL_PREFIX = "/uploads/products/";

    private final Path uploadRoot;

    public LocalProductImageStorageService(
            @Value("${product.image.upload-dir:uploads/products}")
            String uploadDir) {

        this.uploadRoot = Path.of(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上傳圖片不可為空");
        }

        String extension = getExtension(file.getOriginalFilename());

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("圖片格式僅支援 JPG、JPEG、PNG、WEBP");
        }

        ImageUploadValidator.validate(file, extension);

        String storedFileName = UUID.randomUUID() + "." + extension;

        Path targetFile = uploadRoot.resolve(storedFileName).normalize();

        if (!targetFile.startsWith(uploadRoot)) {
            throw new BusinessException("圖片儲存路徑不合法");
        }

        try {
            Files.createDirectories(uploadRoot);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("圖片儲存失敗", e);
        }

        return URL_PREFIX + storedFileName;
    }

    @Override
    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(URL_PREFIX)) {
            return;
        }

        String fileName = imageUrl.substring(URL_PREFIX.length());
        Path targetFile = uploadRoot.resolve(fileName).normalize();

        if (!targetFile.startsWith(uploadRoot)) {
            return;
        }

        try {
            Files.deleteIfExists(targetFile);
        } catch (IOException e) {
            throw new IllegalStateException("圖片刪除失敗", e);
        }
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException("無法判斷圖片格式");
        }

        int dotIndex = originalFilename.lastIndexOf(".");

        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw new BusinessException("圖片缺少有效副檔名");
        }

        return originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
