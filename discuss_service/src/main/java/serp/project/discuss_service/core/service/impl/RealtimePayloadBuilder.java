/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Builder for ready-to-deliver realtime events
 */

package serp.project.discuss_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.response.MessageResponse;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.domain.dto.websocket.WsMessagePayload;
import serp.project.discuss_service.core.domain.dto.websocket.WsMessageReadPayload;
import serp.project.discuss_service.core.domain.dto.websocket.WsReactionPayload;
import serp.project.discuss_service.core.domain.dto.websocket.WsTypingPayload;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.service.IAttachmentUrlService;
import serp.project.discuss_service.core.service.IUserInfoService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RealtimePayloadBuilder {

    private final IAttachmentUrlService attachmentUrlService;
    private final IUserInfoService userInfoService;

    public WsEvent<WsMessagePayload> buildMessageNew(MessageEntity message) {
        return buildMessageEvent(WsEventType.MESSAGE_NEW, message);
    }

    public WsEvent<WsMessagePayload> buildMessageUpdated(MessageEntity message) {
        return buildMessageEvent(WsEventType.MESSAGE_UPDATED, message);
    }

    public WsEvent<WsMessagePayload> buildMessageDeleted(MessageEntity message) {
        WsMessagePayload payload = WsMessagePayload.forDeletion(message.getId(), message.getChannelId());
        return WsEvent.of(WsEventType.MESSAGE_DELETED, payload, message.getChannelId());
    }

    public WsEvent<WsMessageReadPayload> buildMessageRead(
            Long channelId,
            Long messageId,
            Long userId,
            List<Long> readBy,
            Integer readCount) {
        WsMessageReadPayload payload = WsMessageReadPayload.builder()
                .channelId(channelId)
                .messageId(messageId)
                .userId(userId)
                .readBy(readBy == null ? List.of() : readBy)
                .readCount(readCount == null ? 0 : readCount)
                .build();
        return WsEvent.of(WsEventType.MESSAGE_READ, payload, channelId);
    }

    public WsEvent<WsReactionPayload> buildReaction(
            Long channelId,
            Long messageId,
            Long userId,
            String emoji,
            boolean added) {
        WsReactionPayload payload = added
                ? WsReactionPayload.added(messageId, channelId, userId, emoji)
                : WsReactionPayload.removed(messageId, channelId, userId, emoji);
        return WsEvent.of(added ? WsEventType.REACTION_ADDED : WsEventType.REACTION_REMOVED, payload, channelId);
    }

    public WsEvent<WsTypingPayload> buildTyping(Long channelId, Long userId, String userName, boolean isTyping) {
        WsTypingPayload payload = isTyping
                ? WsTypingPayload.start(channelId, userId, userName)
                : WsTypingPayload.stop(channelId, userId);
        return WsEvent.of(isTyping ? WsEventType.TYPING_START : WsEventType.TYPING_STOP, payload, channelId);
    }

    private WsEvent<WsMessagePayload> buildMessageEvent(WsEventType eventType, MessageEntity message) {
        MessageResponse response = enrichMessageSafely(message);
        WsMessagePayload payload = WsMessagePayload.builder()
                .messageId(message.getId())
                .channelId(message.getChannelId())
                .senderId(message.getSenderId())
                .message(response)
                .build();
        return WsEvent.of(eventType, payload, message.getChannelId());
    }

    private MessageResponse enrichMessageSafely(MessageEntity message) {
        try {
            MessageResponse response = attachmentUrlService.enrichMessageWithUrls(message);
            if (response == null) {
                response = MessageResponse.fromEntity(message);
            }
            MessageResponse withUserInfo = userInfoService.enrichMessageWithUserInfo(response);
            return withUserInfo == null ? response : withUserInfo;
        } catch (Exception e) {
            log.warn("Failed to enrich realtime message payload for message {}: {}",
                    message.getId(), e.getMessage());
            return MessageResponse.fromEntity(message);
            
        }
    }
}
