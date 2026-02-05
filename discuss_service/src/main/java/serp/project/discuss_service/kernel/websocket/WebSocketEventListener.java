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
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import serp.project.discuss_service.core.service.IPresenceService;

import java.security.Principal;
import java.util.UUID;

/**
 * Event listener for WebSocket session lifecycle events.
 * Handles user connection, disconnection, and subscription tracking.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final IPresenceService presenceService;

    private static final String CHANNEL_TOPIC_PREFIX = "/topic/channels/";

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

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        Principal principal = event.getUser();
        String destination = getDestination(event);
        
        if (principal instanceof WebSocketAuthChannelInterceptor.WebSocketPrincipal wsUser && destination != null) {
            Long userId = wsUser.getUserIdAsLong();
            
            if (destination.startsWith(CHANNEL_TOPIC_PREFIX)) {
                Long channelId = extractChannelId(destination);
                if (userId != null && channelId != null) {
                    presenceService.userJoinedChannel(userId, channelId);
                    log.debug("User {} subscribed to channel {}", userId, channelId);
                }
            }
        }
    }

    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        Principal principal = event.getUser();
        String destination = getDestination(event);
        
        if (principal instanceof WebSocketAuthChannelInterceptor.WebSocketPrincipal wsUser && destination != null) {
            Long userId = wsUser.getUserIdAsLong();
            
            if (destination.startsWith(CHANNEL_TOPIC_PREFIX)) {
                Long channelId = extractChannelId(destination);
                if (userId != null && channelId != null) {
                    presenceService.userLeftChannel(userId, channelId);
                    log.debug("User {} unsubscribed from channel {}", userId, channelId);
                }
            }
        }
    }

    private String getSessionId(SessionConnectedEvent event) {
        return event.getMessage().getHeaders().get("simpSessionId", String.class);
    }

    private String getDestination(SessionSubscribeEvent event) {
        return event.getMessage().getHeaders().get("simpDestination", String.class);
    }

    private String getDestination(SessionUnsubscribeEvent event) {
        return event.getMessage().getHeaders().get("simpDestination", String.class);
    }

    private Long extractChannelId(String destination) {
        try {
            // Extract channel ID from paths like /topic/channels/123 or /topic/channels/123/typing
            String path = destination.substring(CHANNEL_TOPIC_PREFIX.length());
            String channelIdStr = path.split("/")[0];
            return Long.parseLong(channelIdStr);
        } catch (Exception e) {
            log.warn("Failed to extract channel ID from destination: {}", destination);
            return null;
        }
    }
}
