// File: backend/NexusMart/src/main/java/com/example/NexusMart/model/Cart.java
package com.example.NexusMart.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity//标记这是 JPA 实体类，对应数据库中的一张表
@Table(name = "carts")//明确指定对应的数据库表名为carts（购物车表）
public class Cart extends BaseEntity {//继承基础实体类（通常包含createdAt、updatedAt等公共字段）
    @Id//主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne// “多对一” 关系 —— 多个购物车可以属于同一个用户
    @JoinColumn(name = "userId", nullable = false)
    private User user;//将购物车与用户绑定（如 “ID=100 的购物车属于 ID=123 的用户”）
    //作为用户与购物项之间的 “桥梁”，通过user字段关联用户，通过cartItems字段包含该用户添加的所有商品。

    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    //购物车（Cart）和购物项（CartItem）是 “一对多” 关系（1 个购物车包含多个购物项）
    //CartItem的表中会有一个cart_id字段，指向它所属的购物车 ID
    private List<CartItem> cartItems = new ArrayList<>();
    //初始化：new ArrayList<>()确保cartItems永远不为null、\
    //确保无论何时调用cart.getCartItems()，都能得到一个列表（即使没有购物项，也是空列表），而不是null

//mappedBy = "cart"：表示关联关系由CartItem类中的cart字段维护（即CartItem表中通过cart_id外键关联到carts表）。
//fetch = FetchType.LAZY：懒加载策略 —— 查询购物车时，不会自动加载其包含的购物项，只有调用getCartItems()时才会查询
//cascade = CascadeType.ALL：级联操作 —— 对购物车的操作（如保存、删除）会自动应用到其包含的购物项
//orphanRemoval = true：孤儿删除 —— 当某个购物项从cartItems列表中移除时，该购物项会被自动从数据库中删除
//避免数据库中出现 “无主” 的购物项（不属于任何购物车的购物项）
}

// carts表，表结构
// 字段名	类型
// id	        BIGINT	主键（自增）
// user_id	    BIGINT	外键，关联用户表（users）
// created_at	DATETIME	创建时间（继承自 BaseEntity）
// updated_at	DATETIME	更新时间（继承自 BaseEntity）


// 1. 数据获取流程
// 前端 Cart.jsx 
//     ↓ 调用API
// 后端 CartController 
//     ↓ 调用服务
// CartService 
//     ↓ 调用仓库
// CartRepository 
//     ↓ 查询数据库
// Cart.java 实体映射的 carts 表

// 2. 数据更新流程
// 用户在前端 Cart.jsx 修改数量
//     ↓ 触发事件
// 调用 CartService.js 的 updateCartItem API
//     ↓ 发送到后端
// CartController 接收请求
//     ↓ 调用服务
// CartService 更新 Cart 实体
//     ↓ 保存到数据库
// Cart.java 对应的 carts 表更新