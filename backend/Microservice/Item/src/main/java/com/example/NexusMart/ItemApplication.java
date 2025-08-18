// File: backend/Microservice/Item/src/main/java/com/example/NexusMart/ItemApplication.java
package com.example.NexusMart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication()
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@EntityScan(basePackages = "com.example.NexusMart.model")
@EnableJpaRepositories(basePackages = "com.example.NexusMart.repository")
@EnableAspectJAutoProxy
public class ItemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemApplication.class, args);
	}

}
