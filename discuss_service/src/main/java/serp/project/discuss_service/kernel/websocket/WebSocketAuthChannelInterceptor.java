/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket authentication channel interceptor
 */

package serp.project.discuss_service.kernel.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

/**
 * Channel interceptor for authenticating WebSocket STOMP connections.
 * Extracts JWT token from STOMP headers and validates it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            handleConnect(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader(AUTHORIZATION_HEADER);
        
        if (authHeaders == null || authHeaders.isEmpty()) {
            log.debug("No Authorization header found in WebSocket CONNECT");
            return;
        }

        String authHeader = authHeaders.get(0);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Invalid Authorization header format in WebSocket CONNECT");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String userId = extractUserId(jwt);
            String tenantId = extractTenantId(jwt);
            
            // Create a principal with user info
            WebSocketPrincipal principal = new WebSocketPrincipal(userId, tenantId, jwt);
            accessor.setUser(principal);
            
            log.debug("WebSocket user authenticated: userId={}, tenantId={}", userId, tenantId);
        } catch (Exception e) {
            log.warn("Failed to authenticate WebSocket connection: {}", e.getMessage());
        }
    }

    private String extractUserId(Jwt jwt) {
        // Try common claim names for user ID
        Object userId = jwt.getClaim("user_id");
        if (userId == null) {
            userId = jwt.getClaim("sub");
        }
        return userId != null ? userId.toString() : null;
    }

    private String extractTenantId(Jwt jwt) {
        Object tenantId = jwt.getClaim("tenant_id");
        if (tenantId == null) {
            tenantId = jwt.getClaim("tenantId");
        }
        return tenantId != null ? tenantId.toString() : null;
    }

    /**
     * Custom Principal implementation for WebSocket connections
     */
    public static class WebSocketPrincipal implements Principal {
        private final String userId;
        private final String tenantId;
        private final Jwt jwt;

        public WebSocketPrincipal(String userId, String tenantId, Jwt jwt) {
            this.userId = userId;
            this.tenantId = tenantId;
            this.jwt = jwt;
        }

        @Override
        public String getName() {
            return userId;
        }

        public String getUserId() {
            return userId;
        }

        public Long getUserIdAsLong() {
            try {
                return userId != null ? Long.parseLong(userId) : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        public String getTenantId() {
            return tenantId;
        }

        public Long getTenantIdAsLong() {
            try {
                return tenantId != null ? Long.parseLong(tenantId) : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        public Jwt getJwt() {
            return jwt;
        }
    }
}
