package com.example.NexusMart.service;

import com.example.NexusMart.config.DataSourceType;
import com.example.NexusMart.config.ReplicationRoutingDataSourceContext;
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
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional(readOnly = true)
    public List<MainCategoryDTO> getAllMainCategories() {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            List<MainCategory> mainCategories = mainCategoryRepository.findAll();
            return mainCategories.stream().map(MainCategoryMapper::toDTO).collect(Collectors.toList());
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional(readOnly = true)
    public MainCategoryDTO getMainCategoryById(Long id) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            MainCategory mainCategory = mainCategoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("MainCategory", "id", id.toString()));
            return MainCategoryMapper.toDTO(mainCategory);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional
    public MainCategoryDTO createMainCategory(MainCategoryDTO mainCategoryDTO) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            MainCategory mainCategory = MainCategoryMapper.toEntity(mainCategoryDTO);
            MainCategory savedMainCategory = mainCategoryRepository.save(mainCategory);
            return MainCategoryMapper.toDTO(savedMainCategory);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional
    public MainCategoryDTO updateMainCategory(Long id, MainCategoryDTO mainCategoryDTO) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            MainCategory existingMainCategory = mainCategoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("MainCategory", "id", id.toString()));
            MainCategoryMapper.updateEntityFromDTO(mainCategoryDTO, existingMainCategory);
            MainCategory updatedMainCategory = mainCategoryRepository.save(existingMainCategory);
            return MainCategoryMapper.toDTO(updatedMainCategory);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional
    public void deleteMainCategory(Long id) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            MainCategory existingMainCategory = mainCategoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("MainCategory", "id", id.toString()));
            mainCategoryRepository.delete(existingMainCategory);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    // SubCategory methods

    @Transactional(readOnly = true)
    public List<SubCategoryDTO> getAllSubCategories() {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            List<SubCategory> subCategories = subCategoryRepository.findAll();
            return subCategories.stream().map(SubCategoryMapper::toDTO).collect(Collectors.toList());
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional(readOnly = true)
    public SubCategoryDTO getSubCategoryById(Long id) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            SubCategory subCategory = subCategoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", id.toString()));
            return SubCategoryMapper.toDTO(subCategory);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional
    public SubCategoryDTO createSubCategory(SubCategoryDTO subCategoryDTO, MultipartFile imageFile) throws IOException {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
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
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional
    public SubCategoryDTO updateSubCategory(Long id, SubCategoryDTO subCategoryDTO, MultipartFile imageFile) throws IOException {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
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

            SubCategory updatedSubCategory = subCategoryRepository.save(existingSubCategory);
            return SubCategoryMapper.toDTO(updatedSubCategory);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional
    public void deleteSubCategory(Long id) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            SubCategory existingSubCategory = subCategoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", id.toString()));
            subCategoryRepository.delete(existingSubCategory);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }
}

