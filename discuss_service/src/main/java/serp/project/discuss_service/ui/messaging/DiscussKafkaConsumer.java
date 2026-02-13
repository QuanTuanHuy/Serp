/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Kafka consumer for discuss events
 */

package serp.project.discuss_service.ui.messaging;

import java.util.HashMap;
import java.util.Map;

import io.github.serp.platform.kafka.consumer.SerpKafkaConsumerMetadata;
import io.github.serp.platform.kafka.consumer.SerpKafkaRecordParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;
import serp.project.discuss_service.kernel.utils.KafkaPayloadUtils;
import serp.project.discuss_service.ui.messaging.handler.HandlerRegistry;

@Component
@Slf4j
public class DiscussKafkaConsumer
        extends AbstractKafkaConsumer<WsEvent<Map<String, Object>>, WsEventType> {
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final HandlerRegistry handlerRegistry;
    private final SerpKafkaRecordParser recordParser;

    public DiscussKafkaConsumer(
            HandlerRegistry handlerRegistry,
            SerpKafkaRecordParser recordParser) {
        this.handlerRegistry = handlerRegistry;
        this.recordParser = recordParser;
    }

    @KafkaListener(topics = IDiscussEventPublisher.TOPIC_MESSAGE_EVENTS, groupId = "${spring.kafka.consumer.group-id}")
    public void handleMessageEvent(ConsumerRecord<String, String> record) {
        logRecordMetadata(record, "message");
        processRecord(record, handlerRegistry.getMessageHandlers(), "message");
    }

    @KafkaListener(topics = IDiscussEventPublisher.TOPIC_REACTION_EVENTS, groupId = "${spring.kafka.consumer.group-id}")
    public void handleReactionEvent(ConsumerRecord<String, String> record) {
        logRecordMetadata(record, "reaction");
        processRecord(record, handlerRegistry.getReactionHandlers(), "reaction");
    }

    @KafkaListener(topics = IDiscussEventPublisher.TOPIC_PRESENCE_EVENTS, groupId = "${spring.kafka.consumer.group-id}")
    public void handlePresenceEvent(ConsumerRecord<String, String> record) {
        logRecordMetadata(record, "presence");
        processRecord(record, handlerRegistry.getPresenceHandlers(), "presence");
    }

    @Override
    protected WsEvent<Map<String, Object>> parsePayload(ConsumerRecord<String, String> record) {
        return parseWsEvent(record);
    }

    @Override
    protected WsEventType resolveEventType(WsEvent<Map<String, Object>> event) {
        return event.getType();
    }

    private WsEvent<Map<String, Object>> parseWsEvent(ConsumerRecord<String, String> record) {
        Map<String, Object> raw = parseRawEvent(record);
        if (raw == null) {
            return null;
        }

        String eventTypeStr = KafkaPayloadUtils.getString(raw, "type");
        if (eventTypeStr == null) {
            eventTypeStr = KafkaPayloadUtils.getString(raw, "eventType");
        }
        if (eventTypeStr == null) {
            return null;
        }

        WsEventType eventType = parseEventType(eventTypeStr);
        if (eventType == null) {
            return null;
        }

        Long channelId = KafkaPayloadUtils.getLong(raw, "channelId");
        Long timestamp = KafkaPayloadUtils.getLong(raw, "timestamp");
        Map<String, Object> payloadMap = extractPayload(raw);

        return WsEvent.<Map<String, Object>>builder()
                .type(eventType)
                .payload(payloadMap)
                .channelId(channelId)
                .timestamp(timestamp)
                .build();
    }

    private Map<String, Object> parseRawEvent(ConsumerRecord<String, String> record) {
        try {
            return recordParser.parse(record, new ParameterizedTypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.warn("Failed to parse event payload as JSON", e);
            throw new IllegalArgumentException("Failed to parse websocket event payload", e);
        }
    }

    private WsEventType parseEventType(String eventTypeStr) {
        try {
            return WsEventType.valueOf(eventTypeStr);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown event type: {}", eventTypeStr);
            return null;
        }
    }

    private Map<String, Object> extractPayload(Map<String, Object> raw) {
        Object payloadObj = raw.get("payload");
        if (payloadObj instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> payloadMap = (Map<String, Object>) payloadObj;
            return payloadMap;
        }

        Map<String, Object> payloadMap = new HashMap<>(raw);
        payloadMap.remove("eventType");
        payloadMap.remove("type");
        payloadMap.remove("channelId");
        payloadMap.remove("timestamp");
        payloadMap.remove("payload");
        return payloadMap;
    }

    private void logRecordMetadata(ConsumerRecord<String, String> record, String handlerName) {
        SerpKafkaConsumerMetadata metadata = SerpKafkaConsumerMetadata.fromRecord(record, CORRELATION_ID_HEADER);
        log.debug("Kafka {} event topic={}, partition={}, offset={}, key={}, correlationId={}",
                handlerName,
                metadata.getTopic(),
                metadata.getPartition(),
                metadata.getOffset(),
                metadata.getKey(),
                metadata.getCorrelationId());
    }
}
