// File: backend/NexusMart/src/main/java/com/example/NexusMart/repository/OrderRepository.java
package com.example.NexusMart.repository; // 存放数据访问接口的包

import com.example.NexusMart.model.Order; // 导入订单实体类
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // 标记为数据访问组件
import java.util.List;
//被服务层调用：OrderService通过orderRepository操作数据库
//被控制层调用：OrderController通过orderService操作数据库
@Repository // 告诉Spring这是一个数据访问层组件（仓库），由Spring自动管理
//特殊的@Component注解，标记该接口是 “数据仓库”，负责与数据库交互。Spring 会自动为这个接口创建实现类（无需我们编写）。
public interface OrderRepository extends JpaRepository<Order, Long> {
    //继承 Spring Data JPA 提供的JpaRepository接口，从而获得大量现成的数据库操作方法（如查询、保存、删除等）
    //<Order, Long>：指定实体类型（Order）（对应数据库中的订单表）和主键类型（Long）（id）
    
    List<Order> findByUserId(String userId);
    //findByUserId(String userId)：Spring Data JPA 的自定义查询方法，查询用户 ID 为userId的所有订单，返回List<Order>（订单实体列表）
    //生成的 SQL：相当于执行了SELECT * FROM orders WHERE user_id = ?（orders是Order实体对应的表名）
    //🦒orderRepository 直接操作的是 Order 实体类映射的数据库表，表名通常为 orders（由 @Table(name = "orders") 指定或默认规则生成）。
}
//这只是一个接口，没有实现类，方法怎么执行？”
//答案是：Spring 会在启动时自动为OrderRepository生成实现类，并根据方法名动态生成 SQL 语句，最终通过 JPA（如 Hibernate）执行数据库操作。
// 当调用orderRepository.findByUserId("user123")时：
// 1.Spring 找到OrderRepository的实现类。
// 2.解析findByUserId方法名，生成WHERE user_id = 'user123'的查询条件。
// 3.执行 SQL 查询，将结果转换为List<Order>返回。