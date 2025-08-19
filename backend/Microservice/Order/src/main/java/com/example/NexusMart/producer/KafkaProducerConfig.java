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

//获取Kafka集群的连接地址
//backend/Microservice/Config/src/main/resources/application.yml
//中配置了"optional:configserver:http://localhost:8090/"意味着配置是从Config Server获取的

//backend/Microservice/docker-compose/default/docker-compose.yml中Kafka集群的配置：
//kafka1: host.docker.internal:29092
//kafka2: host.docker.internal:29093
//kafka3: host.docker.internal:29094




    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    //后续会被用于配置 Kafka 生产者工厂（ProducerFactory），告诉生产者 “要连接到哪里的 Kafka 集群”

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        // 1. 设置Kafka集群地址
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 2. 设置Key的序列化器：将String类型的Key转为Kafka传输的字节数组
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 3. 设置Value的序列化器：将String类型的Value转为Kafka传输的字节数组
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }


    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {//<String, String> 表示消息的 Key 和 Value 都是 String 类型
        return new KafkaTemplate<>(producerFactory());
    }
}
//当其他组件（如 OrderService）需要发送消息到 Kafka 时，只需注入 KafkaTemplate 即可
//比如kafkaTemplate.send("order-created", orderId, orderJson) 
// 表示：发送一条消息到名为 order-created 的 Kafka 主题，消息的 Key 是 orderId，Value 是订单的 JSON 字符串。

