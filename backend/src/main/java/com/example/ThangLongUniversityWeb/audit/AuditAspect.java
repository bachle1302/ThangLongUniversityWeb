package com.example.ThangLongUniversityWeb.audit;

import com.example.ThangLongUniversityWeb.entity.AuditLog;
import com.example.ThangLongUniversityWeb.repository.AuditLogRepository;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint pjp, Audit audit) throws Throwable {
        Object result = null;
        Throwable error = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            saveAudit(pjp, audit, error);
        }
    }

    private void saveAudit(ProceedingJoinPoint pjp, Audit audit, Throwable error) {
        Long userId = null;
        String username = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName();
        }
        if (username != null) {
            userId = userRepository.findByUsername(username).map(u -> u.getId()).orElse(null);
        }

        String ip = null;
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            ip = req.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) ip = req.getRemoteAddr();
        }

        String metadata = "args=" + Arrays.toString(pjp.getArgs());
        if (error != null) {
            metadata += "; error=" + error.getClass().getSimpleName() + ":" + error.getMessage();
        }
        if (metadata.length() > 2000) metadata = metadata.substring(0, 2000);

        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(audit.action());
        log.setTargetType(audit.targetType().isBlank() ? null : audit.targetType());
        log.setTargetId(null);
        log.setIp(ip);
        log.setMetadata(metadata);
        log.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(log);
    }
}

