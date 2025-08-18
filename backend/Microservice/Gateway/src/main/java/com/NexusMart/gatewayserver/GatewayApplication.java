package com.NexusMart.gatewayserver;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Gateway Application - API Gateway for microservices
 * Core functionalities:
 * - Request routing to backend services
 * - Circuit breaker for fault tolerance
 * - Rate limiting for protection
 * - Authentication integration with Keycloak
 */
@SpringBootApplication
public class GatewayApplication {

	@Value("${keycloak.url}")
	private String keycloakUrl;

	/**
	 * Application entry point
	 * Starts Spring Boot application and loads all configurations
	 */
	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	/**
	 * Custom route configuration
	 * Defines request path to microservice mapping
	 * 
	 * @param routeLocatorBuilder Route builder
	 * @return Route locator
	 */
	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
				
				
				.route("users_route", r -> r.path("/api/user/**", "/api/cart/**", "/api/feedback/**", "/api/ratings/**")
						.filters(f -> f.circuitBreaker(c -> c.setName("usersCircuitBreaker").setFallbackUri("forward:/contactSupport")))
						.uri("lb:
				
				
				.route("items_route", r -> r.path("/api/item/**", "/api/categories/**", "/api/admin/**")
						.filters(f -> f.circuitBreaker(c -> c.setName("itemsCircuitBreaker").setFallbackUri("forward:/contactSupport")))
						.uri("lb:
				
				
				.route("orders_route", r -> r.path("/api/orders/**")
						.filters(f -> f.circuitBreaker(c -> c.setName("ordersCircuitBreaker").setFallbackUri("forward:/contactSupport"))
								.requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()).setKeyResolver(userKeyResolver())))
						.uri("lb:

				
				.route("payment_route", r -> r.path("/api/payment/**")
						.filters(f -> f.circuitBreaker(c -> c.setName("paymentCircuitBreaker").setFallbackUri("forward:/contactSupport")))
						.uri("lb:
				
				
				.route("keycloak_route", r -> r.path("/realms/**", "/protocol/**")
						.uri(keycloakUrl))
				.build();
	}

	/**
	 * Redis rate limiter configuration
	 * Limits request frequency: 1 request per second, burst 1, token bucket capacity 1
	 * Prevents system overload
	 */
	@Bean
	public RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(1, 1, 1);  
	}

	/**
	 * Circuit breaker default configuration
	 * Sets circuit breaker timeout and default behavior
	 * Triggers circuit breaker when service response exceeds 10 seconds
	 */
	@Bean
	public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
		return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
				.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
				.timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(10)).build())
				.build());
	}

	/**
	 * Rate limiting key resolver
	 * Determines how to identify different users for rate limiting
	 * Uses "order" header field to identify users, defaults to "anonymous" if not present
	 */
	@Bean
	KeyResolver userKeyResolver() {
		return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("order"))
				.defaultIfEmpty("anonymous");
	}
}






