package com.NexusMart.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication//这是一个复合注解，相当于同时添加了 “自动配置”“组件扫描”“启动类” 三个功能，告诉 Spring Boot 这是一个可独立运行的应用。
@EnableConfigServer//告诉 Spring Cloud “这个应用是配置中心服务器”，可以接收其他微服务的配置请求，并返回
public class ConfigApplication {//启动整个 Spring Boot 应用

	public static void main(String[] args) {//main方法：是 Java 程序的 “启动入口”，就像服务器的 “电源按钮”—— 当你运行这个类时，JVM 会首先执行这个方法。
		SpringApplication.run(ConfigApplication.class, args);
		//告诉 Spring Boot：“这是应用的主类（ConfigApplication.class），以此为起点加载整个应用”。
		//解析命令行参数（args）：比如启动时可以通过java -jar app.jar --server.port=8888指定端口，args会接收这些参数并生效。
		//初始化 Spring 容器：加载所有依赖的组件（比如配置中心的核心功能、网络服务等），最终启动一个可运行的服务。	
	}

}
//在微服务架构中，可能有几十个服务（用户服务、订单服务、支付服务等），每个服务都有自己的配置文件（数据库地址、日志级别等）。如果分散管理，修改配置需要逐个服务重启，非常麻烦。

//配置中心的作用就是 **“集中管理所有服务的配置”**：

//所有服务的配置文件（比如user-service.yml、order-service.yml）都存放在一个地方（比如 Git 仓库、本地文件）。
//这个ConfigApplication启动后，会读取这些配置文件，成为 “配置的统一提供方”。
//其他微服务启动时，会向这个配置中心请求自己的配置，无需需在本地存放配置文件。
//当配置需要修改时，只需更新配置中心的文件，服务可以动态获取新配置（无需重启）。