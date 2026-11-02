package com.project.sneakermarket.common.security;

import com.project.sneakermarket.common.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

public final class ImageUploadValidator {

    private static final long MAX_PIXELS = 40_000_000L;

    private ImageUploadValidator() {
    }

    public static void validate(
            MultipartFile file,
            String extension
    ) {
        try (ImageInputStream input =
                     ImageIO.createImageInputStream(file.getInputStream())) {

            if (input == null) {
                throw invalidImage();
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalidImage();
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);

                String detectedFormat = reader.getFormatName()
                        .toLowerCase(Locale.ROOT);
                if (!matchesExtension(detectedFormat, extension)) {
                    throw new BusinessException("圖片副檔名與實際內容不一致");
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0
                        || height <= 0
                        || (long) width * height > MAX_PIXELS) {
                    throw new BusinessException("圖片尺寸過大或不合法");
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw invalidImage();
        }
    }

    private static boolean matchesExtension(
            String detectedFormat,
            String extension
    ) {
        String normalizedExtension = extension.toLowerCase(Locale.ROOT);

        if (Set.of("jpg", "jpeg").contains(normalizedExtension)) {
            return Set.of("jpg", "jpeg").contains(detectedFormat);
        }

        return normalizedExtension.equals(detectedFormat);
    }

    private static BusinessException invalidImage() {
        return new BusinessException("檔案不是有效的圖片");
    }
}
