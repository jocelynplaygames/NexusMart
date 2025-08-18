package com.example.NexusMart.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Slf4j
public class PerformanceLoggingAspect {

    @Pointcut("execution(* com.example.NexusMart.controller.ItemController.searchItems(..))")
    public void searchItemsMethod() {}

    @Around("searchItemsMethod()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("{} executed in {} ms", joinPoint.getSignature(), duration);
        }
    }
}

