package com.eduprajna.Controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.eduprajna.entity.Product;
import com.eduprajna.service.ProductService;
import com.eduprajna.service.StorageService;

@RestController
@RequestMapping("/api/admin/products")
// Allow local dev, Vercel preview and production frontend domains
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "https://nishmitha-roots.vercel.app", "https://rootstraditional.in", "https://www.rootstraditional.in"}, allowCredentials = "true")

public class ProductController {
    private final Logger log = LoggerFactory.getLogger(ProductController.class);
    @Autowired
    private ProductService productService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private com.eduprajna.service.CloudinaryStorageService cloudinaryStorageService;

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        List<Product> products = productService.getAll();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
                .body(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        Product p = productService.getById(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
                .body(p);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Product> create(
            @RequestPart("product") Product p,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            com.eduprajna.service.CloudinaryStorageService.UploadResult res = cloudinaryStorageService.upload(imageFile);
            if (res != null) {
                p.setImageUrl(res.getUrl());
                p.setImagePublicId(res.getPublicId());
            }
        }
        Product saved = productService.save(p);
        return ResponseEntity.ok(saved);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @RequestPart("product") Product p,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {
        p.setId(id);
        if (imageFile != null && !imageFile.isEmpty()) {
            com.eduprajna.service.CloudinaryStorageService.UploadResult res = cloudinaryStorageService.upload(imageFile);
            if (res != null) {
                p.setImageUrl(res.getUrl());
                p.setImagePublicId(res.getPublicId());
            }
        }
        return ResponseEntity.ok(productService.save(p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // Load product to get image URL before deleting DB row
        try {
            Product existing = productService.getById(id);
            if (existing != null && existing.getImageUrl() != null) {
                // Prefer deleting by saved public id (more reliable). Fallback to stored URL.
                if (existing.getImagePublicId() != null && !existing.getImagePublicId().isEmpty()) {
                    cloudinaryStorageService.delete(existing.getImagePublicId());
                } else {
                    cloudinaryStorageService.delete(existing.getImageUrl());
                }
            }
        } catch (Exception ignored) {
            // Ignore errors during image deletion; proceed to delete DB row
        }
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Serve uploaded images via API so frontend can display them
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws IOException {
        long start = System.currentTimeMillis();
        Resource resource = storageService.loadAsResource(filename);
        long readDuration = System.currentTimeMillis() - start;
        MediaType contentType = storageService.probeMediaType(filename);

        // Log small diagnostic info to help debug cold-start vs file-read slowness
        try {
            log.info("Image request served: {} (readDuration={} ms)", filename, readDuration);
        } catch (Exception ignored) {}

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400, public")
                .header("X-Served-By", "local-storage")
                .header("X-Read-Duration-ms", String.valueOf(readDuration))
                .contentType(contentType)
                .body(resource);
    }

    // List all stored image filenames (or absolute URLs)
    @GetMapping("/images")
    public ResponseEntity<List<String>> listImages() {
        List<String> files = storageService.listAll();
        // Convert filenames to API URLs for convenience
        List<String> urls = files.stream()
                .map(name -> "/api/admin/products/images/" + name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(urls);
    }
}