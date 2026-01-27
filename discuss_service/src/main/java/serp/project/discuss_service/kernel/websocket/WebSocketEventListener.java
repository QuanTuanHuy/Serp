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
import serp.project.discuss_service.core.service.IDiscussEventPublisher;

import java.security.Principal;

/**
 * Event listener for WebSocket session lifecycle events.
 * Handles user connection, disconnection, and subscription tracking.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final WebSocketSessionRegistry sessionRegistry;
    private final IDiscussEventPublisher eventPublisher;

    private static final String CHANNEL_TOPIC_PREFIX = "/topic/channels/";

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal instanceof WebSocketAuthChannelInterceptor.WebSocketPrincipal wsUser) {
            Long userId = wsUser.getUserIdAsLong();
            String sessionId = getSessionId(event);
            
            if (userId != null && sessionId != null) {
                boolean wasOffline = !sessionRegistry.isUserConnected(userId);
                sessionRegistry.registerSession(userId, sessionId);
                
                if (wasOffline) {
                    eventPublisher.publishUserOnline(userId);
                    log.info("User {} connected (session: {})", userId, sessionId);
                } else {
                    log.debug("User {} connected with additional session: {}", userId, sessionId);
                }
            }
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        Long userId = sessionRegistry.getUserIdBySessionId(sessionId);
        
        if (userId != null) {
            sessionRegistry.unregisterSession(sessionId);
            
            if (!sessionRegistry.isUserConnected(userId)) {
                eventPublisher.publishUserOffline(userId);
                log.info("User {} disconnected (last session closed)", userId);
            } else {
                log.debug("User {} disconnected session: {} (still has other sessions)", userId, sessionId);
            }
        }
    }

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        Principal principal = event.getUser();
        String destination = getDestination(event);
        
        if (principal instanceof WebSocketAuthChannelInterceptor.WebSocketPrincipal wsUser && destination != null) {
            Long userId = wsUser.getUserIdAsLong();
            
            // Check if subscribing to a channel topic
            if (destination.startsWith(CHANNEL_TOPIC_PREFIX)) {
                Long channelId = extractChannelId(destination);
                if (userId != null && channelId != null) {
                    sessionRegistry.subscribeToChannel(userId, channelId);
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
            
            // Check if unsubscribing from a channel topic
            if (destination.startsWith(CHANNEL_TOPIC_PREFIX)) {
                Long channelId = extractChannelId(destination);
                if (userId != null && channelId != null) {
                    sessionRegistry.unsubscribeFromChannel(userId, channelId);
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
