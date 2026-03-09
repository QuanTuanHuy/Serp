/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.core.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class CorrelationIdFilter extends OncePerRequestFilter {
    private static final String MDC_KEY = "correlationId";

    private final String correlationIdHeader;

    public CorrelationIdFilter(String correlationIdHeader) {
        this.correlationIdHeader = correlationIdHeader;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = request.getHeader(correlationIdHeader);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        request.setAttribute(correlationIdHeader, correlationId);
        response.setHeader(correlationIdHeader, correlationId);

        MDC.put(MDC_KEY, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
