package com.example.NexusMart.model;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {
    @Id//标记这个字段是表的主键（唯一标识一条地址记录，相当于快递系统中每个地址的 “编号”）。
    @GeneratedValue(strategy = GenerationType.IDENTITY)//指定主键生成策略为 “自增”
    private Long id;

    @ManyToOne//表示 “多对一” 关系
    @JoinColumn(name = "userId", nullable = false)
    //这个外键字段不能为空（每个地址必须属于一个用户，不能存在 “无主” 的地址）。
    private User user;

    private String street;

    private String city;

    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    private String country;

    @Version//标记这是乐观锁的版本号字段，用于解决并发更新冲突（比如用户同时修改同一个地址，系统会通过版本号判断是否有冲突）。
    
    @Column(name = "version")
    private Integer version;
}
