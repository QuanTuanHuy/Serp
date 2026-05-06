package serp.project.payment_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import serp.project.payment_service.enums.MessageType;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaMessagePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.payment-success:PAYMENT_SUCCESS}")
    private String paymentSuccessTopic;

    @Value("${app.kafka.topics.payment-failed:PAYMENT_FAILED}")
    private String paymentFailedTopic;

    @Value("${app.kafka.topics.refund-success:REFUND_SUCCESS}")
    private String refundSuccessTopic;

    @Value("${app.kafka.topics.refund-failed:REFUND_FAILED}")
    private String refundFailedTopic;

    @Value("${app.kafka.topics.email-events:serp.email.events}")
    private String emailEventsTopic;

    @Value("${app.kafka.topics.user-notification-events:serp.notification.user.events}")
    private String userNotificationEventsTopic;

    @Value("${app.kafka.event-types.email-send-requested:email.send.requested}")
    private String emailSendRequestedEventType;

    @Value("${app.kafka.event-types.notification-create-requested:notification.create.requested}")
    private String notificationCreateRequestedEventType;

    @Value("${app.kafka.source:payment-service}")
    private String sourceService;

    private final Map<MessageType, String> topicByType = new EnumMap<>(MessageType.class);

    @PostConstruct
    void initTopicMap() {
        topicByType.put(MessageType.PAYMENT_SUCCESS, paymentSuccessTopic);
        topicByType.put(MessageType.PAYMENT_FAILED, paymentFailedTopic);
        topicByType.put(MessageType.REFUND_SUCCESS, refundSuccessTopic);
        topicByType.put(MessageType.REFUND_FAILED, refundFailedTopic);
    }

    public void publish(MessageType messageType, String key, Object payload) {
        if (payload == null) {
            log.warn("Skip Kafka publish: messageType={} key={} payload=null", messageType, key);
            return;
        }

        final String topic = resolveTopic(messageType);
        final String json;
        try {
            json = (payload instanceof String raw) ? raw : objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload for Kafka topic {}: {}", topic, e.getMessage(), e);
            return;
        }

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, json);
        handleAsyncResult(topic, key, future);
    }

    public void publishEmailRequested(String key, Long tenantId, Long actorId,
                                      String aggregateType, String aggregateId, Object payload) {
        publishEnvelope(
                emailEventsTopic,
                key,
                emailSendRequestedEventType,
                tenantId,
                actorId,
                aggregateType,
                aggregateId,
                payload
        );
    }

    public void publishUserNotificationRequested(String key, Long tenantId, Long actorId,
                                                 String aggregateType, String aggregateId, Object payload) {
        publishEnvelope(
                userNotificationEventsTopic,
                key,
                notificationCreateRequestedEventType,
                tenantId,
                actorId,
                aggregateType,
                aggregateId,
                payload
        );
    }

    private void publishEnvelope(String topic, String key, String eventType, Long tenantId, Long actorId,
                                 String aggregateType, String aggregateId, Object payload) {
        if (payload == null) {
            log.warn("Skip Kafka publish envelope: topic={} key={} payload=null", topic, key);
            return;
        }
        if (tenantId == null) {
            log.warn("Skip Kafka publish envelope: topic={} key={} tenantId=null", topic, key);
            return;
        }

        final String json;
        try {
            json = objectMapper.writeValueAsString(buildEnvelope(
                    eventType,
                    tenantId,
                    actorId,
                    aggregateType,
                    aggregateId,
                    payload));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize envelope for Kafka topic {}: {}", topic, e.getMessage(), e);
            return;
        }

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, json);
        handleAsyncResult(topic, key, future);
    }

    private Map<String, Object> buildEnvelope(String eventType, Long tenantId, Long actorId,
                                              String aggregateType, String aggregateId, Object payload) {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("id", UUID.randomUUID().toString());
        meta.put("type", eventType);
        meta.put("source", sourceService);
        meta.put("v", "1.0");
        meta.put("ts", Instant.now().toEpochMilli());
        meta.put("traceId", UUID.randomUUID().toString());
        meta.put("correlationId", UUID.randomUUID().toString());
        meta.put("tenantId", tenantId);
        meta.put("actorId", actorId);
        meta.put("aggregateType", aggregateType);
        meta.put("aggregateId", aggregateId);
        root.put("meta", meta);
        root.put("data", payload);
        return root;
    }

    private void handleAsyncResult(String topic, String key, CompletableFuture<SendResult<String, String>> future) {
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send Kafka message topic={} key={}: {}", topic, key, ex.getMessage(), ex);
                return;
            }
            if (result != null && result.getRecordMetadata() != null) {
                log.info("Kafka message sent topic={} partition={} offset={} key={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        key);
            } else {
                log.info("Kafka message sent topic={} key={}", topic, key);
            }
        });
    }

    private String resolveTopic(MessageType messageType) {
        return Optional.ofNullable(messageType)
                .map(topicByType::get)
                .filter(t -> t != null && !t.isBlank())
                .orElse(paymentFailedTopic);
    }
}

