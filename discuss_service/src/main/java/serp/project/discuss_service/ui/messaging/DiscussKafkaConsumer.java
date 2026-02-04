/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Kafka consumer for discuss events
 */

package serp.project.discuss_service.ui.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.port.client.IWebSocketHubPort;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;
import serp.project.discuss_service.core.service.IMessageService;
import serp.project.discuss_service.kernel.utils.JsonUtils;

import java.util.Map;
import java.util.Optional;

/**
 * Kafka consumer for discuss service.
 * Listens to Kafka events and triggers WebSocket notifications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DiscussKafkaConsumer {

    private final IWebSocketHubPort webSocketHub;
    private final IMessageService messageService;
    private final JsonUtils jsonUtils;

    /**
     * Handle message events (sent, updated, deleted)
     */
    @KafkaListener(topics = IDiscussEventPublisher.TOPIC_MESSAGE_EVENTS, groupId = "${spring.kafka.consumer.group-id}")
    public void handleMessageEvent(String payload) {
        try {
            Map<String, Object> event = jsonUtils.fromJson(payload, Map.class);
            String eventTypeStr = (String) event.get("eventType");
            
            if (eventTypeStr == null) {
                log.warn("Received message event without eventType: {}", payload);
                return;
            }

            WsEventType eventType;
            try {
                eventType = WsEventType.valueOf(eventTypeStr);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown event type: {}", eventTypeStr);
                return;
            }

            Long channelId = getLong(event, "channelId");
            Long messageId = getLong(event, "messageId");

            switch (eventType) {
                case MESSAGE_NEW:
                    handleMessageSent(channelId, messageId);
                    break;
                case MESSAGE_UPDATED:
                    handleMessageUpdated(channelId, messageId);
                    break;
                case MESSAGE_DELETED:
                    handleMessageDeleted(channelId, messageId);
                    break;
                default:
                    log.debug("Ignored message event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process message event: {}", payload, e);
        }
    }

    /**
     * Handle reaction events (added, removed)
     */
    @KafkaListener(topics = IDiscussEventPublisher.TOPIC_REACTION_EVENTS, groupId = "${spring.kafka.consumer.group-id}")
    public void handleReactionEvent(String payload) {
        try {
            Map<String, Object> event = jsonUtils.fromJson(payload, Map.class);
            String eventTypeStr = (String) event.get("eventType");
            
            if (eventTypeStr == null) return;

            WsEventType eventType;
            try {
                eventType = WsEventType.valueOf(eventTypeStr);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown event type: {}", eventTypeStr);
                return;
            }

            Long channelId = getLong(event, "channelId");
            Long messageId = getLong(event, "messageId");
            Long userId = getLong(event, "userId");
            String emoji = (String) event.get("emoji");

            switch (eventType) {
                case REACTION_ADDED:
                    webSocketHub.notifyReaction(channelId, messageId, userId, emoji, true);
                    break;
                case REACTION_REMOVED:
                    webSocketHub.notifyReaction(channelId, messageId, userId, emoji, false);
                    break;
                default:
                    log.debug("Ignored reaction event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Failed to process reaction event: {}", payload, e);
        }
    }

    /**
     * Handle presence events (typing, online, offline)
     */
    @KafkaListener(topics = IDiscussEventPublisher.TOPIC_PRESENCE_EVENTS, groupId = "${spring.kafka.consumer.group-id}")
    public void handlePresenceEvent(String payload) {
        try {
            Map<String, Object> event = jsonUtils.fromJson(payload, Map.class);
            String eventTypeStr = (String) event.get("eventType");
            
            if (eventTypeStr == null) return;

            WsEventType eventType;
            try {
                eventType = WsEventType.valueOf(eventTypeStr);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown event type: {}", eventTypeStr);
                return;
            }

            Long userId = getLong(event, "userId");

            switch (eventType) {
                case TYPING_START: {
                    Long channelId = getLong(event, "channelId");
                    webSocketHub.notifyTyping(channelId, userId, true);
                    break;
                }
                case TYPING_STOP: {
                    Long channelId = getLong(event, "channelId");
                    webSocketHub.notifyTyping(channelId, userId, false);
                    break;
                }
                case USER_ONLINE:
                    log.debug("Received presence event: {} for user {}", eventType, userId);
                    break;
                case USER_OFFLINE:
                    log.debug("Received presence event: {} for user {}", eventType, userId);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to process presence event: {}", payload, e);
        }
    }

    private void handleMessageSent(Long channelId, Long messageId) {
        Optional<MessageEntity> messageOpt = messageService.getMessageById(messageId);
        if (messageOpt.isPresent()) {
            webSocketHub.notifyNewMessage(channelId, messageOpt.get());
        } else {
            log.warn("Message not found for notification: {}", messageId);
        }
    }

    private void handleMessageUpdated(Long channelId, Long messageId) {
        Optional<MessageEntity> messageOpt = messageService.getMessageById(messageId);
        if (messageOpt.isPresent()) {
            webSocketHub.notifyMessageUpdated(channelId, messageOpt.get());
        } else {
            log.warn("Message not found for update notification: {}", messageId);
        }
    }

    private void handleMessageDeleted(Long channelId, Long messageId) {
        webSocketHub.notifyMessageDeleted(channelId, messageId);
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        if (val instanceof String) {
            try {
                return Long.parseLong((String) val);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
