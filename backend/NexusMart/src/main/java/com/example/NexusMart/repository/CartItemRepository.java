// File: backend/NexusMart/src/main/java/com/example/NexusMart/repository/CartItemRepository.java
package com.example.NexusMart.repository;
import com.example.NexusMart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}

