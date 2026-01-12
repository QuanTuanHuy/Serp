/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket configuration for real-time messaging
 */

package serp.project.discuss_service.kernel.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import serp.project.discuss_service.kernel.websocket.WebSocketAuthChannelInterceptor;

/**
 * WebSocket configuration using STOMP protocol over WebSocket.
 * 
 * Endpoints:
 * - /ws/discuss - Main WebSocket endpoint for STOMP connections
 * 
 * Message destinations:
 * - /topic/channels/{channelId} - Subscribe to receive channel messages
 * - /topic/channels/{channelId}/typing - Subscribe to typing indicators
 * - /topic/users/{userId} - Subscribe to user-specific notifications
 * - /app/... - Application-destination prefix for sending messages
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory message broker for broadcasting messages
        // /topic - for broadcasting to multiple subscribers (channels, users)
        // /queue - for point-to-point messaging (user-specific notifications)
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages sent from clients to server
        registry.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific destinations (e.g., /user/{userId}/queue/...)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Main WebSocket endpoint
        registry.addEndpoint("/ws/discuss")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Enable SockJS fallback for browsers without WebSocket support
        
        // Raw WebSocket endpoint (without SockJS)
        registry.addEndpoint("/ws/discuss")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add authentication interceptor for incoming WebSocket messages
        registration.interceptors(authChannelInterceptor);
    }
}
