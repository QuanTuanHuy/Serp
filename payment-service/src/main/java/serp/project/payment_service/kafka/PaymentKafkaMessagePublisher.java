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
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
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

