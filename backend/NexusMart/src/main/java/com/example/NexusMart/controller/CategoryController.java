// File: backend/NexusMart/src/main/java/com/example/NexusMart/controller/CategoryController.java
package com.example.NexusMart.controller;

import com.example.NexusMart.model.MainCategory;
import com.example.NexusMart.model.SubCategory;
import com.example.NexusMart.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/main-categories")
    public ResponseEntity<List<MainCategory>> getAllMainCategories() {
        List<MainCategory> mainCategories = categoryService.getAllMainCategories();
        return ResponseEntity.ok(mainCategories);
    }

    @GetMapping("/main-categories/{mainCategoryId}/sub-categories")
    public ResponseEntity<List<SubCategory>> getSubCategoriesByMainCategoryId(@PathVariable Long mainCategoryId) {
        List<SubCategory> subCategories = categoryService.getSubCategoriesByMainCategoryId(mainCategoryId);
        return ResponseEntity.ok(subCategories);
    }
}
