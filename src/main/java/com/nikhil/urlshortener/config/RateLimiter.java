package com.nikhil.urlshortener.config;

import com.nikhil.urlshortener.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiter {

    private final int maxRequests;
    private final long windowMs;
    private final Map<String, RateWindow> requestCounts = new ConcurrentHashMap<>();

    public RateLimiter(
        @Value("${app.rate-limit.max-requests}") int maxRequests,
        @Value("${app.rate-limit.window-minutes}") int windowMinutes
    ) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMinutes * 60 * 1000L;
    }

    public void checkRateLimit(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        long now = System.currentTimeMillis();

        requestCounts.compute(clientIp, (key, window) -> {
            if (window == null || now - window.startTime > windowMs) {
                return new RateWindow(now, new AtomicInteger(1));
            }
            if (window.count.incrementAndGet() > maxRequests) {
                throw new RateLimitExceededException();
            }
            return window;
        });
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    private record RateWindow(long startTime, AtomicInteger count) {}
}
