/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.messaging.consumer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import serp.project.pmcore.core.domain.dto.message.KafkaEventContext;
import serp.project.pmcore.core.domain.enums.ConsumerInboxAcquireResult;
import serp.project.pmcore.core.exception.KafkaNonRetryableException;
import serp.project.pmcore.core.service.IConsumerInboxService;
import serp.project.pmcore.core.service.messaging.strategy.IKafkaEventHandlerStrategy;
import serp.project.pmcore.core.service.messaging.strategy.KafkaEventHandlerStrategyRegistry;
import serp.project.pmcore.kernel.utils.JsonUtils;

public abstract class AbstractKafkaConsumerTemplate {

    private static final Logger log = LoggerFactory.getLogger(AbstractKafkaConsumerTemplate.class);

    private final JsonUtils jsonUtils;
    private final IConsumerInboxService consumerInboxService;
    private final KafkaEventHandlerStrategyRegistry strategyRegistry;

    protected AbstractKafkaConsumerTemplate(
            JsonUtils jsonUtils,
            IConsumerInboxService consumerInboxService,
            KafkaEventHandlerStrategyRegistry strategyRegistry) {
        this.jsonUtils = jsonUtils;
        this.consumerInboxService = consumerInboxService;
        this.strategyRegistry = strategyRegistry;
    }

    protected final void processRecord(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment,
            String consumerGroupId) {
        String eventId = null;
        String eventType = null;
        Long tenantId = null;
        JsonNode envelope = null;

        try {
            envelope = parseAndValidateEnvelope(record.value());
            eventId = requiredValue(envelope.at("/meta/id"), "Kafka payload missing meta.id");
            eventType = requiredEventType(envelope.at("/meta/type"), "Kafka payload missing meta.type");
            tenantId = optionalLong(envelope.at("/meta/tenantId"));

            ConsumerInboxAcquireResult acquireResult = consumerInboxService.acquireForProcessing(
                    consumerGroupId,
                    eventId,
                    eventType,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    tenantId,
                    payloadHash(record.value()),
                    record.value());

            if (!acquireResult.shouldProcess()) {
                log.info(
                        "Kafka duplicate message skipped: group={}, eventId={}, eventType={}, topic={}, partition={}, offset={}, state={}",
                        consumerGroupId,
                        eventId,
                        eventType,
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        acquireResult);
                acknowledgeAfterCommit(acknowledgment);
                return;
            }

            KafkaEventContext context = new KafkaEventContext(
                    consumerGroupId,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    eventId,
                    eventType,
                    tenantId);

            beforeDispatch(envelope, context);

            String resolvedEventType = eventType;
            IKafkaEventHandlerStrategy strategy = strategyRegistry.findByEventType(eventType)
                    .orElseThrow(() -> new KafkaNonRetryableException(
                            "No Kafka strategy registered for eventType=" + resolvedEventType));
            strategy.handle(envelope, context);

            afterDispatch(envelope, context);

            consumerInboxService.markProcessed(consumerGroupId, eventId, record.offset());
            acknowledgeAfterCommit(acknowledgment);

            log.info(
                    "Kafka message processed successfully: group={}, eventId={}, eventType={}, topic={}, partition={}, offset={}",
                    consumerGroupId,
                    eventId,
                    eventType,
                    record.topic(),
                    record.partition(),
                    record.offset());
        } catch (KafkaNonRetryableException ex) {
            log.warn(
                    "Kafka non-retryable failure: group={}, eventId={}, eventType={}, topic={}, partition={}, offset={}, reason={}",
                    consumerGroupId,
                    eventId,
                    eventType,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    ex.getMessage());
            if (eventId != null) {
                consumerInboxService.markDead(consumerGroupId, eventId, ex.getMessage());
            }
            throw ex;
        } catch (Exception ex) {
            log.error(
                    "Kafka retryable failure: group={}, eventId={}, eventType={}, topic={}, partition={}, offset={}",
                    consumerGroupId,
                    eventId,
                    eventType,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    ex);
            if (eventId != null) {
                consumerInboxService.markFailed(consumerGroupId, eventId, ex.getMessage());
            }
            throw ex;
        }
    }

    protected void beforeDispatch(JsonNode message, KafkaEventContext context) {
    }

    protected void afterDispatch(JsonNode message, KafkaEventContext context) {
    }

    private JsonNode parseAndValidateEnvelope(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new KafkaNonRetryableException("Kafka payload is blank");
        }

        JsonNode message;
        try {
            message = jsonUtils.fromJson(rawPayload, JsonNode.class);
        } catch (Exception ex) {
            throw new KafkaNonRetryableException("Kafka payload is not valid JSON envelope", ex);
        }

        if (message == null || message.isNull()) {
            throw new KafkaNonRetryableException("Kafka payload missing meta section");
        }

        JsonNode meta = message.path("meta");
        if (meta.isMissingNode() || meta.isNull()) {
            throw new KafkaNonRetryableException("Kafka payload missing meta section");
        }

        if (normalize(meta.path("id").asText(null)) == null) {
            throw new KafkaNonRetryableException("Kafka payload missing meta.id");
        }
        if (normalizeEventType(meta.path("type").asText(null)) == null) {
            throw new KafkaNonRetryableException("Kafka payload missing meta.type");
        }

        return message;
    }

    private String payloadHash(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash Kafka payload", ex);
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeEventType(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String requiredValue(JsonNode node, String errorMessage) {
        String normalized = normalize(node.asText(null));
        if (normalized == null) {
            throw new KafkaNonRetryableException(errorMessage);
        }
        return normalized;
    }

    private String requiredEventType(JsonNode node, String errorMessage) {
        String normalized = normalizeEventType(node.asText(null));
        if (normalized == null) {
            throw new KafkaNonRetryableException(errorMessage);
        }
        return normalized;
    }

    private Long optionalLong(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return node.asLong();
    }

    private void acknowledgeAfterCommit(Acknowledgment acknowledgment) {
        if (acknowledgment == null) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    acknowledgment.acknowledge();
                }
            });
            return;
        }

        acknowledgment.acknowledge();
    }
}
