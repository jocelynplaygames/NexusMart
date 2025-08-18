package com.example.NexusMart.mapper;

import com.example.NexusMart.dto.MainCategoryDTO;
import com.example.NexusMart.model.MainCategory;

import java.sql.Timestamp;

public class MainCategoryMapper {
    public static MainCategoryDTO toDTO(MainCategory mainCategory) {
        MainCategoryDTO mainCategoryDTO = new MainCategoryDTO();
        mainCategoryDTO.setId(mainCategory.getId());
        mainCategoryDTO.setName(mainCategory.getName());
        mainCategoryDTO.setCreatedAt(Timestamp.valueOf(mainCategory.getCreatedAt()));
        mainCategoryDTO.setUpdatedAt(Timestamp.valueOf(mainCategory.getUpdatedAt()));
        return mainCategoryDTO;
    }

    public static MainCategory toEntity(MainCategoryDTO mainCategoryDTO) {
        MainCategory mainCategory = new MainCategory();
        mainCategory.setName(mainCategoryDTO.getName());
        return mainCategory;
    }

    public static void updateEntityFromDTO(MainCategoryDTO mainCategoryDTO, MainCategory mainCategory) {
        mainCategory.setName(mainCategoryDTO.getName());
    }
}
