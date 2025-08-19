package com.example.NexusMart.mapper;

import com.example.NexusMart.dto.OrderDTO;
import com.example.NexusMart.dto.OrderItemDTO;
import com.example.NexusMart.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

//一个 Order（订单实体）包含orderItems,它包含多个 orderItem（订单项实体），每个 orderItem 关联一个 Item（商品实体）；

//一个 orderDTO（订单 DTO）包含orderItems（DTO 列表 List<OrderItemDTO>），它包含多个 orderItemDTO（订单项 DTO），
//每个 orderItemDTO 包含商品的核心信息（如 itemId(用newItem.setId(orderItemDTO.getItemId())设置获取)

@Component
public class OrderMapper {

    //将数据库实体 Order 转换为前端需要的 OrderDTO，只保留前端所需的字段，隐藏数据库细节
    public OrderDTO toDTO(Order order) {
        //orderDTO包括了一个列表属性orderItems，每个item可能来自不同的Item实体，所以需要遍历转换
        //先转换其他属性字段
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());//设置主键,从 DTO 中获取订单 ID 并设置到实体（可能是新增订单的 ID，或更新已有订单）
        orderDTO.setUserId(order.getUser().getId());
        orderDTO.setUserName(order.getUser().getUsername());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setStatus(order.getStatus());
        orderDTO.setCreatedAt(order.getCreatedAt());
        orderDTO.setUpdatedAt(order.getUpdatedAt());
        orderDTO.setCardType(order.getCardType());
        orderDTO.setLastFourDigit(order.getLastFourDigit());//卡号后4位（安全脱敏）
        orderDTO.setShippingMethodId(order.getShippingMethod().getId());
        orderDTO.setShippingMethodName(order.getShippingMethod().getName());
        orderDTO.setShippingCost(order.getShippingMethod().getPrice()); // Include shipping cost

        // 再转换订单项列表：将OrderItem实体列表转为OrderItemDTO列表
        List<OrderItemDTO> orderItems = order.getOrderItems().stream()//转换为流（Stream），以便进行批量处理
                .map(this::toOrderItemDTO)// 对流中的每个OrderItem实体，调用toOrderItemDTO方法转换为OrderItemDTO
                .collect(Collectors.toList());//将转换后的流收集为List<OrderItemDTO>
        orderDTO.setOrderItems(orderItems);//将转换后的订单项DTO列表设置到OrderDTO中

        return orderDTO;
    }

    private OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setItemId(orderItem.getItem().getId());
        orderItemDTO.setQuantity(orderItem.getQuantity());
        orderItemDTO.setPrice(orderItem.getPrice());
        orderItemDTO.setItemName(orderItem.getItem().getTitle());
        return orderItemDTO;
    }

//将前端传递的 OrderDTO 转换为数据库实体 Order，结合关联对象（用户、配送方式、支付卡）完成实体构建，用于后续持久化到数据库。
    public Order toEntity(OrderDTO orderDTO, User user, ShippingMethod shippingMethod, Card card) {
        Order order = new Order();
        order.setId(orderDTO.getId());
        order.setUser(user);//设置外键,将用户实体设置到订单实体，ORM 会自动提取 user 的 id 作为外键 user_id
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setStatus(orderDTO.getStatus());
        order.setCardType(card.getType());
        order.setLastFourDigit(card.getCardNumber().substring(card.getCardNumber().length() - 4));
        order.setShippingMethod(shippingMethod);//设置外键,将配送方式实体设置到订单实体，ORM 会自动提取 shippingMethod 的 id 作为外键 shipping_method_id

        // 转换订单项List<OrderItemDTO> orderItems(是包含DTO的列表)为实体列表orderItems。每个订单orderItems有多个orderItem,每个订单项orderItem关联商品Item
        List<OrderItem> orderItems = orderDTO.getOrderItems().stream()
                .map(orderItemDTO -> {
                    OrderItem orderItem = new OrderItem();// 针对每个OrderItemDTO，创建对应的OrderItem实体
                    orderItem.setOrder(order);//设置外键，关联当前订单：在内存中为 OrderItem 绑定它所属的 Order 对象，
                    // ORM 框架（如 JPA）会在保存 OrderItem 到数据库时，
                    // 自动将 order 的 id 作为外键（如 order_id 字段）存入 order_item 表中。
                    // 不是主键（Primary Key）：是每个实体自身的唯一标识（如 OrderItem 的 id 字段），通常由数据库自动生成（如自增 ID）
                    Item newItem = new Item();//为每个 OrderItemDTO 中的 简化版本的itemId 创建一个对应的 Item 实体对象
                    // newItem 临时设置，仅用于在订单项（OrderItem）和商品（Item）之间建立关联关系。只用用于获取Item的id
                    // 因为 OrderItemDTO 中只包含前端传递的 itemId（商品 ID），所以 newItem 只需通过 newItem.setId(orderItemDTO.getItemId()) 设置 id 即可
                    newItem.setId(orderItemDTO.getItemId());//设置主键,从 DTO 中获取商品 ID为订单项关联的商品设置主键
                    orderItem.setItem(newItem);//设置外键，将商品实体设置到订单项实体，ORM 会自动提取 item 的 id 作为外键 item_id
                    orderItem.setQuantity(orderItemDTO.getQuantity());
                    orderItem.setPrice(orderItemDTO.getPrice());
                    return orderItem;
                })
                .collect(Collectors.toList());
        order.setOrderItems(orderItems);//将新写好的orderItems实体列表设置到Order实体中

        return order;
    }
}
// “设置主键”：order.setId(...)、newItem.setId(...)
// 实体类中被@Id标记的字段，是数据库表的主键
// “设置外键”：order.setUser(user)、order.setShippingMethod(shippingMethod)
// 实体类中被@ManyToOne + @JoinColumn等注解标记的字段，是数据库表的外键