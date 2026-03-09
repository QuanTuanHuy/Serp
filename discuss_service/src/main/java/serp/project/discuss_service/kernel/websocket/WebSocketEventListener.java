/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket event listener for connection/disconnection tracking
 */

package serp.project.discuss_service.kernel.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import serp.project.discuss_service.core.service.IPresenceService;

import java.security.Principal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final IPresenceService presenceService;

    private static final String INSTANCE_ID = UUID.randomUUID().toString().substring(0, 8);

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal instanceof WebSocketAuthChannelInterceptor.WebSocketPrincipal wsUser) {
            Long userId = wsUser.getUserIdAsLong();
            Long tenantId = wsUser.getTenantIdAsLong();
            String sessionId = getSessionId(event);

            if (userId != null && sessionId != null) {
                presenceService.registerSession(userId, tenantId, sessionId, INSTANCE_ID);
            }
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        String sessionId = event.getSessionId();
        if (principal instanceof WebSocketAuthChannelInterceptor.WebSocketPrincipal wsUser) {
            Long userId = wsUser.getUserIdAsLong();
            if (userId != null) {
                presenceService.unregisterSession(userId, sessionId);
                return;
            }
        }
        presenceService.unregisterSessionBySessionId(sessionId);
    }

    private String getSessionId(SessionConnectedEvent event) {
        return event.getMessage().getHeaders().get("simpSessionId", String.class);
    }
}
