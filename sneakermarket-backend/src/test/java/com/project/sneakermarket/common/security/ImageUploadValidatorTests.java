package com.project.sneakermarket.common.security;

import com.project.sneakermarket.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageUploadValidatorTests {

    @Test
    void acceptsAnImageWhoseContentMatchesItsExtension()
            throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "safe.png",
                "image/png",
                createPng()
        );

        assertDoesNotThrow(
                () -> ImageUploadValidator.validate(file, "png")
        );
    }

    @Test
    void rejectsNonImageContent() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.png",
                "image/png",
                "not an image".getBytes()
        );

        assertThrows(
                BusinessException.class,
                () -> ImageUploadValidator.validate(file, "png")
        );
    }

    @Test
    void rejectsMismatchedExtensions() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.jpg",
                "image/jpeg",
                createPng()
        );

        assertThrows(
                BusinessException.class,
                () -> ImageUploadValidator.validate(file, "jpg")
        );
    }

    private byte[] createPng() throws IOException {
        BufferedImage image = new BufferedImage(
                10,
                10,
                BufferedImage.TYPE_INT_RGB
        );
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
