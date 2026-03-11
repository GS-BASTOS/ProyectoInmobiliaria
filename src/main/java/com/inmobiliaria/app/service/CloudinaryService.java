package com.inmobiliaria.app.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<String, String> upload(MultipartFile file, String mediaType) throws IOException {
        String resourceType = "VIDEO".equals(mediaType) ? "video" : "auto";

        Map<?, ?> result = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap("resource_type", resourceType)
        );

        String url        = (String) result.get("secure_url");
        String publicId   = (String) result.get("public_id");
        // Para PDFs/docs Cloudinary genera una URL de descarga directa
        String downloadUrl = url;

        return Map.of(
            "url",        url,
            "publicId",   publicId,
            "downloadUrl", downloadUrl
        );
    }

    public void delete(String publicId, String mediaType) throws IOException {
        String resourceType = "VIDEO".equals(mediaType) ? "video" : "image";
        cloudinary.uploader().destroy(
            publicId,
            ObjectUtils.asMap("resource_type", resourceType)
        );
    }
}
