package com.example.NexusMart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//订单服务的启动类
// 1.启动订单微服务（通过SpringApplication.run）；
// 2.开启 JPA 审计功能（通过@EnableJpaAuditing），自动记录订单数据的创建 / 修改痕迹（如谁创建了订单、何时修改了订单状态）。


// OrderController.java - HTTP请求处理
// OrderService.java - 核心业务逻辑
// OrderRepository.java - 数据库操作
// Order.java - 订单实体
// OrderConsumer.java - Kafka消息消费
// DataSourceConfig.java - 数据库配置
// AuditAwareImpl.java - 审计实现

// 调用AuditAwareImpl.java中的getCurrentAuditor()方法
@SpringBootApplication()
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
//开启 JPA 审计功能，用于自动记录实体的创建时间、修改时间、创建人、修改人等信息。
//指定一个名为auditAwareImpl的 Bean（需要自己定义AuditAwareImpl.java），用于获取当前操作的 “用户”（如登录用户 ID），并将其记录到审计字段中（如createdBy、updatedBy）。
// 
// 为什么有多个相同的AuditAwareImpl.java？每个微服务都有自己的Spring容器，无法共享Bean
public class OrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderApplication.class, args);
		// SpringApplication.run(...)：Spring Boot 的启动方法，用于启动整个订单服务。
		// OrderApplication.class：传入当前类的字节码对象，告诉 Spring Boot 以该类为入口启动。
		// args：将命令行参数传入启动方法。

	}

}
// 当你保存一个实体（比如订单）时：
// JPA 检测到实体中有 @CreatedBy 或 @LastModifiedBy 注解的字段。（JPA 是 Java Persistence API（Java 持久化 API）的缩写，是 Java 官方定义的一套 ORM（对象关系映射）规范，用于简化 Java 程序操作数据库的过程。）
// 通过 auditorAwareRef 指定的 Bean 名称（auditAwareImpl），找到 AuditAwareImpl 实例。
// 调用 AuditAwareImpl 的 getCurrentAuditor() 方法，获取当前操作用户的标识（如用户 ID）。
// 自动将这个用户标识填入 @CreatedBy 或 @LastModifiedBy 字段，完成审计记录。
// 总结：
// auditorAwareRef 就像一个 “地址牌”，告诉 JPA 审计功能：“去这个名为 xxx 的 Bean 那里，就能找到当前操作用户的信息”。找到当前操作用户的信息后，JPA 会自动将这些信息存入数据库，具体存入实体类中标记了 @CreatedBy 和 @LastModifiedBy 注解的字段里
// 它的值必须和你定义的 AuditorAware 实现类的 Bean 名称一致，否则 JPA 会找不到用户信息，导致 @CreatedBy 等字段无法自动填充。



// 假设你的订单实体类 Order 有这些字段
// @Entity
// public class Order {
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;
    
//     private String productName; // 订单商品名
    
//     // 审计字段：创建人（由JPA自动填充）
//     @CreatedBy 
//     private String createdBy;

// 当你调用 orderRepository.save(order) 保存订单时：

// JPA 会先通过 auditorAwareRef = "auditAwareImpl" 找到 AuditAwareImpl 这个 Bean；
// 调用 AuditAwareImpl 的 getCurrentAuditor() 方法，拿到当前用户信息（比如 userId = "10086"）；
// 自动将用户信息存入审计字段：
// 第一次保存（新建订单）：createdBy 字段会被赋值为 "10086"，createdTime 被赋值为当前时间；
// 后续修改订单并保存：updatedBy 字段会被更新为 "10086"，updatedTime 被更新为当前时间；
// 最终这些字段会随着整个订单对象一起被写入数据库的 orders 表中。
// 3. 数据库中的效果
// 保存后，orders 表中会有这样一条记录

//AuditAwareImpl 负责 “提供用户信息”，而 JPA 审计功能负责 “自动将这些信息存入数据库的审计字段”，整个过程无需你手动编写 order.setCreatedBy(userId) 这样的代码，完全由框架自动完成。


//订单服务
// 1. 应用启动 → OrderApplication.java
//     ↓
// 2. HTTP请求 → OrderController.java
//     ↓
// 3. 业务处理 → OrderService.java
//     ↓
// 4. 数据操作 → OrderRepository.java
//     ↓
// 5. 实体映射 → Order.java + BaseEntity.java
//     ↓
// 6. 消息发送 → KafkaProducerConfig.java
//     ↓
// 7. 消息消费 → OrderConsumer.java
//     ↓
// 8. 状态通知 → OrderStatusWebSocketHandler.java
//     ↓
// 9. 审计记录 → AuditAwareImpl.java
//SAGA（Saga Pattern）是一种分布式事务管理的设计模式，用于在微服务架构中处理跨多个服务的复杂业务事务。
//核心思想
//将大事务拆分成多个小事务
//每个小事务都有对应的补偿操作
//失败时通过补偿操作回滚






