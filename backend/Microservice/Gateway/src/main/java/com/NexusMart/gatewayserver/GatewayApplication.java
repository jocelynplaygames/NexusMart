package com.NexusMart.gatewayserver;

// 导入必要的依赖包
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;  // 断路器配置
import io.github.resilience4j.timelimiter.TimeLimiterConfig;       // 超时限制配置
import org.springframework.beans.factory.annotation.Value;           // 注入配置值
import org.springframework.boot.SpringApplication;                  // Spring Boot启动类
import org.springframework.boot.autoconfigure.SpringBootApplication; // 自动配置注解
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory; // 断路器工厂
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder; // 断路器配置构建器
import org.springframework.cloud.client.circuitbreaker.Customizer;  // 断路器自定义器
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver; // 限流键解析器
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter; // Redis限流器
import org.springframework.cloud.gateway.route.RouteLocator;         // 路由定位器
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder; // 路由构建器
import org.springframework.context.annotation.Bean;                 // Bean定义注解
import reactor.core.publisher.Mono;                               // 响应式编程单值类型

import java.time.Duration;                                        // 时间间隔

/**
*这个网关服务的核心功能是：
*请求路由：根据路径将请求转发到对应的后端服务（USER、ITEM、ORDER 等）。
*熔断降级：通过 Resilience4j 监控后端服务，故障时触发降级，返回友好提示。
*限流保护：对订单服务设置限流，防止高并发压垮服务。
*认证集成：转发认证相关请求到 Keycloak，实现统一身份认证。
 */
@SpringBootApplication
public class GatewayApplication {
	//@Value用于读取配置文件中的属性值
	// 从配置文件读取Keycloak认证服务器地址
	@Value("${keycloak.url}")// 从配置文件（如 application.yml）中读取 Keycloak 服务的地址（如http://localhost:8080）
	private String keycloakUrl;

// 前提假设
// 网关服务的地址：http://localhost:9000（网关自己的端口是 9000）
// 网关服务（你代码中的GatewayApplication）运行的网络地址（IP + 端口）。作用：作为所有客户端请求的 “统一入口”，客户端只需要知道网关地址，不需要知道后端其他服务的地址。
// keycloakUrl配置为：http://localhost:8080（Keycloak 服务实际运行在 8080 端口）
// Keycloak 服务（身份认证服务）运行的网络地址（IP + 端口）。作用：处理用户登录、令牌生成、权限验证等认证相关操作，是一个独立的服务。

// 客户端（比如浏览器或手机 APP）想获取myrealm领域的配置，它会直接向网关发送请求：
// http://localhost:9000/realms/myrealm
// 为什么客户端不直接访问 Keycloak 的http://localhost:8080/realms/myrealm？因为在微服务架构中，网关是所有请求的统一入口，客户端只需要知道网关地址，不需要知道后端各个服务（包括 Keycloak）的具体地址，这样更安全、更易维护。

// 当网关收到到http://localhost:9000/realms/myrealm这个请求后，会执行以下步骤：
// 检查请求路径/realms/myrealm，发现它符合keycloak_route路由中path("/realms/**")的规则（**匹配myrealm）。
// 根据路由配置.uri(keycloakUrl)，网关会把这个请求转发到keycloakUrl对应的地址，也就是http://localhost:8080。
// 网关会保持原路径不变，只是把 “网关地址” 替换成 “Keycloak 地址”，所以最终转发到 Keycloak 的地址是：http://localhost:8080/realms/myrealm。
// Keycloak 在8080端口收到/realms/myrealm请求后，会处理并返回myrealm领域的配置数据（比如该领域下的用户、角色、认证规则等）。
// Keycloak 把响应结果返回给网关，网关再把结果转发回客户端。

// keycloak_route路由就像一个 “中转站”：
// 客户端 → 网关/realms/myrealm → 转发到 Keycloak http://localhost:8080/realms/myrealm
// 这样客户端不用记住 Keycloak 的地址，只需访问网关即可完成认证相关操作。

	/**
	 * 应用启动入口
	 * 启动Spring Boot应用，加载所有配置和Bean
	 */
	public static void main(String[] args) {
		//Java 程序的标准入口方法，JVM 启动时会首先执行这个方法。
		// String[] args：用于接收命令行参数（例如启动时指定端口：--server.port=9000），这里暂时用不到，但必须保留方法签名。
		SpringApplication.run(GatewayApplication.class, args);
		//Spring Boot 的核心启动方法，作用是：
		//加载 Spring 上下文（IOC 容器），扫描并注册所有带@Bean、@Controller等注解的组件（包括路由配置、熔断 Bean、限流规则等）。
		//初始化 Spring Cloud Gateway 的核心组件（如路由匹配器、过滤器链、负载均衡器等）。
		//启动嵌入式 Web 服务器（默认是 Netty，因为 Gateway 基于响应式编程），监听配置的端口（默认 8080，可通过application.properties修改）。
	}

	/**
	 * 自定义路由配置
	 * 定义请求路径与微服务的映射关系，就像"交通指挥员"
	 * 
	 * @param routeLocatorBuilder 路由构建器
	 * @return 路由定位器
	 */
	@Bean //当你在一个方法上添加 @Bean 时，这个方法的返回值会被 Spring 容器管理，成为一个 “Bean”（可以理解为 Spring 容器中的一个对象实例）
	public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()//return后的内容是RouteLocator对象（路由定位器），它包含了所有定义的路由规则。这个返回值会被 Spring 容器接收
				
				// 用户相关路由：用户管理、购物车、反馈、评分
				.route("users_route", r -> r.path("/api/user/**", "/api/cart/**", "/api/feedback/**", "/api/ratings/**")
						.filters(f -> f.circuitBreaker(c -> c.setName("usersCircuitBreaker").setFallbackUri("forward:/contactSupport")))//接下去是FallbackController.java中的方法
						.uri("lb://USER"))  // lb:// 表示负载均衡，USER是服务名
				
				// 商品相关路由：商品管理、分类、管理员功能
				.route("items_route", r -> r.path("/api/item/**", "/api/categories/**", "/api/admin/**")
						.filters(f -> f.circuitBreaker(c -> c.setName("itemsCircuitBreaker").setFallbackUri("forward:/contactSupport")))//当熔断触发时，请求会被转发到/contactSupport（降级接口）
						.uri("lb://ITEM"))
				
				// 订单路由：订单管理（带限流保护）
				.route("orders_route", r -> r.path("/api/orders/**")//.route:定义整个路由。.path:路径匹配。 定义一个 ID 为"orders_route"的路由，规定 “路径匹配/api/orders/**的请求
						.filters(f -> f.circuitBreaker(c -> c.setName("ordersCircuitBreaker").setFallbackUri("forward:/contactSupport"))//.filters:配置过滤器（熔断、限流）
								.requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()).setKeyResolver(userKeyResolver())))//最小传入的是下文定义的，redisRateLimiter()和userKeyResolver()是 RequestRateLimiterSpec 类（Spring Cloud Gateway 提供的限流配置类）中的自带方法
								// f -> f.circuitBreaker(...).requestRateLimiter(...) 
								// “接收一个过滤器配置对象f，先调用circuitBreaker方法添加熔断过滤器，再调用requestRateLimiter方法添加限流过滤器，返回配置后的过滤器配置对象”。
								//c -> c.setName(...).setFallbackUri(...)
								//“接收一个熔断配置对象c，调用setName设置熔断组件名称为ordersCircuitBreaker，调用setFallbackUri设置降级路径为/contactSupport，返回配置后的熔断对象”。
								//config -> config.setRateLimiter(...).setKeyResolver(...)
								//“接收一个限流配置对象config，调用setRateLimiter关联redisRateLimiter定义的限流规则，调用setKeyResolver关联userKeyResolver定义的限流维度，返回配置后的限流对象”。
						.uri("lb://ORDER"))// .uri: 配置转发地址

				//path("/api/orders/**")的作用：这是一个 “路径谓词（Path Predicate）”，由 Spring Cloud Gateway 内置实现。它会检查请求的 URL 路径是否符合/api/orders/**的模式
				// 匹配到orders_route后，请求会进入该路由配置的过滤器链（filters(...)部分），依次执行熔断过滤器和限流过滤器。
//确定限流维度（userKeyResolver）,下文定义了该方法
//执行限流规则（redisRateLimiter）
//熔断过滤器（circuitBreaker）：监控后端服务状态。熔断过滤器会关联全局默认配置（defaultCustomizer方法）
//(通过setName("ordersCircuitBreaker")给当前路由的熔断组件命名后，Spring 会自动找到前面defaultCustomizer中定义的全局默认配置（超时 10 秒、默认熔断规则），并应用到这个名为ordersCircuitBreaker的组件上。)
//控请求到 ORDER 服务的状态。当请求转发到ORDER服务后，熔断过滤器会监控：是否超时（超过 10 秒未响应）？是否返回错误（如 500、404 等）？这些都会被记为 “失败请求”。
//触发熔断时的降级处理,如果失败率达到阈值，熔断器会 “打开”，此时请求不会真正发送到ORDER服务，而是直接转发到/contactSupport（由setFallbackUri指定）。例如，/contactSupport可能是一个预先定义的接口，返回 “订单服务暂时不可用，请稍后重试” 的友好提示。

//请求转发：发送到lb://ORDER对应的后端服务,过滤器链执行通过（未被限流、未触发熔断）后，网关会将请求转发到uri("lb://ORDER")指定的目标服务。
//lb://ORDER的含义：lb是LoadBalance（负载均衡）的缩写，由 Spring Cloud LoadBalancer 组件实现。ORDER是后端服务的 “服务名”（在服务注册中心，如 Eureka/Consul 中注册的名称）。
//转发： 网关会通过服务注册中心查询ORDER服务的所有可用实例（比如192.168.1.100:8082、192.168.1.101:8082），然后根据负载均衡策略（默认轮询）选择一个实例，将/api/orders/123请求转发过去。


				// 支付路由：支付处理
				.route("payment_route", r -> r.path("/api/payment/**")
						.filters(f -> f.circuitBreaker(c -> c.setName("paymentCircuitBreaker").setFallbackUri("forward:/contactSupport")))
						.uri("lb://PAYMENT"))
				
				// Keycloak认证路由：直接转发到认证服务器
				//路由规则：将/realms/**（Keycloak 的领域配置）和/protocol/**（认证协议，如 OAuth2、OpenID Connect）路径的请求转发到 Keycloak 服务，实现统一认证授权。
				.route("keycloak_route", r -> r.path("/realms/**", "/protocol/**")
						.uri(keycloakUrl))
				.build();
	}

	/**
	 * Redis限流器配置
	 * 限制请求频率：每秒1个请求，突发1个请求，令牌桶容量1
	 * 防止系统被大量请求压垮
	 */
	@Bean
	public RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(1, 1, 1);  // 参数：令牌桶容量, 每秒补充速率, 突发请求数
	}
	//RedisRateLimiter是 Spring Cloud Gateway 提供的基于 Redis 的令牌桶限流工具，参数含义：
// 第一个1：replenishRate（令牌补充速率）：每秒生成 1 个令牌。
// 第二个1：burstCapacity（令牌桶容量）：最多允许 1 个令牌的突发（即瞬间最多处理 1 个请求）。
// 第三个1：timeout（超时时间）：获取令牌的超时时间（秒）。
// 效果：每秒最多处理 1 个请求，超过则被限流（返回 429 Too Many Requests）。

//Redis中存储的限流数据
//key: "rate_limit:order:123"  # 用户123的限流键
//value: {
//  "tokens": 1,               # 当前可用令牌数
//  "last_refill": 1640995200, # 上次补充时间
//  "capacity": 1              # 令牌桶容量
//}

// 对比令牌数、时间过程由 Spring Cloud Gateway 自动执行，无需手动编写比较逻辑，只需通过redisRateLimiter配置令牌规则即可。


	/**
	 * 断路器默认配置
	 * 设置断路器的超时时间和默认行为
	 * 当服务响应超过10秒时，自动触发断路器
	 */
	@Bean //这个 Bean 的配置最终会作用到由熔断工厂创建的所有熔断组件（CircuitBreaker） 上
	//当你在代码中通过注解（如@CircuitBreaker）或 API 使用熔断功能时，框架会自动使用这个工厂创建的熔断组件

	//这个方法返回的是一个 Customizer 类型的对象（用于配置熔断规则）。
	//因为加了 @Bean 注解，Spring 会自动将这个 Customizer 对象存入容器中。
	//之后，当 Spring Cloud Gateway 需要配置熔断组件时，会自动从容器中找到这个 Customizer Bean 并使用它的配置。
	public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {//defaultCustomizer 方法的核心作用是给所有熔断组件设置一套通用的默认配置
		//Customizer类型：Spring 框架提供的函数式接口（专业术语）。含义：“定制器”，用于对某个组件进行自定义配置。
		//ReactiveResilience4JCircuitBreakerFactory。类型：Resilience4j 框架结合 Spring Cloud Gateway 提供的熔断工厂类（专业术语）。
		//熔断过滤器会关联全局默认配置（defaultCustomizer方法）
		//定义了：请求超时时间 10 秒，以及默认熔断规则（失败率 > 50% 且请求数 > 20 时触发熔断）
		return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
		//Resilience4JConfigBuilder：Resilience4j 框架提供的配置构建器类（专业术语）。
		// 外层Lambda：对熔断工厂(factory)进行配置，调用configureDefault方法// 内层Lambda：为每个熔断组件(id)创建配置，包含默认熔断规则和10秒超时
		// Lambda 的关键是：把->左边看作 “输入参数”，右边看作 “对参数的操作及返回结果”
		//factory：参数，代表ReactiveResilience4JCircuitBreakerFactory（熔断工厂实例）。方法体：factory.configureDefault(...)
		//id：参数，代表每个熔断组件的唯一标识（如usersCircuitBreaker）。方法体：通过Resilience4JConfigBuilder创建配置对象，设置熔断规则和超时时间，最后build()返回配置。
				.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())  // 使用默认断路器配置
				//CircuitBreakerConfig：Resilience4j 框架提供的断路器配置类（专业术语）
				.timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(10)).build())  // 10秒超时
				//TimeLimiterConfig：Resilience4j 框架提供的超时配置类（专业术语）。含义：用于配置请求的超时时间（超过该时间视为失败）。
				//timeoutDuration：框架提供的方法名。含义：“超时持续时间”，用于设置具体的超时时间
				//Duration：Java 标准库中的时间类（专业术语）。含义：表示一段时间
				.build());//“构建”，表示将前面设置的参数整合为一个完整的配置对象
	}
// 	从请求头order中获取值作为 key；如果没有该头，则用"anonymous"。
// 效果：相同order头的请求会被视为同一 “用户”，共享限流配额（即同一个订单 ID 的请求每秒最多 1 个）。

	/**
	 * 限流键解析器
	 * 决定如何识别不同的用户进行限流
	 * 这里根据请求头中的"order"字段识别用户，如果没有则标记为"匿名用户"
	 */
	@Bean
	KeyResolver userKeyResolver() {//KeyResolver：Spring Cloud Gateway 提供的接口，用于定义限流的 “键”（即限流维度）
	//ServerWebExchange exchange：代表当前的 HTTP 请求 - 响应上下文，包含了请求头、请求参数、路径等所有请求信息。
		return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("order"))// 从请求头Headers中获取order字段的值（例如请求头可能包含order: 123，这里就会获取到"123"）。
		// Mono：响应式编程中的类型，表示一个异步的单个结果（这里是限流的键）。
		// justOrEmpty(...)：如果order头存在且有值，就返回包含该值的Mono；如果order头不存在（为null），就返回一个空的Mono。
				.defaultIfEmpty("anonymous");  // 从请求头order中获取值作为 “限流键”（比如请求头order: 123，则键为123）；如果没有该头，默认用"anonymous"。
	}
	//Mono：响应式编程中的异步结果容器（Reactor 框架），用于处理非阻塞请求。
}


// redis存储结构
// # 不同用户的限流状态
// rate_limit:order:123     # 用户123的限流状态
// rate_limit:order:456     # 用户456的限流状态  
// rate_limit:anonymous     # 匿名用户的限流状态

// 前端请求 → Nginx → GatewayApplication → 微服务
//                 ↓
//             FallbackController (降级处理)
//                 ↓
//             SecurityConfig (安全验证)
//                 ↓
//             CorsConfig (跨域处理)
//                 ↓
//             路由转发到具体微服务:
//             - USER服务 (用户管理)
//             - ITEM服务 (商品管理)  
//             - ORDER服务 (订单管理)
//             - PAYMENT服务 (支付处理)
//             - Keycloak (认证服务)


// 假设：
// 客户端（浏览器）：寄件人，想知道 “订单 456” 的详情。
// 网关：快递中转站，地址是http://localhost:9000。
// 后端 ORDER 服务：收件人，地址是http://localhost:8082（网关通过lb://ORDER发现的实例）。
// 步骤 1：客户端发送请求（寄包裹）
// 客户端给网关发了一个 “查询订单 456” 的请求（相当于寄包裹到中转站）：
// GET http://localhost:9000/api/orders/456
// 步骤 2：网关转发请求（中转站派件）
// 网关根据路由规则，把这个请求转发给 ORDER 服务（中转站把包裹送到收件人）：
// GET http://localhost:8082/api/orders/456
// 步骤 3：后端服务处理并生成响应（收件人回寄包裹）
// ORDER 服务收到请求后，查询到订单 456 的详情，生成一个 HTTP 响应（相当于收件人把 “订单详情” 打包回寄）：

// 状态码：200 OK（表示成功）
// 响应体：{"id":456, "goods":"手机", "price":5000}（订单数据）
// 步骤 4：响应通过网关返回给客户端（中转站转发回寄包裹）
// ORDER 服务把响应发给网关（回寄的包裹先到中转站），网关再把这个响应原封不动地转发给客户端（中转站把包裹交给寄件人）。

// 客户端最终收到的响应就是：
// 200 OK + {"id":456, "goods":"手机", "price":5000}
// 为什么 “无需手动写代码” Spring Cloud Gateway 的核心功能之一就是 “请求转发与响应回传”，它内部已经实现了这套逻辑






//lb://ORDER
// （1）lb://：负载均衡协议标识
// lb 是 Load Balance（负载均衡）的缩写，是 Spring Cloud 规定的协议标识。
// 作用：告诉网关 “通过服务名从服务注册中心（如 Eureka、Nacos 等）查找服务实例，并在多个实例间做负载均衡”。
// 举例：如果 ORDER 服务有 2 个实例（192.168.1.100:8082 和 192.168.1.101:8082），网关会自动在这两个实例间分配请求（比如轮询方式），避免单个服务压力过大。

// （2）ORDER：后端服务的 “服务名”
// ORDER 是后端服务在服务注册中心注册时使用的名称（类似人的名字，用于标识服务）。
// 服务注册中心（如 Eureka）会维护一个 “服务名 → 实例地址” 的映射表，例如：

// 服务名       实例地址列表
// ORDER  →  [192.168.1.100:8082, 192.168.1.101:8082]
// USER   →  [192.168.1.102:8083]
// 网关通过 ORDER 这个服务名，就能从注册中心找到对应的实例地址，无需硬编码具体 IP 和端口。

// （3） 实际转发流程举例
// 当客户端请求 http://网关地址:9000/api/orders/456 时：
// 网关匹配到 orders_route 路由，触发 uri("lb://ORDER")。
// 网关向服务注册中心查询 ORDER 服务的可用实例，得到 [192.168.1.100:8082, 192.168.1.101:8082]。
// 网关通过负载均衡算法（默认默认轮询）选择一个实例，比如 192.168.1.100:8082。
// 最终请求被转发到 http://192.168.1.100:8082/api/orders/456。




//代码中熔断和令牌桶限流的工作过程
//限流通过令牌桶控制请求频率（代码中的 RedisRateLimiter 和 KeyResolver），防止服务被瞬间高并发压垮；
//熔断通过监控服务状态（代码中的 Resilience4j 配置，Resilience4j 的默认配置中，包含一套监控服务状态的核心规则，用于判断服务是否 “健康”），在服务故障时 “跳闸” 并返回降级响应，保护整个系统。
//请求路径：用户多次访问订单接口 GET /api/orders/123（通过网关地址 http://localhost:9000 访问）。
//限流配置：redisRateLimiter() 定义每秒生成 1 个令牌，桶容量 1（即每秒最多处理 1 个请求）；userKeyResolver() 从请求头 order 取键（假设请求头为 order:123）。
//熔断配置：defaultCustomizer() 定义超时 10 秒，失败率 > 50% 且请求数 > 20 时触发熔断；ordersCircuitBreaker 配置降级路径 /contactSupport。
//后端服务：ORDER 服务（lb://ORDER）处理订单请求，模拟两种情况：正常响应、超时 / 错误。

//第一步：首次请求（正常流程，未触发限流和熔断）
//用户第 1 次发送请求：
//GET http://localhost:9000/api/orders/123（请求头包含 order:123）
//1. 路由匹配与限流检查
//网关匹配到 orders_route 路由（路径 /api/orders/** 匹配）。
//执行 requestRateLimiter 过滤器：
//调用 userKeyResolver()：从请求头 order 拿到键 123。
//调用 redisRateLimiter()：检查 123 的令牌桶，此时桶内有 1 个令牌（初始状态），请求 “拿到令牌”，允许通过。
//2. 熔断过滤器准备
//执行 circuitBreaker 过滤器：绑定 ordersCircuitBreaker 组件，应用 defaultCustomizer 的全局配置（超时 10 秒，默认熔断规则）。
//3. 转发请求到后端服务
//网关通过 lb://ORDER 找到 ORDER 服务实例（如 192.168.1.100:8082），转发请求。
//ORDER 服务正常响应（5 秒内返回订单数据 {"id":123, "price":100}）。
//4. 响应返回与状态记录
//响应通过网关返回给用户（200 OK + 订单数据）。
//熔断过滤器记录 “成功请求”（失败率 0%），令牌桶令牌数减为 0（等待 1 秒后自动补充 1 个）。
//第二步：1 秒内发送第 2 次请求（触发限流）
//用户在第 1 次请求后 0.5 秒内，发送第 2 次请求（同路径，同请求头 order:123）。
//1. 限流检查失败
//再次执行 requestRateLimiter 过滤器：
//键仍为 123，但令牌桶内令牌未补充（1 秒未到），无令牌可用。
//限流触发：网关直接返回 429 Too Many Requests（太多请求），终止流程。
//2. 结果
//用户收到限流响应，提示 “请求过于频繁，请稍后再试”，请求未到达 ORDER 服务。






