// File: backend/NexusMart/src/main/java/com/example/NexusMart/repository/MainCategoryRepository.java
package com.example.NexusMart.repository;
import com.example.NexusMart.model.MainCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MainCategoryRepository extends JpaRepository<MainCategory, Long> {
}