/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.kernel.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

@Component
public class InternalApiAuthenticationFilter extends OncePerRequestFilter {
    public static final String API_KEY_HEADER = "X-Internal-Api-Key";
    public static final String TENANT_ID_HEADER = "X-Tenant-Id";
    public static final String SERVICE_HEADER = "X-Internal-Service";
    private static final String INTERNAL_PATH_MARKER = "/internal/";

    private final String expectedApiKey;
    private final String roles;

    public InternalApiAuthenticationFilter(
            @Value("${internal-api.api-key:}") String expectedApiKey,
            @Value("${internal-api.roles:TMS_ADMIN,TMS_POSTOFFICER_MANAGER,TMS_POSTOFFICER,TMS_HUB_MANAGER,TMS_HUB_EMPLOYEE}") String roles
    ) {
        this.expectedApiKey = normalize(expectedApiKey);
        this.roles = roles;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String apiKey = normalize(request.getHeader(API_KEY_HEADER));
        if (apiKey == null) {
            if (requiresInternalApiKey(request)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "X-Internal-Api-Key is required.");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }
        if (!requiresInternalApiKey(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (expectedApiKey == null || !secureEquals(expectedApiKey, apiKey)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid internal API key.");
            return;
        }

        Long tenantId = parseTenantId(request.getHeader(TENANT_ID_HEADER));
        if (tenantId == null) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "X-Tenant-Id is required for internal API calls.");
            return;
        }

        String serviceName = normalize(request.getHeader(SERVICE_HEADER));
        var authorities = Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .toList();
        var authentication = new UsernamePasswordAuthenticationToken(
                serviceName == null ? "internal-service" : serviceName,
                "N/A",
                authorities
        );
        authentication.setDetails(new InternalApiAuthenticationDetails(tenantId, serviceName));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private boolean requiresInternalApiKey(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return requestUri != null && requestUri.contains(INTERNAL_PATH_MARKER);
    }

    private Long parseTenantId(String rawValue) {
        try {
            String normalized = normalize(rawValue);
            if (normalized == null) {
                return null;
            }
            Long tenantId = Long.valueOf(normalized);
            return tenantId > 0 ? tenantId : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean secureEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
