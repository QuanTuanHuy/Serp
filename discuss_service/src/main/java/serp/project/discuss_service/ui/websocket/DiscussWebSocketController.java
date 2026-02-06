/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket controller for real-time messaging
 */

package serp.project.discuss_service.ui.websocket;

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
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.port.client.IWebSocketHubPort;
import serp.project.discuss_service.core.usecase.MessageUseCase;
import serp.project.discuss_service.kernel.websocket.WebSocketAuthChannelInterceptor;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

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
    private final IWebSocketHubPort webSocketHubPort;

    /**
     * Handle sending a new message via WebSocket
     * Client sends to: /app/channels/{channelId}/message
     * Errors are sent back to /user/queue/events with ERROR event type
     */
    @MessageMapping("/channels/{channelId}/message")
    public void handleMessage(
            @DestinationVariable Long channelId,
            @Payload SendMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Principal principal = headerAccessor.getUser();
        if (!(principal instanceof WebSocketAuthChannelInterceptor.WebSocketPrincipal wsUser)) {
            log.warn("WebSocket message rejected: user not authenticated");
            sendErrorToUser(principal, ErrorCode.UNAUTHORIZED.toString(), "User not authenticated", null);
            return;
        }
        
        Long userId = wsUser.getUserIdAsLong();
        Long tenantId = wsUser.getTenantIdAsLong();
        
        if (userId == null || tenantId == null) {
            log.warn("WebSocket message rejected: missing userId or tenantId");
            sendErrorToUser(principal, ErrorCode.UNAUTHORIZED.toString(), "Missing authentication credentials", null);
            return;
        }
        
        try {
            messageUseCase.sendMessage(channelId, userId, tenantId, 
                    request.getContent(), request.getMentions());
            log.debug("Message sent via WebSocket to channel {} by user {}", channelId, userId);
        } catch (AppException e) {
            log.error("Failed to send message via WebSocket - Code: {}, Message: {}", e.getCode(), e.getMessage());
            sendErrorToUser(principal, e.getErrorCode(), e.getMessage(), channelId);
        } catch (Exception e) {
            log.error("Failed to send message via WebSocket: {}", e.getMessage(), e);
            sendErrorToUser(principal, ErrorCode.INTERNAL_SERVER_ERROR.toString(), "Failed to send message. Please try again.", channelId);
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
            messageUseCase.sendTypingIndicator(channelId, userId, isTyping);
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

    /**
     * Send error event to user's personal events queue as a WsEvent with ERROR type.
     * Client receives errors on the same /user/queue/events destination.
     */
    private void sendErrorToUser(Principal principal, String code, String message, Long channelId) {
        if (principal == null) {
            log.warn("Cannot send error to user: principal is null");
            return;
        }

        Map<String, Object> errorPayload = new HashMap<>();
        errorPayload.put("code", code);
        errorPayload.put("message", message);
        if (channelId != null) {
            errorPayload.put("channelId", channelId);
        }

        WsEvent<Map<String, Object>> errorEvent = WsEvent.of(
                WsEventType.ERROR,
                errorPayload,
                channelId
        );

        try {
            webSocketHubPort.sendErrorToUser(
                    Long.valueOf(principal.getName()),
                    errorEvent
            );
            log.debug("Sent error to user {}: {} - {}", principal.getName(), code, message);
        } catch (Exception e) {
            log.error("Failed to send error message to user: {}", e.getMessage(), e);
        }
    }
}
