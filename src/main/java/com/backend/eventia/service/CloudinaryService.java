package com.backend.eventia.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface CloudinaryService {
    Map<String, Object> uploadImage(MultipartFile file, String folder);
    Map<String, Object> deleteImage(String publicId);
}
