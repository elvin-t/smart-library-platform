package org.mariaelvin.library.auth_service.config;

import feign.Logger;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignClientConfig {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {

            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                String traceId = request.getHeader(TRACE_ID_HEADER);
                if (traceId != null && !traceId.isBlank()) {
                    requestTemplate.header(TRACE_ID_HEADER, traceId);
                    return;
                }
            }

            String mdcTraceId = MDC.get(TRACE_ID_MDC_KEY);
            if (mdcTraceId != null && !mdcTraceId.isBlank()) {
                requestTemplate.header(TRACE_ID_HEADER, mdcTraceId);
            }
        };
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}