// File: backend/NexusMart/src/main/java/com/example/NexusMart/model/CartItem.java
package com.example.NexusMart.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cart_items")
public class CartItem extends BaseEntity {
    //（1）购物项 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //（2）关联购物车（Cart）
    @ManyToOne//多个购物项可以属于同一个购物车
    @JoinColumn(name = "cartId", nullable = false)
    //指定数据库表中关联购物车的外键字段名为cartId（cart_items表中的cart_id字段）
    private Cart cart;

    //（3）关联商品（Item）
    @ManyToOne//多个购物项可以关联同一个商品
    //（例如：用户 A 的购物车和用户 B 的购物车都有 “手机” 这个商品，对应两个不同的购物项，但都关联到同一个Item）
    @JoinColumn(name = "itemId", nullable = false)
    // 指定数据库表中关联商品的外键字段名为itemId（cart_items表中的item_id字段）
    private Item item;

    //（4）商品购买数量
    @Column(nullable = false)
    private int quantity;
}//通过cart和item两个外键，将 “用户的购物车” 与 “具体商品” 连接起来，明确 “哪个用户的购物车中有哪个商品”。

// CartItem实体对应数据库中的cart_items表，表结构
// 字段名	        类型
// id	        BIGINT	主键（自增）
// cart_id	    BIGINT	外键，关联购物车表（carts）
// item_id	    BIGINT	外键，关联商品表（items）
// quantity	INT	购买数量
// created_at	DATETIME	创建时间（继承自 BaseEntity）
// updated_at	DATETIME	更新时间（继承自 BaseEntity）

