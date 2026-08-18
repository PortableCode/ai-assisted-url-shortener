package com.vishnu.urlshortener.link.api;

import com.vishnu.urlshortener.link.application.CreateLinkRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

@Component
public class CreateLinkRateLimitInterceptor implements HandlerInterceptor {

    private static final String CREATE_LINK_PATH = "/api/v1/links";

    private final CreateLinkRateLimiter rateLimiter;

    public CreateLinkRateLimitInterceptor(CreateLinkRateLimiter rateLimiter) {
        this.rateLimiter = Objects.requireNonNull(rateLimiter, "rateLimiter must not be null");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("POST".equals(request.getMethod()) && CREATE_LINK_PATH.equals(requestPath(request))) {
            rateLimiter.assertAllowed(request.getRemoteAddr());
        }
        return true;
    }

    private String requestPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }
}
