package org.codewithzea.authservice.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    public void logBeforeController(JoinPoint joinPoint) {
        String user = SecurityContextHolder.getContext().getAuthentication() != null ?
                SecurityContextHolder.getContext().getAuthentication().getName() :
                "ANONYMOUS";

        AuditLog log = new AuditLog();
        log.setAction("API_REQUEST");
        log.setEndpoint(request.getRequestURI());
        log.setMethod(request.getMethod());
        log.setIpAddress(request.getRemoteAddr());
        log.setPerformedBy(user);
        log.setPerformedAt(Instant.now());

        auditLogRepository.save(log);
    }
}
