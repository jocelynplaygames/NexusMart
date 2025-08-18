package com.example.NexusMart.controller;

import com.example.NexusMart.dto.MainCategoryDTO;
import com.example.NexusMart.dto.SubCategoryDTO;
import com.example.NexusMart.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    

    @GetMapping("/main-categories")
    public ResponseEntity<List<MainCategoryDTO>> getAllMainCategories() {
        List<MainCategoryDTO> mainCategories = adminService.getAllMainCategories();
        return ResponseEntity.ok(mainCategories);
    }

    @GetMapping("/main-categories/{id}")
    public ResponseEntity<MainCategoryDTO> getMainCategoryById(@PathVariable Long id) {
        MainCategoryDTO mainCategory = adminService.getMainCategoryById(id);
        return ResponseEntity.ok(mainCategory);
    }

    @PostMapping("/main-categories")
    public ResponseEntity<MainCategoryDTO> createMainCategory(@RequestBody MainCategoryDTO mainCategoryDTO) {
        MainCategoryDTO createdMainCategory = adminService.createMainCategory(mainCategoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMainCategory);
    }

    @PutMapping("/main-categories/{id}")
    public ResponseEntity<MainCategoryDTO> updateMainCategory(@PathVariable Long id, @RequestBody MainCategoryDTO mainCategoryDTO) {
        MainCategoryDTO updatedMainCategory = adminService.updateMainCategory(id, mainCategoryDTO);
        return ResponseEntity.ok(updatedMainCategory);
    }

    @DeleteMapping("/main-categories/{id}")
    public ResponseEntity<Void> deleteMainCategory(@PathVariable Long id) {
        adminService.deleteMainCategory(id);
        return ResponseEntity.noContent().build();
    }

    
    @GetMapping("/sub-categories")
    public ResponseEntity<List<SubCategoryDTO>> getAllSubCategories() {
        List<SubCategoryDTO> subCategories = adminService.getAllSubCategories();
        return ResponseEntity.ok(subCategories);
    }

    @GetMapping("/sub-categories/{id}")
    public ResponseEntity<SubCategoryDTO> getSubCategoryById(@PathVariable Long id) {
        SubCategoryDTO subCategory = adminService.getSubCategoryById(id);
        return ResponseEntity.ok(subCategory);
    }

    @PostMapping("/sub-categories")
    public ResponseEntity<SubCategoryDTO> createSubCategory(
            @RequestPart("subCategory") String subCategoryJson,
            @RequestPart("imageFile") MultipartFile imageFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        SubCategoryDTO subCategoryDTO = objectMapper.readValue(subCategoryJson, SubCategoryDTO.class);
        SubCategoryDTO createdSubCategory = adminService.createSubCategory(subCategoryDTO, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubCategory);
    }

    @PutMapping("/sub-categories/{id}")
    public ResponseEntity<SubCategoryDTO> updateSubCategory(
            @PathVariable Long id,
            @RequestPart("subCategory") String subCategoryJson,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        SubCategoryDTO subCategoryDTO = objectMapper.readValue(subCategoryJson, SubCategoryDTO.class);
        SubCategoryDTO updatedSubCategory = adminService.updateSubCategory(id, subCategoryDTO, imageFile);
        return ResponseEntity.ok(updatedSubCategory);
    }

    @DeleteMapping("/sub-categories/{id}")
    public ResponseEntity<Void> deleteSubCategory(@PathVariable Long id) {
        adminService.deleteSubCategory(id);
        return ResponseEntity.noContent().build();
    }
}
