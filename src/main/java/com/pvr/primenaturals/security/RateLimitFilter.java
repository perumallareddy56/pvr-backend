package com.pvr.primenaturals.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple per-IP rate limiter using a sliding window counter.
 * Limits sensitive endpoints to MAX_REQUESTS per WINDOW_MS.
 * No external dependency — pure Java ConcurrentHashMap.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 60;         // requests per window
    private static final long WINDOW_MS = 60_000L;      // 1 minute window

    // Strict limits for auth & payment endpoints
    private static final int AUTH_MAX = 10;
    private static final long AUTH_WINDOW_MS = 60_000L;

    private record BucketEntry(AtomicInteger count, long windowStart) {}

    private final Map<String, BucketEntry> generalBuckets = new ConcurrentHashMap<>();
    private final Map<String, BucketEntry> authBuckets   = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String ip = getClientIp(request);
        String uri = request.getRequestURI();

        boolean isAuthEndpoint = uri.contains("/api/auth/") || uri.contains("/api/orders/payment");
        boolean throttled = isAuthEndpoint
                ? isThrottled(ip, authBuckets, AUTH_MAX, AUTH_WINDOW_MS)
                : isThrottled(ip, generalBuckets, MAX_REQUESTS, WINDOW_MS);

        if (throttled) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please slow down.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isThrottled(String ip, Map<String, BucketEntry> buckets, int max, long windowMs) {
        long now = System.currentTimeMillis();
        BucketEntry entry = buckets.compute(ip, (key, existing) -> {
            if (existing == null || (now - existing.windowStart()) > windowMs) {
                return new BucketEntry(new AtomicInteger(1), now);
            }
            return existing;
        });
        int count = entry.count().getAndIncrement();
        return count >= max;
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
