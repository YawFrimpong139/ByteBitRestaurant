package org.codewithzea.restaurantservice.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* org.codewithzea.restaurantservice.controller..*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        MDC.put("method", className + "." + methodName);
        log.info("Entering method with args: {}", args);

        try {
            Object result = joinPoint.proceed();
            log.info("Method completed successfully");
            return result;
        } catch (Exception e) {
            log.error("Method failed: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}