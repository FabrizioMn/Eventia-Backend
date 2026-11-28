package com.backend.eventia.service;

import com.backend.eventia.service.impl.CloudinaryServiceImpl;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private CloudinaryServiceImpl cloudinaryService;

    @BeforeEach
    void setUp() {
        cloudinaryService = new CloudinaryServiceImpl(cloudinary);
    }

    @Test
    @DisplayName("Lanza excepción si el archivo es nulo o vacío")
    void shouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> cloudinaryService.uploadImage(emptyFile, "banners"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El archivo de imagen no puede estar vacío");

        assertThatThrownBy(() -> cloudinaryService.uploadImage(null, "banners"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El archivo de imagen no puede estar vacío");
    }

    @Test
    @DisplayName("Lanza excepción si el tipo de archivo no es una imagen permitida")
    void shouldThrowExceptionWhenFileTypeIsInvalid() {
        MockMultipartFile pdfFile = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> cloudinaryService.uploadImage(pdfFile, "banners"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Formato no permitido");
    }

    @Test
    @DisplayName("Sube imagen exitosamente a Cloudinary y retorna datos")
    void shouldUploadImageSuccessfully() throws IOException {
        MockMultipartFile validImage = new MockMultipartFile(
                "file",
                "banner.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/example-cloud/image/upload/v1234/eventia/banner.png",
                "public_id", "eventia/banners/banner123",
                "format", "png"
        ));

        Map<String, Object> result = cloudinaryService.uploadImage(validImage, "eventia/banners");

        assertThat(result).isNotNull();
        assertThat(result.get("secure_url")).isEqualTo("https://res.cloudinary.com/example-cloud/image/upload/v1234/eventia/banner.png");
        verify(uploader).upload(any(byte[].class), anyMap());
    }
}
