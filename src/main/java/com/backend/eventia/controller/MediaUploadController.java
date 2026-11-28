package com.backend.eventia.controller;

import com.backend.eventia.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class MediaUploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "eventia/banners") String folder
    ) {
        Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, folder);
        return ResponseEntity.ok(Map.of(
                "url", uploadResult.getOrDefault("secure_url", uploadResult.get("url")),
                "publicId", uploadResult.getOrDefault("public_id", ""),
                "format", uploadResult.getOrDefault("format", ""),
                "width", uploadResult.getOrDefault("width", 0),
                "height", uploadResult.getOrDefault("height", 0)
        ));
    }
}
