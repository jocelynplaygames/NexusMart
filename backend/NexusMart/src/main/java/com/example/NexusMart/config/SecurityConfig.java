// File: backend/NexusMart/src/main/java/com/example/NexusMart/config/SecurityConfig.java
package com.example.NexusMart.config;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // 从配置文件（backend/Microservice/docker-compose/default/common-config.yml）注入Keycloak的认证服务器地址和领域
    //keycloak.auth-server-url: "keycloak:http://keycloak:7080"
    //keycloak.realm: "NexusMart"

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;
    //存储的是 Keycloak 服务器的地址，当前端传递 Bearer {token} 时，Spring Security 需要知道 “这个 token 是哪个认证服务器签发的”

    @Value("${keycloak.realm}")
    private String realm;
    //告诉 Spring Security “当前应用属于 Keycloak 的哪个领域”，只能验证该领域下签发的 JWT
    
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        //用于定义跨域资源共享（CORS）的配置规则（如允许哪些域名访问、允许哪些请求方法等）
        this.corsConfigurationSource = corsConfigurationSource;
    }
    //将外部定义的跨域配置（CorsConfigurationSource）引入到SecurityConfig类中，供后续配置 Spring Security 的跨域规则使用

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 调用 Spring Security 的 CORS 配置方法，用于开启并配置跨域支持
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                //->哪些前端域名访问后端的规则，已经定义在corsConfigurationSource里了，现在把这些规则应用到当前的安全配置中

                // 2. 配置接口访问权限（核心）
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                //->接下来要定义 “访问某个接口需要满足什么条件”
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")  // /api/admin下的接口需要ADMIN角色
                        .requestMatchers(HttpMethod.GET, "/api/user/**").permitAll()  // GET方式访问/api/user/**允许匿名
                        .requestMatchers(HttpMethod.GET, "/api/feedback/**").permitAll()  // GET方式访问/api/feedback/**允许匿名
                        .requestMatchers(HttpMethod.POST, "/api/item").authenticated()  // POST /api/item需要登录（已认证）
                        .requestMatchers(HttpMethod.PUT, "/api/item/**").authenticated()  // PUT /api/item/**需要登录
                        .requestMatchers(HttpMethod.DELETE, "/api/item/**").authenticated()  // DELETE /api/item/**需要登录
                        .requestMatchers("/api/user/{id}").authenticated()  // 访问用户详情需要登录
                        .requestMatchers("/api/cart/{id}").authenticated()  // 访问购物车需要登录
                        .anyRequest().permitAll()  // 其他未匹配的接口允许匿名访问
                )
                // 3. 关闭CSRF（跨站请求伪造）保护，因前后端分离项目中，前端通常通过 token 认证，无需 CSRF 令牌。
                .csrf(AbstractHttpConfigurer::disable)
                
                // 4. 配置OAuth2资源服务器（基于JWT）
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );
        return http.build();
    }
//oauth2ResourceServer(...)：Spring Security 的方法，用于声明当前应用是 “OAuth2 资源服务器”。
//资源服务器的作用是：保护后端接口，只允许携带有效令牌（JWT）的请求访问，并根据令牌中的信息识别用户身份。
//oauth2 -> ...：lambda 表达式，通过oauth2这个配置对象（类型为OAuth2ResourceServerConfigurer）来细化资源服务器的配置。

//jwt(...)：指定资源服务器使用JWT（JSON Web Token） 作为令牌格式。
//意味着前端需要在请求头中携带Authorization: Bearer {jwt_token}，后端通过验证这个 JWT 来确认用户身份。
//jwt -> ...：lambda 表达式，通过jwt这个配置对象（类型为JwtConfigurer）来配置 JWT 的具体验证规则

//jwtAuthenticationConverter(...)：设置 JWT 的 “转换器”
// 用于将 JWT 令牌中的信息（如用户 ID、角色）转换为 Spring Security 能识别的 “认证对象”（Authentication）

//http.build()构建最终的SecurityFilterChain对象（Spring Security 的过滤器链），并返回。
//这个过滤器链会拦截所有 HTTP 请求，按照前面定义的规则（跨域、权限、JWT 验证等）进行处理。


// 例如，当前端发送GET /api/orders/123请求并携带 JWT 时：

// 1.过滤器链会拦截请求，检查是否有Bearer {jwt}。
// 2.调用jwtAuthenticationConverter解析 JWT，得到用户 ID 和角色（如ROLE_USER）。
// 3.根据.authorizeHttpRequests中定义的规则（如/api/orders/**需要authenticated()），判断该用户是否有权限访问。
// 4.验证通过则放行请求，否则返回 401（未认证）或 403（权限不足）。



    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        //Keycloak 生成的 JWT 中，角色信息通常以特定格式存储（如realm_access.roles字段）
        
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter
                (new KeycloakRoleConverter());
        // 从 JWT 中提取这些角色，并转换为 Spring Security 能识别的 “权限”（GrantedAuthority）。
        return jwtAuthenticationConverter;
    }
    //例如：Keycloak 的 JWT 中realm_access.roles: ["ADMIN"]
    //经转换后会成为 Spring Security 的ROLE_ADMIN权限，供hasRole("ADMIN")判断使用。

    @Bean
    public SimpleAuthorityMapper grantedAuthoritiesMapper() {
        SimpleAuthorityMapper mapper = new SimpleAuthorityMapper();
        mapper.setPrefix("ROLE_");
        mapper.setConvertToUpperCase(true);
        return mapper;
    }
    //统一权限格式。Spring Security 默认要求角色以ROLE_为前缀（如ROLE_ADMIN）
    //该配置确保从 JWT 中解析的角色（如ADMIN）会被自动添加前缀并转为大写
}