// File: backend/NexusMart/src/main/java/com/example/NexusMart/config/CorsConfig.java
package com.example.NexusMart.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
//CorsConfig 通过 corsConfigurationSource() 方法定义了跨域规则
@Configuration
public class CorsConfig {

    @Bean//corsConfigurationSource 注册为 Spring 容器中的 bean
    //SecurityConfig 作为 Spring 组件，会自动从容器中获取该 bean，并在 filterChain 方法中应用
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        // 允许前端域名 http://localhost:3000 访问
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许GET/POST/PUT/DELETE 等请求方法
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        // 允许携带 Authorization（token）、Content-Type 等请求头

        configuration.setAllowCredentials(true);
        //允许前端跨域请求携带凭证信息（如 Cookie、HTTP 认证信息、JWT token 等），不拦截
        configuration.setMaxAge(3600L);//设置预检请求（Preflight Request）的缓存时间
        //表示 “预检请求的结果会被浏览器缓存 3600 秒（1 小时）”。
        // 在这 1 小时内，同一前端域名对同一后端接口的跨域请求，不会再发送重复的预检请求

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //UrlBasedCorsConfigurationSource 是 Spring 提供的跨域配置源，用于根据请求的 URL 路径匹配对应的跨域规则
        //返回它后，Spring 的跨域过滤器会自动使用这些规则拦截并处理跨域请求
        source.registerCorsConfiguration("/**", configuration);
        //将 configuration 中定义的跨域规则，应用到后端所有接口（/**）
        //"/**"：表示匹配后端所有接口路径（* 匹配任意字符，** 匹配任意路径，包括多级目录
        //configuration：是前面定义的跨域规则（如允许的域名 http://localhost:3000、允许的方法 GET/POST 等）。

        return source;//将配置好的 UrlBasedCorsConfigurationSource 对象返回，Spring 会自动使用它来处理所有跨域请求
    }
}
//CorsConfig 解决 “前端能不能访问后端” 的问题（跨域限制）。
//SecurityConfig 解决 “前端访问后端时，能不能操作某个接口” 的问题（身份和权限验证）。

//前端请求到达时，先检查是否符合上述规则。如果不符合（如前端域名是 http://localhost:4000），
// 直接拒绝访问（返回 403）；如果符合，允许进入下一步安全验证

//SecurityConfig 的 filterChain 方法中，
// 通过 .cors(cors -> cors.configurationSource(corsConfigurationSource)) 
// 将 CorsConfig 定义的跨域规则整合到安全过滤器链中。
//跨域检查通过后，SecurityConfig 会按定义的规则验证请求：携带有效的 JWT token、符合特定角色、token 有效期等
//所有安全规则验证通过后，请求才会到达后端的业务接口（如 OrderController）