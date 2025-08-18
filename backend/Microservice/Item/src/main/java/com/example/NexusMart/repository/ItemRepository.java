package com.example.NexusMart.repository;
import com.example.NexusMart.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    List<Item> findBySellerId(String userId);

    @Query("SELECT i FROM Item i WHERE i.id = :id AND i.deleted = false")
    Optional<Item> findActiveItemById(@Param("id") Long id);

    Optional<Object> findByIdAndSellerId(Long listingId, String userId);

    List<Item> findAllByDeletedFalse();

    List<Item> findBySellerIdAndDeletedFalse(String sellerId);

    @Query(value = "SELECT * FROM items WHERE deleted = false ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<Item> findRandomItems(@Param("count") int count);
}
