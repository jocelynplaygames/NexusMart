// File: backend/NexusMart/src/main/java/com/example/NexusMart/service/OrderService.java
package com.example.NexusMart.service;

import com.example.NexusMart.dto.OrderDTO;
import com.example.NexusMart.exception.ResourceNotFoundException;
import com.example.NexusMart.mapper.OrderMapper;
import com.example.NexusMart.model.*;
import com.example.NexusMart.repository.CardRepository;
import com.example.NexusMart.repository.CartRepository;
import com.example.NexusMart.repository.OrderRepository;
import com.example.NexusMart.repository.ShippingMethodRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service// 标记为Spring的服务层组件，由Spring管理
public class OrderService {

    // 依赖注入：Spring 会自动创建这些对象，并注入到 OrderService 中
    private final CartRepository cartRepository;
    private final ShippingMethodRepository shippingMethodsRepository;
    private final CardRepository cardRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;// 用于调用外部支付服务的工具
    private final OrderMapper orderMapper;// 用于实体与DTO转换的映射器

// 从配置文件读取支付服务的URL
    @Value("${payment.service.url}")
    private String paymentServiceUrl;

// 构造函数，用于注入依赖
//将所需的依赖对象传递进来并赋值给成员变量。
// 上面的代码中，OrderService的构造函数接收了 6 个参数（都是它的依赖），并通过this.xxx = xxx保存起来
    public OrderService(CartRepository cartRepository, ShippingMethodRepository shippingMethodsRepository,
                        CardRepository cardRepository, OrderRepository orderRepository, 
                        RestTemplate restTemplate, OrderMapper orderMapper) {
        this.cartRepository = cartRepository;
        this.shippingMethodsRepository = shippingMethodsRepository;
        this.cardRepository = cardRepository;
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
        this.orderMapper = orderMapper;
    }

    public List<ShippingMethod> getShippingMethods() {
        return shippingMethodsRepository.findAll();
        //从数据库查询ShippingMethod实体列表。本方法被OrderController.java调用
    }

    public Optional<OrderDTO> getOrder(Long orderId) {//根据订单 ID 
    //服务层通过仓库接口（OrderRepository）直接与数据库交互，无需关心底层 SQL 实现
        return orderRepository.findById(orderId).map(orderMapper::toDTO);
        //从数据库查询Order实体（可能不存在，返回Optional<Order>）
        //如果实体存在，通过OrderMapper将Order实体转换为OrderDTO
//.map(...)：Optional类的方法，用于 “转换值”        
//orderMapper::toDTO：方法引用（Java 8 语法），等价于(order) -> orderMapper.toDTO(order)。
//表示调用orderMapper的toDTO方法，将Order实体转换为OrderDTO（DTO 与实体的转换逻辑由OrderMapper实现）。
    }
// 1. orderRepository.findById(orderId)：直接操作数据库
// orderRepository是数据访问层的接口，findById(orderId)是它继承的数据库查询方法。
// 调用这个方法时，Spring Data JPA 会自动执行 SQL：SELECT * FROM orders WHERE id = ?（orders是Order实体对应的表）。
// 返回结果是Optional<Order>：如果数据库中存在 ID 为orderId的订单，Optional中包含Order实体（数据库记录的映射对象）；如果不存在，Optional为空。
// 2. .map(orderMapper::toDTO)：实体转 DTO
// 数据库查询返回的是Order实体（与数据库表结构强关联，可能包含敏感字段或冗余信息）。
// 通过orderMapper::toDTO（方法引用）将Order实体转换为OrderDTO（前端需要的精简数据格式）。
// 只有当Optional不为空时（即查询到订单），才会执行转换；如果为空，直接返回空Optional。

    public List<OrderDTO> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId).stream().map(orderMapper::toDTO).collect(Collectors.toList());
//findByUserId(userId)：Spring Data JPA 的自定义查询方法，查询用户 ID 为userId的所有订单，返回List<Order>（订单实体列表）
//.stream()：将List<Order>转换为Stream<Order>，以便使用Java 8的流式API
//.map(orderMapper::toDTO)：对每个元素（Order实体）转换为OrderDTO，得到一个Stream<OrderDTO>（DTO 流）
//collect：流的终止操作，将流中的元素收集为指定的集合。
//Collectors.toList()将Stream<OrderDTO>转换为List<OrderDTO>
    }


//🎆仓库接口orderRepository 操作的 orders 表，shippingMethodsRepository 操作的 shipping_methods 表，cardRepository 操作的 cards 表
//在 Spring Data JPA 中，仓库接口（Repository）操作的表，完全由它关联的实体类决定。
// 实体类名 → 表名;实体类名默认对应表名（通常转为小写或复数形式）。
// 当用户添加商品到购物车时，cartRepository 操作 carts 表，记录 “购物车中有哪些商品”。
// 当用户选择配送方式时，shippingMethodsRepository 操作 shipping_methods 表，查询 “顺丰、圆通等配送方式的信息”。
// 当用户选择支付卡时，cardRepository 操作 cards 表，查询 “用户绑定的银行卡信息”。
// 当用户提交订单时，orderRepository 操作 orders 表，保存 “这次订单的总金额、关联的配送方式和支付卡”。


    @Transactional// 标记为事务方法：所有操作要么全成功，要么全失败（如支付成功但订单保存失败时，回滚所有操作）
    public Long createOrder(Long cartId, Long shippingMethodId, Long cardId) {
        // 步骤1：验证依赖资源是否存在（购物车、配送方式、支付卡）

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId.toString()));
        ShippingMethod shippingMethod = shippingMethodsRepository.findById(shippingMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("ShippingMethod", "id", shippingMethodId.toString()));
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId.toString()));

        // 步骤2：计算订单总金额（商品总价 + 运费 + 税费）
        double itemsTotal = cart.getCartItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getItem().getPrice())
                .sum();
        //cart：当前用户的购物车对象（Cart实体）。getCartItems()：获取购物车中所有的 “购物项”（CartItem列表）
        //.mapToDouble(...)流的转换操作：将每个CartItem对象转换为一个double类型的数值
        //左侧 item：表示输入参数。箭头 ->：分隔参数和执行逻辑，读作 “映射为” 或 “执行以下操作”。右侧，对参数的处理逻辑
        double shippingCost = shippingMethod.getPrice();//从数据库shipping_methods表中获取运费，通过shippingMethod.getPrice()获取
        double tax = (itemsTotal + shippingCost) * 0.06;
        double totalAmount = itemsTotal + shippingCost + tax;

        // 步骤3：调用外部支付服务处理支付
        boolean paymentSuccessful = Boolean.TRUE.equals(//等价于更安全的true == 支付结果（但用equals更规范）
                restTemplate.postForObject(paymentServiceUrl, totalAmount, Boolean.class)
                //restTemplate：Spring 提供的 HTTP 客户端工具，用于发送 HTTP 请求（这里是 POST 请求）调用外部服务。
                //paymentServiceUrl：支付服务的接口地址（从配置文件读取，如https://pay-service.com/pay）。
                //totalAmount：发送给支付服务的请求体数据，即订单总金额（如299.99元）。
                //Boolean.class：指定支付服务返回的数据类型是Boolean（true/false）。
        );

        // 步骤4：支付成功则创建订单
        if (paymentSuccessful) {
            Order order = new Order();//🎆创建一个新的Order实体对象
            order.setUser(cart.getUser());//从购物车中获取下单用户
            order.setTotalAmount(totalAmount);
            order.setStatus("COMPLETED");
            order.setShippingMethod(shippingMethod);
            order.setCardType(card.getType());
            order.setLastFourDigit(card.getCardNumber().substring(card.getCardNumber().length() - 4));
            //substring(起始索引)：从指定索引开始截取字符串，直到末尾。

            // 4.2 将购物车中的商品项（CartItem）转换为订单中的商品项（OrderItem）
            List<OrderItem> orderItems = cart.getCartItems().stream()
                    .map(cartItem -> {//遍历购物车中的每个商品项，创建对应的订单项，复制关键信息（商品、数量、单价），并绑定到当前订单
                        OrderItem orderItem = new OrderItem();//🎆创建新的订单项对象
                        orderItem.setOrder(order);//🎆关联当前订单（重要：订单项必须属于某个订单）
                        orderItem.setItem(cartItem.getItem());
                        orderItem.setQuantity(cartItem.getQuantity());
                        orderItem.setPrice(cartItem.getItem().getPrice());
                        return orderItem;
                    })
                    .collect(Collectors.toList());
            order.setOrderItems(orderItems);

//🎆订单项（OrderItem）代表 “订单中的一个具体商品”（如外卖订单中的 “汉堡 ×2” 这个条目,属于某个具体的外卖订单（如订单号 666））

            // 4.3 保存订单到数据库
            Order savedOrder = orderRepository.save(order);
            //订单的数据访问接口OrderRepository将order对象保存到数据库通常是 orders表
            //执行orderRepository.save(order)后，order对象中的所有属性
            //（如userId、totalAmount、status、关联的shippingMethod信息等）会被映射到orders表的对应字段中
            // 同时数据库会自动生成订单的id（主键）和相关时间戳（如createdAt）
            // 并返回这个更新后的对象，赋值给savedOrder
            // 返回的savedOrder对象包含数据库生成的id、创建时间等属性，这些信息会自动回填到order对象中

//savedOrder 和 order 都是 Order 类型的对象
// order：是内存中手动创建的临时对象（通过 new Order() 创建），仅在代码运行时存在，未被保存到数据库。
// savedOrder：是经过数据库持久化后的对象（通过 orderRepository.save(order) 返回），包含数据库自动生成的信息。

            // 4.4 清空购物车
            cart.getCartItems().clear();//Cart 包含属性cartItems：购物项列表List<CartItem>，代表购物车中的一个具体商品
            cartRepository.save(cart);

            // 返回新订单ID
            return savedOrder.getId();
        } else {
            throw new RuntimeException("Payment failed");
        }
    }
}

//OrderService.js 和 OrderService.java 是前后端分离架构中的服务层对应关系：
//OrderService.java: 后端业务逻辑核心，处理订单的创建、查询、更新等复杂业务
//OrderService.js: 前端API调用封装，为React组件提供简洁的订单服务接口
//两者通过RESTful API进行通信，共同实现完整的订单管理功能。
