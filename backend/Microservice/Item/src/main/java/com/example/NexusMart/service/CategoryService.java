package com.example.NexusMart.service;
import com.example.NexusMart.config.DataSourceType;
import com.example.NexusMart.config.ReplicationRoutingDataSourceContext;
import com.example.NexusMart.model.MainCategory;
import com.example.NexusMart.model.SubCategory;
import com.example.NexusMart.repository.MainCategoryRepository;
import com.example.NexusMart.repository.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public List<MainCategory> getAllMainCategories() {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            return mainCategoryRepository.findAll();
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional(readOnly = true)
    public List<SubCategory> getSubCategoriesByMainCategoryId(Long mainCategoryId) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            return subCategoryRepository.findByMainCategoryId(mainCategoryId);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }
}
