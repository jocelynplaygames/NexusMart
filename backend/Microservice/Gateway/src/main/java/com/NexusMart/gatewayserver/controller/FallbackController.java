package com.NexusMart.gatewayserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
//与之前网关配置GatewayApplication.java中的setFallbackUri("forward:/contactSupport")直接对应
@RestController//表明这是一个处理 HTTP 请求的控制器，并且所有方法的返回值会直接作为 HTTP 响应体
public class FallbackController {

    @RequestMapping("/contactSupport")
    // Spring 的请求映射注解，表示当请求路径为/contactSupport时，会触发该方法。
    //与网关配置中的setFallbackUri("forward:/contactSupport")对应：当熔断触发时，网关会自动将请求转发到这个路径，执行该方法。
    public Mono<String> contactSupport() {//由于网关（Spring Cloud Gateway）基于响应式编程（Reactor），所以降级方法的返回值需要是响应式类型Mono（表示一个异步的单个结果）。
        return Mono.just("An error occurred. Please try after some time or contact support team!!!");
        //Mono.just(...)：创建一个包含具体字符串的响应式对象。
    }

}
//当GatewayApplication.java中的ordersCircuitBreaker触发熔断时（如ORDER服务超时或失败率过高）：
//网关根据setFallbackUri("forward:/contactSupport")的配置，将请求转发到当前控制器的/contactSupport路径。来到FallbackController.java中的contactSupport()方法这里
//contactSupport()方法被调用，返回包含提示信息的Mono<String>。
//这个字符串通过网关返回给客户端，用户看到的是友好的错误提示






