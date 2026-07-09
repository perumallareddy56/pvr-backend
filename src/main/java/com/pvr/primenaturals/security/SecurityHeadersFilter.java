package com.pvr.primenaturals.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adds hardening HTTP security headers to every API response.
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // Prevent MIME-type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Clickjacking protection
        response.setHeader("X-Frame-Options", "DENY");

        // XSS protection (legacy browsers)
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // HTTPS enforcement (1 year)
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Referrer policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Basic Content-Security-Policy
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' https://checkout.razorpay.com; " +
                "frame-src https://api.razorpay.com; " +
                "connect-src 'self' https://api.razorpay.com wss:; " +
                "img-src 'self' data: https:; " +
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                "font-src 'self' https://fonts.gstatic.com;");

        // Disable browser caching for API responses
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");

        chain.doFilter(request, response);
    }
}
