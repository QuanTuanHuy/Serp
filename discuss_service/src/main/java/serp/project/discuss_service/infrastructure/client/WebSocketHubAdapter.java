/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket hub adapter implementation
 */

package serp.project.discuss_service.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.response.MessageResponse;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.domain.dto.websocket.WsMessagePayload;
import serp.project.discuss_service.core.domain.dto.websocket.WsPresencePayload;
import serp.project.discuss_service.core.domain.dto.websocket.WsReactionPayload;
import serp.project.discuss_service.core.domain.dto.websocket.WsTypingPayload;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.port.client.IWebSocketHubPort;
import serp.project.discuss_service.core.service.IAttachmentUrlService;
import serp.project.discuss_service.core.service.IPresenceService;
import serp.project.discuss_service.core.service.IUserInfoService;

import java.util.Set;

/**
 * WebSocket hub adapter for real-time message delivery.
 * Implements the IWebSocketHubPort interface for sending messages via WebSocket.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHubAdapter implements IWebSocketHubPort {

    private final SimpMessagingTemplate messagingTemplate;
    private final IPresenceService presenceService;
    private final IAttachmentUrlService attachmentUrlService;
    private final IUserInfoService userInfoService;

    // Topic destinations
    private static final String CHANNEL_TOPIC = "/topic/channels/%d";
    private static final String CHANNEL_TYPING_TOPIC = "/topic/channels/%d/typing";
    private static final String USER_QUEUE = "/queue/notifications";
    private static final String USER_ERROR_QUEUE = "/user/queue/errors";

    @Override
    public void broadcastToChannel(Long channelId, Object payload) {
        if (channelId == null || payload == null) {
            log.warn("Cannot broadcast to channel with null channelId or payload");
            return;
        }
        String destination = String.format(CHANNEL_TOPIC, channelId);
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("Broadcast to channel {} at {}", channelId, destination);
    }

    @Override
    public void sendToUser(Long userId, Object payload) {
        if (userId == null || payload == null) {
            log.warn("Cannot send message to user with null userId or payload");
            return;
        }
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                USER_QUEUE,
                payload
        );
        log.debug("Sent message to user {} queue", userId);
    }

    @Override
    public void sendErrorToUser(Long userId, Object payload) {
        if (userId == null || payload == null) {
            log.warn("Cannot send error message to user with null userId or payload");
            return;
        }
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                USER_ERROR_QUEUE,
                payload
        );
        log.debug("Sent error message to user {} error queue", userId);
    }

    @Override
    public void sendToUsers(Set<Long> userIds, Object payload) {
        for (Long userId : userIds) {
            sendToUser(userId, payload);
        }
        log.debug("Sent message to {} users", userIds.size());
    }

    @Override
    public void subscribeUserToChannel(Long userId, Long channelId) {
        presenceService.userJoinedChannel(userId, channelId);
        log.debug("User {} subscribed to channel {}", userId, channelId);
    }

    @Override
    public void unsubscribeUserFromChannel(Long userId, Long channelId) {
        presenceService.userLeftChannel(userId, channelId);
        log.debug("User {} unsubscribed from channel {}", userId, channelId);
    }

    @Override
    public Set<Long> getChannelSubscribers(Long channelId) {
        return presenceService.getOnlineChannelSubscribers(channelId);
    }

    @Override
    public void notifyTyping(Long channelId, Long userId, boolean isTyping) {
        if (channelId == null || userId == null) {
            log.warn("Cannot notify typing with null channelId or userId");
            return;
        }
        String destination = String.format(CHANNEL_TYPING_TOPIC, channelId);
        
        WsTypingPayload payload = isTyping 
                ? WsTypingPayload.start(channelId, userId, null) 
                : WsTypingPayload.stop(channelId, userId);
        
        WsEvent<WsTypingPayload> event = WsEvent.of(
                isTyping ? WsEventType.TYPING_START : WsEventType.TYPING_STOP,
                payload,
                channelId
        );
        
        messagingTemplate.convertAndSend(destination, event);
        log.debug("Typing {} notification sent for user {} in channel {}", 
                isTyping ? "start" : "stop", userId, channelId);
    }

    @Override
    public void notifyNewMessage(Long channelId, MessageEntity message) {
        if (channelId == null || message == null) {
            log.warn("Cannot notify new message with null channelId or message");
            return;
        }
        MessageResponse enrichedMessage = attachmentUrlService.enrichMessageWithUrls(message);
        enrichedMessage = userInfoService.enrichMessageWithUserInfo(enrichedMessage);
        WsMessagePayload payload = WsMessagePayload.builder()
                .messageId(message.getId())
                .channelId(message.getChannelId())
                .senderId(message.getSenderId())
                .message(enrichedMessage)
                .build();
        WsEvent<WsMessagePayload> event = WsEvent.of(
                WsEventType.MESSAGE_NEW,
                payload,
                channelId
        );
        
        broadcastToChannel(channelId, event);
        log.debug("New message notification sent for message {} in channel {}", 
                message.getId(), channelId);
    }

    @Override
    public void notifyMessageUpdated(Long channelId, MessageEntity message) {
        if (channelId == null || message == null) {
            log.warn("Cannot notify message updated with null channelId or message");
            return;
        }
        MessageResponse enrichedMessage = attachmentUrlService.enrichMessageWithUrls(message);
        enrichedMessage = userInfoService.enrichMessageWithUserInfo(enrichedMessage);
        WsMessagePayload payload = WsMessagePayload.builder()
                .messageId(message.getId())
                .channelId(message.getChannelId())
                .senderId(message.getSenderId())
                .message(enrichedMessage)
                .build();
        WsEvent<WsMessagePayload> event = WsEvent.of(
                WsEventType.MESSAGE_UPDATED,
                payload,
                channelId
        );
        
        broadcastToChannel(channelId, event);
        log.debug("Message updated notification sent for message {} in channel {}", 
                message.getId(), channelId);
    }

    @Override
    public void notifyMessageDeleted(Long channelId, Long messageId) {
        if (channelId == null || messageId == null) {
            log.warn("Cannot notify message deleted with null channelId or messageId");
            return;
        }
        WsMessagePayload payload = WsMessagePayload.forDeletion(messageId, channelId);
        WsEvent<WsMessagePayload> event = WsEvent.of(
                WsEventType.MESSAGE_DELETED,
                payload,
                channelId
        );
        
        broadcastToChannel(channelId, event);
        log.debug("Message deleted notification sent for message {} in channel {}", 
                messageId, channelId);
    }

    @Override
    public void notifyReaction(Long channelId, Long messageId, Long userId, String emoji, boolean added) {
        if (channelId == null || messageId == null || userId == null || emoji == null) {
            log.warn("Cannot notify reaction with null channelId, messageId, userId or emoji");
            return;
        }
        WsReactionPayload payload = added
                ? WsReactionPayload.added(messageId, channelId, userId, emoji)
                : WsReactionPayload.removed(messageId, channelId, userId, emoji);
        
        WsEvent<WsReactionPayload> event = WsEvent.of(
                added ? WsEventType.REACTION_ADDED : WsEventType.REACTION_REMOVED,
                payload,
                channelId
        );
        
        broadcastToChannel(channelId, event);
        log.debug("Reaction {} notification sent for message {} in channel {}", 
                added ? "added" : "removed", messageId, channelId);
    }

    @Override
    public void notifyPresenceChange(Long channelId, Long userId, boolean online) {
        if (channelId == null || userId == null) {
            log.warn("Cannot notify presence change with null channelId or userId");
            return;
        }
        WsPresencePayload payload = online
                ? WsPresencePayload.online(userId, null, null, channelId) // TODO: add userName and avatarUrl
                : WsPresencePayload.offline(userId, channelId);
        
        WsEvent<WsPresencePayload> event = WsEvent.of(
                WsEventType.USER_PRESENCE_CHANGED,
                payload,
                channelId
        );
        
        broadcastToChannel(channelId, event);
        log.debug("Presence {} notification sent for user {} in channel {}", 
                online ? "online" : "offline", userId, channelId);
    }
}
