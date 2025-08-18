package com.example.NexusMart.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
//被OrderApplication.java中的@EnableJpaAuditing调用
@Component("auditAwareImpl")
//将当前类注册为 Spring Bean，名称明确指定为auditAwareImpl
//这个名称与OrderApplication中@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")的配置完全对应
public class AuditAwareImpl implements AuditorAware<String> {//作用是返回当前操作用户的标识（这里是用户名）
// AuditorAware<String> 是 Spring Data JPA 提供的一个接口，专门用于获取当前操作数据的用户标识（即 “审计者” 信息）
// 泛型 <String> 表示这个用户标识的数据类型是字符串（比如用户名、用户 ID 字符串等）。
    
    @Override
    public Optional<String> getCurrentAuditor() {
            return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getName());
    }
// SecurityContextHolder.getContext()：获取 Spring Security 的上下文对象
// .getAuthentication()：从上下文获取认证信息对象（包含登录用户信息）
// .getName()：从认证信息中获取用户名（登录时输入的用户名）
// Optional.ofNullable(...)：将可能为 null 的用户名包装成 Optional 对象，避免空指针异常
}
// 因为AuditorAware接口的getCurrentAuditor()方法强制要求返回Optional<T>类型，这是 JPA 的规范。
// 这样设计的目的是：明确告诉框架 “当前用户可能不存在”（比如匿名访问），框架会根据是否有值来处理@CreatedBy等字段（例如匿名访问时可能填入"anonymous"）。
