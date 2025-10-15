package com.eduprajna.Controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduprajna.entity.Category;
import com.eduprajna.repository.CategoryRepository;

@RestController
@RequestMapping("/api/categories")
// Allow local dev, Vercel preview and production frontend domains
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "https://nishmitha-roots.vercel.app", "https://rootstraditional.in", "https://www.rootstraditional.in"}, allowCredentials = "true")
public class CategoryController {
    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> cats = categoryRepository.findAll();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(cats);
    }
}