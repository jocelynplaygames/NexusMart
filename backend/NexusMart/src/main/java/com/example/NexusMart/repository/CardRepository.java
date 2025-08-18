// File: backend/NexusMart/src/main/java/com/example/NexusMart/repository/CardRepository.java
package com.example.NexusMart.repository;
import com.example.NexusMart.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUserId(String userId);
}
