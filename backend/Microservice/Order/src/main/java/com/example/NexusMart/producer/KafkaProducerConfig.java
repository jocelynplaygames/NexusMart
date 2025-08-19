// File: backend/Microservice/Order/src/main/java/com/example/NexusMart/producer/KafkaProducerConfig.java
package com.example.NexusMart.producer;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
//Spring 的注解，标记这个类是 “配置类”。Spring 容器启动时会自动扫描并加载这个类，执行其中的 @Bean 方法
public class KafkaProducerConfig {


// 这表明 `bootstrapServers` 的值是从配置文件中读取的，具体是 `spring.kafka.bootstrap-servers` 这个配置项。
// 查看了 `backend/Microservice/Order/src/main/resources/application.yml`，但没有找到 Kafka 配置。
// 注意到 Order 服务的配置中有：spring:  config:    import: "optional:configserver:http://localhost:8090/"
// 这表明配置是从配置中心（Config Server）获取的。
// 查看配置中心的配置文件 `backend/Microservice/Config/config-repo/` 目录，发现这里有各个服务的配置文件，包括 `order.yml`。
//  在 order.yml 中找到配置spring:  kafka:    bootstrap-servers: localhost:9092,localhost:9093,localhost:9094
//Kafka 集群由 3 个 broker 节点组成
    @Value("${spring.kafka.bootstrap-servers}")//获取Kafka集群的连接地址
    private String bootstrapServers;
    //后续会被用于配置 Kafka 生产者工厂（ProducerFactory），告诉生产者 “要连接到哪里的 Kafka 集群”

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();//存储配置，Kafka配置项非常多（如连接地址、序列化方式、重试机制等）
//ProducerConfig 是 Apache Kafka 客户端（org.apache.kafka.clients.producer 包下）提供的一个工具类
// 它定义了 Kafka 生产者（Producer）的所有配置参数常量
        // “Kafka 集群引导地址” 配置项
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);//来自上文
        // “消息 Key 的序列化器” 配置项
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //StringSerializer.class：Kafka 提供的默认序列化器，用于将String类型的 Key 转为字节数组

        // “消息 Value 的序列化器” 配置项
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);//工厂封装
        //DefaultKafkaProducerFactory：Spring Kafka 提供的默认生产者工厂实现类
        // 负责根据前面的configProps配置，创建和管理 Kafka 生产者（org.apache.kafka.clients.producer.Producer）实例。
    }


    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {//<String, String> 表示消息的 Key 和 Value 都是 String 类型
        return new KafkaTemplate<>(producerFactory());//KafkaTemplate必须接收一个 ProducerFactory
    }
}
//ProducerFactory 是创建 KafkaTemplate 的前提，而 KafkaTemplate 是实际发送消息的工具
//当其他组件（如 OrderService）需要发送消息到 Kafka 时，只需注入 KafkaTemplate 即可
//当业务代码调用 kafkaTemplate.send("topic", "key", "value") 时：
//KafkaTemplate 会通过 ProducerFactory 获取一个 Producer 实例。
//Producer 会使用工厂中配置的序列化器，将"key"和"value"转为字节数组。
//按照配置的bootstrap.servers地址，连接 Kafka 集群并发送消息。

