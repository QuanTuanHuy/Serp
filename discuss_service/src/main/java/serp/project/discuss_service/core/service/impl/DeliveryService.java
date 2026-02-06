/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project -
 */

package serp.project.discuss_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;
import serp.project.discuss_service.core.domain.dto.response.MessageResponse;
import serp.project.discuss_service.core.domain.dto.websocket.*;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.port.client.IWebSocketHubPort;
import serp.project.discuss_service.core.service.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService implements IDeliveryService {

    private final IWebSocketHubPort webSocketHub;

    private final IMessageService messageService;
    private final IChannelMemberService memberService;
    private final IPresenceService presenceService;
    private final IAttachmentUrlService attachmentUrlService;
    private final IUserInfoService userInfoService;

    @Override
    public void sendToUser(Long userId, Object payload) {
        webSocketHub.sendToUser(userId, payload);
    }

    @Override
    public void sendToUsers(Set<Long> userIds, Object payload) {
        webSocketHub.sendToUsers(userIds, payload);
    }

    @Override
    public void fanOutToChannelMembers(Long channelId, Object payload) {
        if (channelId == null || payload == null) {
            log.warn("Cannot fan-out with null channelId or payload");
            return;
        }

        Set<Long> memberIds = memberService.getMemberIds(channelId);
        Set<Long> onlineUserIds = presenceService.getOnlineUsers(memberIds);
        if (onlineUserIds.isEmpty()) {
            log.debug("No online members in channel {}, skipping fan-out", channelId);
            return;
        }

        sendToUsers(onlineUserIds, payload);
        log.debug("Fan-out to {} members of channel {}", onlineUserIds.size(), channelId);
    }

    @Override
    public void fanOutToChannelMembersExcept(Long channelId, Long excludeUserId, Object payload) {
        if (channelId == null || excludeUserId == null || payload == null) {
            log.warn("Cannot fan-out with null channelId, excludeUserId or payload");
            return;
        }
        Set<Long> memberIds = memberService.getMemberIds(channelId).stream()
                .filter(memberId -> !memberId.equals(excludeUserId))
                .collect(Collectors.toSet());
        Set<Long> onlineUserIds = presenceService.getOnlineUsers(memberIds);
        if (onlineUserIds.isEmpty()) {
            log.debug("No online members in channel {}, skipping fan-out", channelId);
            return;
        }

        sendToUsers(onlineUserIds, payload);
        log.debug("Fan-out to {} members of channel {} except {}", onlineUserIds.size(), channelId, excludeUserId);

    }

    @Override
    public void notifyTyping(Long channelId, Long userId, boolean isTyping) {
        if (channelId == null || userId == null) {
            log.warn("Cannot notify typing with null channelId or userId");
            return;
        }
        String userName = userInfoService.getUserById(userId).map(ChannelMemberResponse.UserInfo::getName).orElse("");
        if (userName.isBlank()) {
            log.warn("Cannot notify typing for user {}: no name found", userId);
        }

        WsTypingPayload payload = isTyping ?
                WsTypingPayload.start(channelId, userId, userName) :
                WsTypingPayload.stop(channelId, userId);

        WsEventType eventType = isTyping ? WsEventType.TYPING_START : WsEventType.TYPING_STOP;
        WsEvent<WsTypingPayload> event = WsEvent.of(eventType, payload, channelId);
        fanOutToChannelMembersExcept(channelId, userId, event);
    }

    @Override
    public void notifyNewMessage(Long channelId, Long messageId) {
        if (channelId == null || messageId == null) {
            log.warn("Cannot notify new message with null channelId or messageId");
        }
        Optional<MessageResponse> messageOpt = getMessageById(messageId);
        messageOpt.ifPresent(message -> {
            WsMessagePayload payload = WsMessagePayload.builder()
                    .messageId(messageId)
                    .channelId(channelId)
                    .senderId(message.getSenderId())
                    .message(message)
                    .build();
            WsEvent<WsMessagePayload> event = WsEvent.of(WsEventType.MESSAGE_NEW, payload, channelId);
            fanOutToChannelMembers(channelId, event);
            log.debug("Notified new message {} in channel {}", messageId, channelId);
        });
    }

    @Override
    public void notifyMessageUpdated(Long channelId, Long messageId) {
        if (channelId == null || messageId == null) {
            log.warn("Cannot notify updated message with null channelId or messageId");
        }
        Optional<MessageResponse> messageOpt = getMessageById(messageId);
        messageOpt.ifPresent(message -> {
            WsMessagePayload payload = WsMessagePayload.builder()
                    .messageId(messageId)
                    .channelId(channelId)
                    .senderId(message.getSenderId())
                    .message(message)
                    .build();
            WsEvent<WsMessagePayload> event = WsEvent.of(WsEventType.MESSAGE_UPDATED, payload, channelId);
            fanOutToChannelMembers(channelId, event);
            log.debug("Notified updated message {} in channel {}", messageId, channelId);
        });
    }

    @Override
    public void notifyMessageDeleted(Long channelId, Long messageId) {
        if (channelId == null || messageId == null) {
            log.warn("Cannot notify deleted message with null channelId or messageId");
        }
        WsMessagePayload payload = WsMessagePayload.forDeletion(messageId, channelId);
        WsEvent<WsMessagePayload> event = WsEvent.of(
                WsEventType.MESSAGE_DELETED,
                payload,
                channelId
        );
        fanOutToChannelMembers(channelId, event);
        log.debug("Notified deleted message {} in channel {}", messageId, channelId);
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

        fanOutToChannelMembers(channelId, event);
        log.debug("Reaction {} fan-out for message {} in channel {}",
                added ? "added" : "removed", messageId, channelId);
    }

    @Override
    public void notifyPresenceChange(Long userId) {
        if (userId == null) {
            log.warn("Cannot notify presence change with null userId");
            return;
        }

        presenceService.getUserPresence(userId)
                .flatMap(presence ->
                        userInfoService.getUserById(userId)
                                .map(userInfo -> WsPresencePayload.fromEntity(
                                        presence,
                                        userInfo.getName(),
                                        userInfo.getAvatarUrl()
                                ))
                )
                .ifPresent(payload -> {
                    WsEvent<WsPresencePayload> event =
                            WsEvent.of(WsEventType.USER_PRESENCE_CHANGED, payload);

                    log.debug("Presence change for user {}: {}", userId, payload);

                    // TODO: Fan-out to specific users
                });
    }


    public Optional<MessageResponse> getMessageById(Long messageId) {
        Optional< MessageEntity> messageOpt = messageService.getMessageById(messageId);
        return messageOpt.map(attachmentUrlService::enrichMessageWithUrls)
                .map(userInfoService::enrichMessageWithUserInfo);
    }
}
