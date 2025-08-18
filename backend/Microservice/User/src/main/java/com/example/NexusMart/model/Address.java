package com.example.NexusMart.model;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter//Lombok 提供的注解，自动生成所有字段的 getter 和 setter 方法（不用手动写 getStreet()、setCity() 等方法，简化代码）。
@Setter
@Entity//告诉 Spring Data JPA “这是一个数据库实体类”，对应数据库中的一张表（相当于给这个类贴了 “数据库表映射” 的标签）。
@Table(name = "addresses")//指定这个实体对应的数据库表名是 addresses（如果不指定，默认表名是类名 address）。
public class Address extends BaseEntity {//BaseEntity 是一个基础实体类（通常包含所有表共有的字段，比如 createdAt（创建时间）、updatedAt（更新时间））。
    @Id//标记这个字段是表的主键（唯一标识一条地址记录，相当于快递系统中每个地址的 “编号”）。
    @GeneratedValue(strategy = GenerationType.IDENTITY)//指定主键生成策略为 “自增”
    private Long id;

    @ManyToOne//表示 “多对一” 关系
    @JoinColumn(name = "userId", nullable = false)//指定数据库表中关联用户的外键列名为 userId（addresses 表通过 userId 字段和 users 表关联，相当于每个地址都记录 “属于哪个用户”）。
    //这个外键字段不能为空（每个地址必须属于一个用户，不能存在 “无主” 的地址）。
    private User user;

    private String street;

    private String city;

    private String state;

    @Column(name = "postal_code")//数据库中该字段的列名为 postal_code（下划线命名，符合数据库命名习惯，而 Java 中用驼峰 postalCode）。
    private String postalCode;

    private String country;

    @Version//标记这是乐观锁的版本号字段，用于解决并发更新冲突（比如用户同时修改同一个地址，系统会通过版本号判断是否有冲突）。
    //原理：每次更新地址时，版本号会自动加 1。如果两个操作同时读取到版本号 1，其中一个先更新成 2，另一个再更新时发现版本号已变，就会失败（避免覆盖别人的修改）。
    @Column(name = "version")
    private Integer version;
}
