/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Discuss event publisher service implementation
 */

package serp.project.discuss_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.port.client.IKafkaProducerPort;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of discuss event publisher.
 * Publishes discuss-specific events to Kafka topics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscussEventPublisherService implements IDiscussEventPublisher {

    private final IKafkaProducerPort kafkaProducer;

    // Event type constants
    private static final String EVENT_MESSAGE_SENT = "MESSAGE_SENT";
    private static final String EVENT_MESSAGE_UPDATED = "MESSAGE_UPDATED";
    private static final String EVENT_MESSAGE_DELETED = "MESSAGE_DELETED";
    private static final String EVENT_CHANNEL_CREATED = "CHANNEL_CREATED";
    private static final String EVENT_CHANNEL_UPDATED = "CHANNEL_UPDATED";
    private static final String EVENT_CHANNEL_ARCHIVED = "CHANNEL_ARCHIVED";
    private static final String EVENT_MEMBER_JOINED = "MEMBER_JOINED";
    private static final String EVENT_MEMBER_LEFT = "MEMBER_LEFT";
    private static final String EVENT_MEMBER_REMOVED = "MEMBER_REMOVED";
    private static final String EVENT_MEMBER_ROLE_CHANGED = "MEMBER_ROLE_CHANGED";
    private static final String EVENT_REACTION_ADDED = "REACTION_ADDED";
    private static final String EVENT_REACTION_REMOVED = "REACTION_REMOVED";
    private static final String EVENT_TYPING_START = "TYPING_START";
    private static final String EVENT_TYPING_STOP = "TYPING_STOP";
    private static final String EVENT_USER_ONLINE = "USER_ONLINE";
    private static final String EVENT_USER_OFFLINE = "USER_OFFLINE";

    // ==================== MESSAGE EVENTS ====================

    @Override
    public void publishMessageSent(MessageEntity message) {
        if (message == null) {
            log.warn("Cannot publish MESSAGE_SENT event: message is null");
            return;
        }
        Map<String, Object> event = buildMessageEvent(EVENT_MESSAGE_SENT, message);
        String key = String.valueOf(message.getChannelId());
        kafkaProducer.sendMessageAsync(key, event, TOPIC_MESSAGE_EVENTS, 
            (success, topic, payload, ex) -> {
                if (!success) {
                    log.error("Failed to publish MESSAGE_SENT event for message {}", 
                            message.getId(), ex);
                }
            });
        log.debug("Published MESSAGE_SENT event for message {}", message.getId());
    }

    @Override
    public void publishMessageUpdated(MessageEntity message) {
        if (message == null) {
            log.warn("Cannot publish MESSAGE_UPDATED event: message is null");
            return;
        }
        Map<String, Object> event = buildMessageEvent(EVENT_MESSAGE_UPDATED, message);
        String key = String.valueOf(message.getChannelId());
        kafkaProducer.sendMessageAsync(key, event, TOPIC_MESSAGE_EVENTS);
        log.debug("Published MESSAGE_UPDATED event for message {}", message.getId());
    }

    @Override
    public void publishMessageDeleted(MessageEntity message) {
        if (message == null) {
            log.warn("Cannot publish MESSAGE_DELETED event: message is null");
            return;
        }
        Map<String, Object> event = buildMessageEvent(EVENT_MESSAGE_DELETED, message);
        String key = String.valueOf(message.getChannelId());
        kafkaProducer.sendMessageAsync(key, event, TOPIC_MESSAGE_EVENTS);
        log.debug("Published MESSAGE_DELETED event for message {}", message.getId());
    }

    // ==================== CHANNEL EVENTS ====================

    @Override
    public void publishChannelCreated(ChannelEntity channel) {
        if (channel == null) {
            log.warn("Cannot publish CHANNEL_CREATED event: channel is null");
            return;
        }
        Map<String, Object> event = buildChannelEvent(EVENT_CHANNEL_CREATED, channel);
        String key = String.valueOf(channel.getId());
        kafkaProducer.sendMessageAsync(key, event, TOPIC_CHANNEL_EVENTS);
        log.debug("Published CHANNEL_CREATED event for channel {}", channel.getId());
    }

    @Override
    public void publishChannelUpdated(ChannelEntity channel) {
        if (channel == null) {
            log.warn("Cannot publish CHANNEL_UPDATED event: channel is null");
            return;
        }
        Map<String, Object> event = buildChannelEvent(EVENT_CHANNEL_UPDATED, channel);
        String key = String.valueOf(channel.getId());
        kafkaProducer.sendMessageAsync(key, event, TOPIC_CHANNEL_EVENTS);
        log.debug("Published CHANNEL_UPDATED event for channel {}", channel.getId());
    }

    @Override
    public void publishChannelArchived(ChannelEntity channel) {
        if (channel == null) {
            log.warn("Cannot publish CHANNEL_ARCHIVED event: channel is null");
            return;
        }
        Map<String, Object> event = buildChannelEvent(EVENT_CHANNEL_ARCHIVED, channel);
        String key = String.valueOf(channel.getId());
        kafkaProducer.sendMessageAsync(key, event, TOPIC_CHANNEL_EVENTS);
        log.debug("Published CHANNEL_ARCHIVED event for channel {}", channel.getId());
    }

    // ==================== MEMBER EVENTS ====================

    @Override
    public void publishMemberJoined(ChannelMemberEntity member) {
        if (member == null) {
            log.warn("Cannot publish MEMBER_JOINED event: member is null");
            return;
        }
        Map<String, Object> event = buildMemberEvent(EVENT_MEMBER_JOINED, member);
        String key = String.valueOf(member.getChannelId());
        kafkaProducer.sendMessageAsync(key, event, TOPIC_MEMBER_EVENTS);
        log.debug("Published MEMBER_JOINED event for user {} in channel {}", 
                member.getUserId(), member.getChannelId());
    }

    @Override
    public void publishMemberLeft(ChannelMemberEntity member) {
        if (member == null) {
            log.warn("Cannot publish MEMBER_LEFT event: member is null");
            return;
        }
        Map<String, Object> event = buildMemberEvent(EVENT_MEMBER_LEFT, member);
        String key = String.valueOf(member.getChannelId());
        kafkaProducer.sendMessageAsync(key, event, TOPIC_MEMBER_EVENTS);
        log.debug("Published MEMBER_LEFT event for user {} in channel {}", 
                member.getUserId(), member.getChannelId());
    }

    @Override
    public void publishMemberRemoved(ChannelMemberEntity member) {
        if (member == null) {
            log.warn("Cannot publish MEMBER_REMOVED event: member is null");
            return;
        }
        Map<String, Object> event = buildMemberEvent(EVENT_MEMBER_REMOVED, member);
        String key = String.valueOf(member.getChannelId());
        kafkaProducer.sendMessageAsync(key, event, TOPIC_MEMBER_EVENTS);
        log.debug("Published MEMBER_REMOVED event for user {} in channel {}", 
                member.getUserId(), member.getChannelId());
    }

    @Override
    public void publishMemberRoleChanged(ChannelMemberEntity member) {
        if (member == null) {
            log.warn("Cannot publish MEMBER_ROLE_CHANGED event: member is null");
            return;
        }
        Map<String, Object> event = buildMemberEvent(EVENT_MEMBER_ROLE_CHANGED, member);
        String key = String.valueOf(member.getChannelId());
        kafkaProducer.sendMessageAsync(key, event, TOPIC_MEMBER_EVENTS);
        log.debug("Published MEMBER_ROLE_CHANGED event for user {} in channel {}", 
                member.getUserId(), member.getChannelId());
    }

    // ==================== REACTION EVENTS ====================

    @Override
    public void publishReactionAdded(Long messageId, Long channelId, Long userId, String emoji) {
        if (messageId == null || channelId == null || userId == null || emoji == null) {
            log.warn("Cannot publish REACTION_ADDED event: missing required fields");
            return;
        }
        Map<String, Object> event = buildReactionEvent(EVENT_REACTION_ADDED, messageId, channelId, userId, emoji);
        String key = String.valueOf(channelId);
        kafkaProducer.sendMessageAsync(key, event, TOPIC_REACTION_EVENTS);
        log.debug("Published REACTION_ADDED event for message {} by user {}", messageId, userId);
    }

    @Override
    public void publishReactionRemoved(Long messageId, Long channelId, Long userId, String emoji) {
        if (messageId == null || channelId == null || userId == null || emoji == null) {
            log.warn("Cannot publish REACTION_REMOVED event: missing required fields");
            return;
        }
        Map<String, Object> event = buildReactionEvent(EVENT_REACTION_REMOVED, messageId, channelId, userId, emoji);
        String key = String.valueOf(channelId);
        kafkaProducer.sendMessageAsync(key, event, TOPIC_REACTION_EVENTS);
        log.debug("Published REACTION_REMOVED event for message {} by user {}", messageId, userId);
    }

    // ==================== PRESENCE EVENTS ====================

    @Override
    public void publishTypingIndicator(Long channelId, Long userId, boolean isTyping) {
        if (channelId == null || userId == null) {
            log.warn("Cannot publish typing indicator event: missing required fields");
            return;
        }
        String eventType = isTyping ? EVENT_TYPING_START : EVENT_TYPING_STOP;
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("channelId", channelId);
        event.put("userId", userId);
        event.put("timestamp", System.currentTimeMillis());

        String key = String.valueOf(channelId);
        kafkaProducer.sendMessageAsync(key, event, TOPIC_PRESENCE_EVENTS);
        log.debug("Published {} event for user {} in channel {}", eventType, userId, channelId);
    }

    @Override
    public void publishUserOnline(Long userId) {
        if (userId == null) {
            log.warn("Cannot publish USER_ONLINE event: userId is null");
            return;
        }
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", EVENT_USER_ONLINE);
        event.put("userId", userId);
        event.put("timestamp", System.currentTimeMillis());

        String key = String.valueOf(userId);
        kafkaProducer.sendMessageAsync(key, event, TOPIC_PRESENCE_EVENTS);
        log.debug("Published USER_ONLINE event for user {}", userId);
    }

    @Override
    public void publishUserOffline(Long userId) {
        if (userId == null) {
            log.warn("Cannot publish USER_OFFLINE event: userId is null");
            return;
        }
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", EVENT_USER_OFFLINE);
        event.put("userId", userId);
        event.put("timestamp", System.currentTimeMillis());

        String key = String.valueOf(userId);
        kafkaProducer.sendMessageAsync(key, event, TOPIC_PRESENCE_EVENTS);
        log.debug("Published USER_OFFLINE event for user {}", userId);
    }

    // ==================== HELPER METHODS ====================

    private Map<String, Object> buildMessageEvent(String eventType, MessageEntity message) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("messageId", message.getId());
        event.put("channelId", message.getChannelId());
        event.put("senderId", message.getSenderId());
        event.put("content", message.getContent());
        event.put("messageType", message.getMessageType());
        event.put("parentId", message.getParentId());
        event.put("mentions", message.getMentions());
        event.put("timestamp", System.currentTimeMillis());
        return event;
    }

    private Map<String, Object> buildChannelEvent(String eventType, ChannelEntity channel) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("channelId", channel.getId());
        event.put("name", channel.getName());
        event.put("channelType", channel.getType());
        event.put("tenantId", channel.getTenantId());
        event.put("createdBy", channel.getCreatedBy());
        event.put("timestamp", System.currentTimeMillis());
        return event;
    }

    private Map<String, Object> buildMemberEvent(String eventType, ChannelMemberEntity member) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("channelId", member.getChannelId());
        event.put("userId", member.getUserId());
        event.put("role", member.getRole());
        event.put("status", member.getStatus());
        event.put("timestamp", System.currentTimeMillis());
        return event;
    }

    private Map<String, Object> buildReactionEvent(String eventType, Long messageId, Long channelId, Long userId, String emoji) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("messageId", messageId);
        event.put("channelId", channelId);
        event.put("userId", userId);
        event.put("emoji", emoji);
        event.put("timestamp", System.currentTimeMillis());
        return event;
    }
}
