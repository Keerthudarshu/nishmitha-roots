package com.eduprajna.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * CloudinaryService handles file uploads to Cloudinary
 * This provides permanent, CDN-backed image storage
 */
@Service
public class CloudinaryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);
    
    private final Cloudinary cloudinary;
    
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }
    
    /**
     * Upload a file to Cloudinary
     * @param file The multipart file to upload
     * @return The secure HTTPS URL of the uploaded image
     * @throws IOException if upload fails
     */
    public String uploadFile(MultipartFile file) throws IOException {
        try {
            logger.info("Uploading file to Cloudinary: {} (size: {} bytes)", 
                file.getOriginalFilename(), file.getSize());
            
            // Generate unique public ID to avoid conflicts
            String publicId = "nishmitha-roots/" + UUID.randomUUID().toString();
            
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", "nishmitha-roots/products", // Organize in folders
                    "resource_type", "auto", // Auto-detect file type
                    "overwrite", false, // Don't overwrite existing files
                    "use_filename", true, // Use original filename as part of URL
                    "unique_filename", true // Ensure unique filename
                ));
            
            String secureUrl = uploadResult.get("secure_url").toString();
            logger.info("File uploaded successfully to Cloudinary: {}", secureUrl);
            
            return secureUrl;
            
        } catch (Exception e) {
            logger.error("Failed to upload file to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to Cloudinary: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete a file from Cloudinary
     * @param imageUrl The Cloudinary URL of the image to delete
     * @return true if deletion was successful
     */
    public boolean deleteFile(String imageUrl) {
        try {
            if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
                logger.warn("Invalid Cloudinary URL for deletion: {}", imageUrl);
                return false;
            }
            
            // Extract public ID from Cloudinary URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId == null) {
                logger.warn("Could not extract public ID from URL: {}", imageUrl);
                return false;
            }
            
            logger.info("Deleting file from Cloudinary with public ID: {}", publicId);
            
            Map deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String result = deleteResult.get("result").toString();
            
            boolean success = "ok".equals(result);
            logger.info("Cloudinary deletion result for {}: {}", publicId, result);
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to delete file from Cloudinary: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Extract public ID from Cloudinary URL
     * @param cloudinaryUrl Full Cloudinary URL
     * @return Public ID or null if extraction fails
     */
    private String extractPublicIdFromUrl(String cloudinaryUrl) {
        try {
            // Cloudinary URL format: https://res.cloudinary.com/cloud_name/image/upload/v123456/folder/filename.ext
            // We need to extract: folder/filename (without extension)
            
            if (!cloudinaryUrl.contains("/upload/")) {
                return null;
            }
            
            String[] parts = cloudinaryUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }
            
            String afterUpload = parts[1];
            // Remove version number if present (v123456/)
            if (afterUpload.matches("^v\\d+/.*")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
            }
            
            // Remove file extension
            int dotIndex = afterUpload.lastIndexOf('.');
            if (dotIndex > 0) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }
            
            return afterUpload;
            
        } catch (Exception e) {
            logger.error("Error extracting public ID from URL: {}", cloudinaryUrl, e);
            return null;
        }
    }
    
    /**
     * Check if Cloudinary is properly configured
     * @return true if Cloudinary is configured and accessible
     */
    public boolean isConfigured() {
        try {
            // Try to get account details to verify configuration
            Map accountResult = cloudinary.api().usage(ObjectUtils.emptyMap());
            logger.info("Cloudinary is properly configured. Usage check successful.");
            return true;
        } catch (Exception e) {
            logger.error("Cloudinary configuration check failed: {}", e.getMessage());
            return false;
        }
    }
}