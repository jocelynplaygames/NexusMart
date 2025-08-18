package com.example.NexusMart.repository;
import com.example.NexusMart.model.Feedback;
import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByItemId(Long itemId);
    List<Feedback> findByUserId(String userId);
    boolean existsByItemIdAndUserId(Long itemId, String userId);

    @Query("SELECT f FROM Feedback f WHERE f.sellerId = :sellerId")
    List<Feedback> findBySellerId(@Param("sellerId") String sellerId);
}

