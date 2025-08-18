// File: backend/NexusMart/src/main/java/com/example/NexusMart/dto/CreateOrderRequestDTO.java
package com.example.NexusMart.dto;

import lombok.Data;
//当用户在前端点击 “提交订单” 时，需要告诉后端三个关键信息
// “从哪个购物车取商品”“用哪种配送方式”“用哪张卡支付”。
// 前端将这三个参数打包成 JSON，通过 POST 请求发送给后端
// 后端控制器orderController.java通过@RequestBody注解，将 JSON 自动转换为CreateOrderRequestDTO对象
@Data
//Lombok 框架的注解，编译时自动生成所有字段的 getter、setter、toString 等方法
public class CreateOrderRequestDTO {
    private Long cartId;
    private Long shippingMethodId;
    private Long cardId;
}
