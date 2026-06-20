package com.example.ThangLongUniversityWeb.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String safeOriginalName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String contentType = file.getContentType();
        boolean isImage = contentType != null && contentType.startsWith("image/");
        String resourceType = isImage ? "image" : "raw";

        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", resourceType);

        // Raw files (pdf/docx/...) need explicit public_id + filename override
        // so Cloudinary delivery URL keeps file extension and downloads cleanly.
        if (!isImage) {
            options.put("public_id", "chat_files/" + UUID.randomUUID() + "_" + safeOriginalName);
            options.put("filename_override", safeOriginalName);
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    options
            );
            return result.get("secure_url").toString();
        } catch (IOException | RuntimeException ex) {
            throw new RuntimeException(
                    "Upload file len Cloudinary that bai. Kiem tra CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY va CLOUDINARY_API_SECRET. Chi tiet: "
                            + ex.getMessage(),
                    ex
            );
        }
    }
}
