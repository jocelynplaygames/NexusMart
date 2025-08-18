// File: backend/NexusMart/src/main/java/com/example/NexusMart/service/CategoryService.java
package com.example.NexusMart.service;
import com.example.NexusMart.model.MainCategory;
import com.example.NexusMart.model.SubCategory;
import com.example.NexusMart.repository.MainCategoryRepository;
import com.example.NexusMart.repository.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    private final MainCategoryRepository mainCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    @Autowired
    public CategoryService(MainCategoryRepository mainCategoryRepository, SubCategoryRepository subCategoryRepository) {
        this.mainCategoryRepository = mainCategoryRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    public List<MainCategory> getAllMainCategories() {
        return mainCategoryRepository.findAll();
    }

    public List<SubCategory> getSubCategoriesByMainCategoryId(Long mainCategoryId) {
        return subCategoryRepository.findByMainCategoryId(mainCategoryId);
    }
}
