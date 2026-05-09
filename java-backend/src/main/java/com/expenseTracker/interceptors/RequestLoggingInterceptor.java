package com.expenseTracker.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * HTTP Interceptor for request/response logging and correlation tracking
 */
@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final String REQUEST_START_TIME = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(REQUEST_START_TIME, startTime);

        // Generate or extract correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        // Add correlation ID to response header
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        // Log request details
        log.info("Incoming Request | Method: {} | URI: {} | CorrelationId: {} | IP: {}",
                request.getMethod(),
                request.getRequestURI(),
                correlationId,
                getClientIp(request));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        long duration = System.currentTimeMillis() - startTime;
        String correlationId = response.getHeader(CORRELATION_ID_HEADER);

        // Log response details
        log.info("Outgoing Response | Method: {} | URI: {} | Status: {} | Duration: {}ms | CorrelationId: {}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration,
                correlationId);

        // Log exceptions if present
        if (ex != null) {
            log.error("Request failed with exception | CorrelationId: {} | Error: {}",
                    correlationId,
                    ex.getMessage(),
                    ex);
        }
    }

    /**
     * Extract client IP from request headers
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
