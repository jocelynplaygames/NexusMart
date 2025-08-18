package com.example.NexusMart.dto;
import lombok.Data;
//数据库中可能存储了很多字段（如创建时间、修改时间等审计字段）。
//但前端展示配送方式时，只需要 id、name、price 等核心信息。
//ShippingMethodDTO 就定义了这个 “包裹” 的格式，只包含需要传输的字段。
@Data
//Lombok 框架的注解，编译时自动生成所有字段的 getter、setter、toString 等方法
public class ShippingMethodDTO {
    //对应前端需要的信息
    private Long id;
    private String name;
    private String description;
    private double price;
    private String estimatedDeliveryTime;
}
