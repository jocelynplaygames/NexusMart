package com.example.NexusMart.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;// Lombok 框架注解，自动生成 getter 和 setter 方法
import lombok.Setter;

@Getter//编译时自动为类中所有字段生成 getter（获取值）和 setter（设置值）方法
@Setter
@Entity//JPA 注解，标记该类是实体类，会与数据库中的表建立映射关系（ORM 映射）
@Table(name = "shippingMethods")
//JPA 注解，指定该实体对应的数据库表名是shippingMethods（如果不指定，默认表名是类名小写shippingmethod）
public class ShippingMethod extends BaseEntity {
    //继承BaseEntity基类（通常包含所有实体共有的字段，如createdAt、updatedAt、createdBy等审计字段）

    @Id
    //JPA 注解，标记该字段是主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //配置主键生成策略：IDENTITY表示主键由数据库自动生成（自增主键），插入数据时无需手动设置id
    //作用就是指定实体类中 id 字段的生成策略
    private Long id;

    @Column(nullable = false)
    //映射数据库列的约束：nullable = false表示该字段不允许为 null
    private String name;

    @Lob
    //Lob：标记该字段是大文本类型（对应数据库的TEXT或CLOB类型）
    private String description;

    @Column(nullable = false)
    private double price;

    private String estimatedDeliveryTime;
}

// ShippingMethodSelection.jsx 和 ShippingMethod.java 的关系：
// ShippingMethod.java: 后端数据模型，定义配送方式的数据结构和业务规则
// ShippingMethodSelection.jsx: 前端用户界面组件，提供配送方式选择功能
// 两者通过API进行数据交互