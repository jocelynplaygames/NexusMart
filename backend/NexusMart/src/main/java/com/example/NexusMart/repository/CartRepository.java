// File: backend/NexusMart/src/main/java/com/example/NexusMart/repository/CartRepository.java
package com.example.NexusMart.repository;
import com.example.NexusMart.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    //第一个参数 Cart表示当前仓库接口（CartRepository）操作的是 Cart 实体类（即购物车实体）。
    //第二个参数 Long 表示 Cart 实体类的主键（id 字段）的数据类型是 Long。
    Optional<Object> findByUserId(String userId);
}
