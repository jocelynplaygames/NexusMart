// File: backend/Microservice/Item/src/main/java/com/example/NexusMart/config/RedisConfig.java
package com.example.NexusMart.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        config.setPassword(redisPassword);
        logger.info("LettuceConnectionFactory created with Host: {}, Port: {}, Password: {}", redisHost, redisPort, redisPassword);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return redisTemplate;
    }
}



//@Configuration
//public class RedisConfig {
//
//    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);
//
//    @Value("${spring.data.redis.password}")
//    private String redisPassword;
//
//    @Value("${spring.data.redis.sentinel.master}")
//    private String master;
//
//    @Value("${spring.data.redis.sentinel.nodes}")
//    private String sentinelNodes;
//
//    @Bean(destroyMethod = "shutdown")
//    public ClientResources clientResources() {
//        return DefaultClientResources.create();
//    }
//
//    @Bean
//    public RedisSentinelConfiguration redisSentinelConfiguration() {
//        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
//        sentinelConfig.master(master);
//        Arrays.stream(sentinelNodes.split(",")).forEach(node -> {
//            String[] parts = node.split(":");
//            String host = parts[0];
//            int port = Integer.parseInt(parts[1]);
//            sentinelConfig.sentinel(host, port);
//        });
//        sentinelConfig.setPassword(RedisPassword.of(redisPassword));
//
//        // Log configuration details
//        logger.info("Redis Sentinel Configuration: master={}, nodes={}, password={}", master, sentinelNodes, redisPassword);
//
//        return sentinelConfig;
//    }
//
//    @Bean
//    public LettuceConnectionFactory redisConnectionFactory(RedisSentinelConfiguration sentinelConfig) {
//        LettuceConnectionFactory factory = new LettuceConnectionFactory(sentinelConfig);
//        logger.info("LettuceConnectionFactory created with Sentinel Configuration: {}", sentinelConfig);
//        return factory;
//    }
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
//                .allowIfSubType(Object.class)
//                .build();
//        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
//
//        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
//        return redisTemplate;
//    }
//
//    @Bean
//    public RedisURI redisUri() {
//        RedisURI.Builder builder = RedisURI.Builder.sentinel(getHost(sentinelNodes.split(",")[0]), getPort(sentinelNodes.split(",")[0]), master)
//                .withPassword(redisPassword.toCharArray())
//                .withTimeout(Duration.ofSeconds(10));
//
//        Arrays.stream(sentinelNodes.split(",")).skip(1).forEach(node -> {
//            builder.withSentinel(getHost(node), getPort(node));
//        });
//
//        RedisURI redisUri = builder.build();
//
//        // Log Redis URI
//        logger.info("Redis URI: {}", redisUri);
//
//        return redisUri;
//    }
//
//    @Bean
//    public RedisClient redisClient(ClientResources clientResources, RedisURI redisUri) {
//        RedisClient redisClient = RedisClient.create(clientResources, redisUri);
//        logger.info("Created RedisClient with URI: {}", redisUri);
//        return redisClient;
//    }
//
//    @Bean
//    public StatefulRedisConnection<String, String> redisConnection(RedisClient redisClient) {
//        logger.info("Attempting to connect to Redis...");
//        StatefulRedisConnection<String, String> connection = redisClient.connect();
//        logger.info("Successfully connected to Redis");
//        return connection;
//    }
//
//    private String getHost(String node) {
//        return node.split(":")[0];
//    }
//
//    private int getPort(String node) {
//        return Integer.parseInt(node.split(":")[1]);
//    }
//}