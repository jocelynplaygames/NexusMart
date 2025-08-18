// File: backend/NexusMart/src/main/java/com/example/NexusMart/service/AdminService.java
package com.example.NexusMart.service;

import com.example.NexusMart.dto.MainCategoryDTO;
import com.example.NexusMart.dto.SubCategoryDTO;
import com.example.NexusMart.exception.ResourceNotFoundException;
import com.example.NexusMart.mapper.MainCategoryMapper;
import com.example.NexusMart.mapper.SubCategoryMapper;
import com.example.NexusMart.model.MainCategory;
import com.example.NexusMart.model.SubCategory;
import com.example.NexusMart.repository.MainCategoryRepository;
import com.example.NexusMart.repository.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final MainCategoryRepository mainCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final CloudinaryService cloudinaryService;

    @Value("${cloudinary.category-upload-folder}")
    private String imageFolder;

    @Autowired
    public AdminService(MainCategoryRepository mainCategoryRepository,
                        SubCategoryRepository subCategoryRepository,
                        CloudinaryService cloudinaryService) {
        this.mainCategoryRepository = mainCategoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // MainCategory methods

    public List<MainCategoryDTO> getAllMainCategories() {
        List<MainCategory> mainCategories = mainCategoryRepository.findAll();
        return mainCategories.stream().map(MainCategoryMapper::toDTO).collect(Collectors.toList());
    }

    public MainCategoryDTO getMainCategoryById(Long id) {
        MainCategory mainCategory = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MainCategory", "id", id.toString()));
        return MainCategoryMapper.toDTO(mainCategory);
    }

    public MainCategoryDTO createMainCategory(MainCategoryDTO mainCategoryDTO) {
        MainCategory mainCategory = MainCategoryMapper.toEntity(mainCategoryDTO);
        MainCategory savedMainCategory = mainCategoryRepository.save(mainCategory);
        return MainCategoryMapper.toDTO(savedMainCategory);
    }

    public MainCategoryDTO updateMainCategory(Long id, MainCategoryDTO mainCategoryDTO) {
        MainCategory existingMainCategory = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MainCategory", "id", id.toString()));
        MainCategoryMapper.updateEntityFromDTO(mainCategoryDTO, existingMainCategory);
        MainCategory updatedMainCategory = mainCategoryRepository.save(existingMainCategory);
        return MainCategoryMapper.toDTO(updatedMainCategory);
    }

    public void deleteMainCategory(Long id) {
        MainCategory existingMainCategory = mainCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MainCategory", "id", id.toString()));
        mainCategoryRepository.delete(existingMainCategory);
    }

    // SubCategory methods

    public List<SubCategoryDTO> getAllSubCategories() {
        List<SubCategory> subCategories = subCategoryRepository.findAll();
        return subCategories.stream().map(SubCategoryMapper::toDTO).collect(Collectors.toList());
    }

    public SubCategoryDTO getSubCategoryById(Long id) {
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", id.toString()));
        return SubCategoryMapper.toDTO(subCategory);
    }

    public SubCategoryDTO createSubCategory(SubCategoryDTO subCategoryDTO, MultipartFile imageFile) throws IOException {
        MainCategory mainCategory = mainCategoryRepository.findById(subCategoryDTO.getMainCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("MainCategory", "id", subCategoryDTO.getMainCategoryId().toString()));

        SubCategory subCategory = SubCategoryMapper.toEntity(subCategoryDTO);
        subCategory.setMainCategory(mainCategory);

        if (imageFile != null && !imageFile.isEmpty()) {
            Map<String, Object> uploadResult = cloudinaryService.uploadFile(imageFile.getBytes(), imageFolder);
            subCategory.setImageUrl((String) uploadResult.get("url"));
        }

        SubCategory savedSubCategory = subCategoryRepository.save(subCategory);
        return SubCategoryMapper.toDTO(savedSubCategory);
    }

    public SubCategoryDTO updateSubCategory(Long id, SubCategoryDTO subCategoryDTO, MultipartFile imageFile) throws IOException {
        SubCategory existingSubCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", id.toString()));

        MainCategory mainCategory = mainCategoryRepository.findById(subCategoryDTO.getMainCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("MainCategory", "id", subCategoryDTO.getMainCategoryId().toString()));

        SubCategoryMapper.updateEntityFromDTO(subCategoryDTO, existingSubCategory);
        existingSubCategory.setMainCategory(mainCategory);

        if (imageFile != null && !imageFile.isEmpty()) {
            if (existingSubCategory.getImageUrl() != null && !existingSubCategory.getImageUrl().isEmpty()) {
                String publicId = cloudinaryService.extractPublicIdFromUrl(existingSubCategory.getImageUrl());
                cloudinaryService.deleteFile(publicId, imageFolder);
            }
            Map<String, Object> uploadResult = cloudinaryService.uploadFile(imageFile.getBytes(), imageFolder);
            existingSubCategory.setImageUrl((String) uploadResult.get("url"));
        }

        // Ensure that the imageUrl is only updated if it's not null in subCategoryDTO
        if (subCategoryDTO.getImageUrl() != null) {
            existingSubCategory.setImageUrl(subCategoryDTO.getImageUrl());
        }

        SubCategory updatedSubCategory = subCategoryRepository.save(existingSubCategory);
        return SubCategoryMapper.toDTO(updatedSubCategory);
    }

    public void deleteSubCategory(Long id) {
        SubCategory existingSubCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", id.toString()));
        subCategoryRepository.delete(existingSubCategory);
    }
}

