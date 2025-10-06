package com.eduprajna.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Keep local file serving for fallback compatibility
        // Note: With Cloudinary integration, most images will be served directly from CDN
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/", "file:./uploads/")
                .setCachePeriod(86400); // Cache for 24 hours
        
        // Serve local admin product images (fallback only)
        registry.addResourceHandler("/admin/products/images/**")
                .addResourceLocations("file:./uploads/", "classpath:/static/uploads/")
                .setCachePeriod(86400); // Cache for 24 hours
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:3000", 
                    "http://127.0.0.1:3000",
                    "https://nishmitha-roots.vercel.app"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
