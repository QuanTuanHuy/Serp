/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket controller for real-time messaging
 */

package serp.project.discuss_service.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import serp.project.discuss_service.core.domain.dto.request.MarkAsReadRequest;
import serp.project.discuss_service.core.domain.dto.request.SendMessageRequest;
import serp.project.discuss_service.core.domain.dto.request.TypingIndicatorRequest;
import serp.project.discuss_service.core.port.client.IWebSocketHubPort;
import serp.project.discuss_service.core.usecase.MessageUseCase;
import serp.project.discuss_service.kernel.websocket.WebSocketAuthChannelInterceptor;

import java.security.Principal;

/**
 * WebSocket controller handling real-time messaging operations via STOMP.
 * 
 * Message mappings:
 * - /app/channels/{channelId}/message - Send a new message
 * - /app/channels/{channelId}/typing - Send typing indicator
 * - /app/channels/{channelId}/read - Mark messages as read
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class DiscussWebSocketController {

    private final MessageUseCase messageUseCase;
    private final IWebSocketHubPort webSocketHub;

    /**
     * Handle sending a new message via WebSocket
     * Client sends to: /app/channels/{channelId}/message
     */
    @MessageMapping("/channels/{channelId}/message")
    public void handleMessage(
            @DestinationVariable Long channelId,
            @Payload SendMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Principal principal = headerAccessor.getUser();
        if (principal instanceof WebSocketAuthChannelInterceptor.WebSocketPrincipal wsUser) {
            Long userId = wsUser.getUserIdAsLong();
            Long tenantId = wsUser.getTenantIdAsLong();
            
            if (userId == null || tenantId == null) {
                log.warn("WebSocket message rejected: missing userId or tenantId");
                return;
            }
            
            try {
                messageUseCase.sendMessage(channelId, userId, tenantId, 
                        request.getContent(), request.getMentions());
                log.debug("Message sent via WebSocket to channel {} by user {}", channelId, userId);
            } catch (Exception e) {
                log.error("Failed to send message via WebSocket: {}", e.getMessage());
                // Could send error back to user's queue here
            }
        } else {
            log.warn("WebSocket message rejected: user not authenticated");
        }
    }

    /**
     * Handle typing indicator via WebSocket
     * Client sends to: /app/channels/{channelId}/typing
     */
    @MessageMapping("/channels/{channelId}/typing")
    public void handleTyping(
            @DestinationVariable Long channelId,
            @Payload TypingIndicatorRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Principal principal = headerAccessor.getUser();
        if (principal instanceof WebSocketAuthChannelInterceptor.WebSocketPrincipal wsUser) {
            Long userId = wsUser.getUserIdAsLong();
            
            if (userId == null) {
                log.warn("WebSocket typing indicator rejected: missing userId");
                return;
            }
            
            boolean isTyping = Boolean.TRUE.equals(request.getIsTyping());
            webSocketHub.notifyTyping(channelId, userId, isTyping);
            log.debug("Typing indicator {} for user {} in channel {}", 
                    isTyping ? "started" : "stopped", userId, channelId);
        }
    }

    /**
     * Handle mark as read via WebSocket
     * Client sends to: /app/channels/{channelId}/read
     */
    @MessageMapping("/channels/{channelId}/read")
    public void handleMarkAsRead(
            @DestinationVariable Long channelId,
            @Payload MarkAsReadRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Principal principal = headerAccessor.getUser();
        if (principal instanceof WebSocketAuthChannelInterceptor.WebSocketPrincipal wsUser) {
            Long userId = wsUser.getUserIdAsLong();
            Long tenantId = wsUser.getTenantIdAsLong();
            
            if (userId == null || tenantId == null) {
                log.warn("WebSocket mark as read rejected: missing userId or tenantId");
                return;
            }
            
            try {
                messageUseCase.markAsRead(channelId, userId, request.getMessageId());
                log.debug("Messages marked as read in channel {} by user {}", channelId, userId);
            } catch (Exception e) {
                log.error("Failed to mark messages as read via WebSocket: {}", e.getMessage());
            }
        }
    }
}
