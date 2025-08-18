// File: backend/NexusMart/src/main/java/com/example/NexusMart/XianweiECommerceApplication.java
package com.example.NexusMart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication()
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
public class NexusMartApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexusMartApplication.class, args);
	}

}
