package com.NexusMart.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
// 标记当前类是 Spring Boot 应用的主类，会自动触发：
// 组件扫描（扫描当前包及子包中的@Component、@Service等注解的类）；
// 自动配置（根据引入的依赖自动配置 Spring 环境）。
@EnableEurekaServer
// 明确当前应用是Eureka 服务注册中心，会启动 Eureka 的服务端功能（接收其他服务的注册、提供服务发现等）
public class EurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaApplication.class, args);
	}

}

// EurekaApplication本身是一个服务注册中心，由3个实例组成集群：
// # docker-compose.yml中的Eureka集群
// eurekaserver1:  # 端口8761
// eurekaserver2:  # 端口8762  
// eurekaserver3:  # 端口8763

// Eureka就像整个微服务架构的"电话簿"，所有服务都在这里登记自己的联系方式，需要联系其他服务时就查这个电话簿！

//如何被使用
// GatewayApplication.java中的路由配置
// .uri("lb://USER")    // 通过Eureka发现USER服务
// .uri("lb://ITEM")    // 通过Eureka发现ITEM服务

// // 微服务A调用微服务B
// @Autowired
// private RestTemplate restTemplate;
// // 通过服务名调用，Eureka自动解析为实际地址
// restTemplate.getForObject("http://USER/api/user/123", User.class);