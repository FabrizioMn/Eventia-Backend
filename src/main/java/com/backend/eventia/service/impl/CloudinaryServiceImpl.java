package com.backend.eventia.service.impl;

import com.backend.eventia.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public Map<String, Object> uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen no puede estar vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El tamaño del archivo no puede exceder 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Formato no permitido. Solo se aceptan imágenes JPG, PNG, WEBP o GIF");
        }

        try {
            String targetFolder = (folder != null && !folder.isBlank()) ? folder : "eventia/banners";
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", targetFolder,
                    "resource_type", "image"
            ));
            return uploadResult;
        } catch (IOException e) {
            log.error("Error al subir archivo a Cloudinary", e);
            throw new IllegalArgumentException("Error al procesar la imagen para Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("El publicId no puede estar vacío");
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return result;
        } catch (IOException e) {
            log.error("Error al eliminar archivo de Cloudinary", e);
            throw new IllegalArgumentException("Error al eliminar la imagen en Cloudinary: " + e.getMessage());
        }
    }
}
