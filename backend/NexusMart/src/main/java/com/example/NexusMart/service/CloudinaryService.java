// File: backend/NexusMart/src/main/java/com/example/NexusMart/service/CloudinaryService.java
package com.example.NexusMart.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.cloud-name}") String cloudName,
                             @Value("${cloudinary.api-key}") String apiKey,
                             @Value("${cloudinary.api-secret}") String apiSecret) {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    public Map<String, Object> uploadFile(byte[] fileBytes, String folder) throws IOException {
        return cloudinary.uploader().upload(fileBytes, ObjectUtils.asMap(
                "folder", folder,
                "overwrite", true,
                "resource_type", "image"));
    }

    public Map<String, Object> deleteFile(String publicId, String folder) throws IOException {
        Map<String, Object> result = cloudinary.uploader().destroy(folder + "/" + publicId, ObjectUtils.asMap(
                "resource_type", "image"));
        log.info("Cloudinary delete result: {}", result);
        return result;
    }

    public String extractPublicIdFromUrl(String url) {
        log.info("Extracting public ID from URL: {}", url);
        String[] parts = url.split("/");
        String publicIdWithExtension = parts[parts.length - 1];
        String publicId = publicIdWithExtension.substring(0, publicIdWithExtension.lastIndexOf('.'));
        log.info("Extracted public ID: {}", publicId);
        return publicId;
    }
}
