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

import serp.project.discuss_service.kernel.property.WebsocketProperties;
import serp.project.discuss_service.kernel.websocket.WebSocketAuthChannelInterceptor;

/**
 * WebSocket configuration using STOMP protocol over WebSocket with server-side fan-out.
 * <p>
 * Endpoint:
 * - /ws/discuss - Main WebSocket endpoint for STOMP connections
 * <p>
 * Client subscribes to:
 * - /user/queue/events - Single personal queue for ALL events (messages, reactions, typing, presence)
 * <p>
 * Client sends to:
 * - /app/channels/{channelId}/message - Send a new message
 * - /app/channels/{channelId}/typing - Send typing indicator
 * - /app/channels/{channelId}/read - Mark messages as read
 * <p>
 * Server-side fan-out:
 * When a message is sent to a channel, the server looks up channel members,
 * checks online status, and delivers the event to each online user's personal queue.
 * Clients do NOT subscribe to per-channel topics.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebsocketProperties websocketProperties;

    private final WebSocketAuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory message broker
        // /queue - for point-to-point messaging (user-specific event delivery and errors)
        registry.enableSimpleBroker("/queue");
        
        // Prefix for messages sent from clients to server
        registry.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific destinations (e.g., /user/{userId}/queue/events)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Main WebSocket endpoint
        registry.addEndpoint(websocketProperties.getEndpoint())
                .setAllowedOrigins(websocketProperties.getAllowedOrigins())
                .withSockJS(); // Enable SockJS fallback for browsers without WebSocket support
        
        // Raw WebSocket endpoint (without SockJS)
        registry.addEndpoint(websocketProperties.getEndpoint())
                .setAllowedOrigins(websocketProperties.getAllowedOrigins());
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add authentication interceptor for incoming WebSocket messages
        registration.interceptors(authChannelInterceptor);
    }
}
