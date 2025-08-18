package com.example.NexusMart.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingCacheAspect {

    private final CacheManager cacheManager;

    @Autowired
    public LoggingCacheAspect(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Pointcut("execution(* com.example.NexusMart.service.ItemService.*(..)) && @annotation(cacheable)")
    public void cacheableMethods(Cacheable cacheable) {}

    @Before("cacheableMethods(cacheable)")
    public void logCacheableInvocation(Cacheable cacheable) {
        for (String cacheName : cacheable.value()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Object key = generateKey(cacheable, cacheName);
                if (cache.get(key) != null) {
                    log.info("Cache hit for cache: {}", cacheName);
                } else {
                    log.info("Cache miss for cache: {}", cacheName);
                }
            }
        }
    }

    @AfterReturning(pointcut = "cacheableMethods(cacheable)", returning = "result")
    public void logCacheableResult(Cacheable cacheable, Object result) {
        for (String cacheName : cacheable.value()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                log.info("Result cached in cache: {}", cacheName);
            }
        }
    }

    private Object generateKey(Cacheable cacheable, String cacheName) {
        // Implement key generation logic based on method parameters
        // This can be complex and might need reflection to get the method parameters and annotations
        // For simplicity, let's assume it's a simple string key
        return "generatedKeyBasedOnParameters";
    }
}
