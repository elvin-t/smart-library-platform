package org.mariaelvin.library.analytics_service.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            String authorization = request.getHeader("Authorization");
            String traceId = request.getHeader("X-Trace-Id");

            if (authorization != null && !authorization.isBlank()) {
                template.header("Authorization", authorization);
            }

            if (traceId != null && !traceId.isBlank()) {
                template.header("X-Trace-Id", traceId);
            } else if (MDC.get("traceId") != null) {
                template.header("X-Trace-Id", MDC.get("traceId"));
            }

            Object userId = request.getAttribute("userId");
            Object email = request.getAttribute("email");
            Object roles = request.getAttribute("roles");
            Object permissions = request.getAttribute("permissions");

            if (userId != null) {
                template.header("X-User-Id", String.valueOf(userId));
            }

            if (email != null) {
                template.header("X-User-Email", String.valueOf(email));
            }

            if (roles instanceof List<?> roleList) {
                template.header("X-Roles", join(roleList));
            }

            if (permissions instanceof List<?> permissionList) {
                template.header("X-Permissions", join(permissionList));
            }
        };
    }

    private String join(List<?> values) {
        return values.stream()
                .map(String::valueOf)
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }
}