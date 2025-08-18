// File: backend/NexusMart/src/main/java/com/example/NexusMart/repository/RatingRepository.java
package com.example.NexusMart.repository;
import com.example.NexusMart.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByEntityIdAndEntityType(String entityId, Rating.EntityType entityType);
}
