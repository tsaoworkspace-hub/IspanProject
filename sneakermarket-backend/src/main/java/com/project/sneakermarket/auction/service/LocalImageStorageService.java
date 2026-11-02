package com.project.sneakermarket.auction.service;

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

/**
 * 競標商品本機圖片儲存服務。
 *
 * 開發階段先將圖片儲存在後端專案外部資料夾，
 * 資料庫只保存可供前端使用的相對 URL。
 */
@Service
public class LocalImageStorageService implements ImageStorageService {

    /**
     * 目前允許的圖片副檔名。
     */
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp");

    /**
     * 圖片實際存放根目錄。
     *
     * 若設定檔沒有指定 auction.image.upload-dir，
     * 預設使用 uploads/auctions。
     */
    private final Path uploadRoot;

    public LocalImageStorageService(
            @Value("${auction.image.upload-dir:uploads/auctions}")
            String uploadDir) {

        this.uploadRoot = Path.of(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    /**
     * 儲存一張競標商品圖片。
     *
     * 實際位置：
     * uploads/auctions/{auctionProductId}/{UUID}.jpg
     *
     * 回傳給資料庫：
     * /uploads/auctions/{auctionProductId}/{UUID}.jpg
     */
    @Override
    public String store(
            MultipartFile file,
            Long auctionProductId) {

        if (file == null || file.isEmpty()) {
            throw new BusinessException("上傳圖片不可為空");
        }

        if (auctionProductId == null || auctionProductId <= 0) {
            throw new BusinessException("競標商品 ID 不正確");
        }

        String extension = getExtension(file.getOriginalFilename());

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(
                    "圖片格式僅支援 JPG、JPEG、PNG、WEBP"
            );
        }

        ImageUploadValidator.validate(file, extension);

        // 不直接使用使用者原始檔名，避免撞名與特殊路徑問題。
        String storedFileName =
                UUID.randomUUID() + "." + extension;

        Path productDirectory = uploadRoot
                .resolve(String.valueOf(auctionProductId))
                .normalize();

        Path targetFile = productDirectory
                .resolve(storedFileName)
                .normalize();

        /*
         * 防止非預期路徑跳脫 uploadRoot。
         * 正常情況下 auctionProductId 為 Long，
         * 這裡仍保留最後一道安全檢查。
         */
        if (!targetFile.startsWith(uploadRoot)) {
            throw new BusinessException("圖片儲存路徑不合法");
        }

        try {
            Files.createDirectories(productDirectory);

            Files.copy(
                    file.getInputStream(),
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {
            /*
             * 這屬於伺服器檔案系統錯誤，不是使用者輸入錯誤。
             * 後續建立統一圖片例外處理時再改成專用 Exception。
             */
            throw new IllegalStateException("圖片儲存失敗", e);
        }

        return "/uploads/auctions/"
                + auctionProductId
                + "/"
                + storedFileName;
    }

    /**
     * 刪除先前已儲存的圖片。
     *
     * 後續建立競標 Transaction 失敗時，
     * 可用此方法清理已經寫入硬碟的孤兒檔案。
     */
    @Override
    public void delete(String imageUrl) {

        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String prefix = "/uploads/auctions/";

        if (!imageUrl.startsWith(prefix)) {
            return;
        }

        String relativePath =
                imageUrl.substring(prefix.length());

        Path targetFile = uploadRoot
                .resolve(relativePath)
                .normalize();

        // 不允許刪除 uploadRoot 以外的檔案。
        if (!targetFile.startsWith(uploadRoot)) {
            return;
        }

        try {
            Files.deleteIfExists(targetFile);

        } catch (IOException e) {
            throw new IllegalStateException("圖片刪除失敗", e);
        }
    }

    /**
     * 從原始檔名取得副檔名。
     */
    private String getExtension(String originalFilename) {

        if (originalFilename == null ||
                originalFilename.isBlank()) {
            throw new BusinessException("無法判斷圖片格式");
        }

        int dotIndex = originalFilename.lastIndexOf('.');

        if (dotIndex < 0 ||
                dotIndex == originalFilename.length() - 1) {
            throw new BusinessException("圖片缺少有效副檔名");
        }

        return originalFilename
                .substring(dotIndex + 1)
                .toLowerCase(Locale.ROOT);
    }
}
