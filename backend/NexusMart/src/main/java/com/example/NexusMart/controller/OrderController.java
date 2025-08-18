package com.example.NexusMart.controller;

import com.example.NexusMart.dto.CreateOrderRequestDTO;
import com.example.NexusMart.dto.OrderDTO;
import com.example.NexusMart.exception.ResourceNotFoundException;
import com.example.NexusMart.model.ShippingMethod;
import com.example.NexusMart.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
//✨告诉 Spring 这是一个处理 HTTP 请求的控制器，返回数据会直接作为 JSON 响应
@RequestMapping("/api/orders")
//✨所有该控制器的接口都以/api/orders开头
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

//✨用户在结算页面点击 “选择配送方式”，前端端需要显示可选的快递递方式,前端 JavaScript 会发送一个 GET 请求
//前端发送请求：GET /api/orders/shipping-methods
//这个请求的 URL 是/api/orders/shipping-methods，对应后端OrderController的getShippingMethods方法。
//控制器接收到请求，调用orderService.getShippingMethods()
    @GetMapping("/shipping-methods")
    //✨精确匹配请求路径，当收到GET /api/orders/shipping-methods请求时，触发getShippingMethods方法
    public ResponseEntity<List<ShippingMethod>> getShippingMethods() {//🎆控制层
        //调用orderService.getShippingMethods()获取数据，通过ResponseEntity.ok(...)返回 200 状态码和配送方式列表。
        List<ShippingMethod> shippingMethods = orderService.getShippingMethods();
        //调用orderService文件中的getShippingMethods()获取数据，让🎆服务处去接触数据库：shippingMethodsRepository.findAll()
        return ResponseEntity.ok(shippingMethods);
        //创建一个 HTTP 状态码为200 OK的响应。
        //响应体中包含shippingMethods列表，Spring 会自动将其转换为 JSON 格式
    }

//✨用户点击 “查看订单详情”，前端 JavaScript 会发送一个 GET 请求：/api/orders/100
//控制器通过@PathVariable获取orderId=100，调用orderService.getOrder(100)
    @GetMapping("/{orderId}")//处理 GET 请求，路径为基础路径 + /{orderId}。
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderId) {//@ 将路径中的orderId参数转换为 Long 类型传入方法。
        OrderDTO orderDTO = orderService.getOrder(orderId) //调用订单服务的getOrder方法，根据orderId查询订单详情。
// 返回值：Optional<OrderDTO>（Optional包装的订单数据传输对象） 
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId.toString()));
                //.orElseThrow(...)这是Optional类的核心方法
        return ResponseEntity.ok(orderDTO);
    }

//✨用户点击 “我的订单”，前端 JavaScript 会发送一个 GET 请求：/api/orders/user/123
//控制器获取userId=123，调用orderService.getOrdersByUser(123)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable String userId) {
        List<OrderDTO> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }

//✨用户在结算页面点击 “提交订单”，系统需要生成新订单，前端 JavaScript 会发送一个 POST 请求：/api/orders
//请求体包含{cartId:5, shippingMethodId:1, cardId:3}
//控制器通过@RequestBody解析请求参数，调用orderService.createOrder(5,1,3)（“后台，用购物车 5、快递 1、支付卡 3 创建新订单”）。
    @PostMapping//处理 POST 请求，路径为/api/orders
    public ResponseEntity<Long> createOrder(@RequestBody CreateOrderRequestDTO request) {// CreateOrderRequestDTO.java定义了订单的结构
        //@ 将请求体（JSON 格式）转换为CreateOrderRequestDTO对象，包含创建订单所需的参数（购物车 ID、配送方式 ID、支付卡片 ID）
        //不自己干活，交给orderService的createOrder方法
        Long orderId = orderService.createOrder(request.getCartId(), request.getShippingMethodId(), request.getCardId());
        //调用orderService.createOrder(...)创建订单，返回订单 ID。
        //getShippingMethodById(request.getShippingMethodId())获取配送方式信息，并返回 200 状态码和订单 ID。
        return ResponseEntity.ok(orderId);
    }
}

