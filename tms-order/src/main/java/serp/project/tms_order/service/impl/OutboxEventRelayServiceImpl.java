/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.tms_order.domain.OutboxEvent;
import serp.project.tms_order.enums.OutboxEventStatus;
import serp.project.tms_order.repository.OutboxEventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OutboxEventRelayServiceImpl {
    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final int DEFAULT_MAX_RETRIES = 5;
    private static final long DEFAULT_SEND_TIMEOUT_MS = 10000L;
    private static final long[] RETRY_DELAYS_SECONDS = {30L, 120L, 300L, 900L, 1800L};

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.outbox.batch-size:50}")
    private int configuredBatchSize;

    @Value("${app.kafka.outbox.max-retries:5}")
    private int configuredMaxRetries;

    @Value("${app.kafka.outbox.send-timeout-ms:10000}")
    private long configuredSendTimeoutMs;

    public OutboxEventRelayServiceImpl(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${app.kafka.outbox.relay-interval-ms:10000}")
    @Transactional(rollbackFor = Exception.class)
    public void publishPendingEvents() {
        Pageable pageable = PageRequest.of(0, resolveConfiguredBatchSize());
        List<OutboxEvent> events = outboxEventRepository.findPublishableEvents(
                List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED),
                LocalDateTime.now(),
                pageable
        );

        for (OutboxEvent event : events) {
            publishSingleEvent(event);
        }
    }

    private void publishSingleEvent(OutboxEvent event) {
        try {
            kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayload())
                    .get(resolveConfiguredSendTimeoutMs(), TimeUnit.MILLISECONDS);
            event.setStatus(OutboxEventStatus.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());
            event.setErrorMessage(null);
            event.setNextRetryAt(null);
            log.info("Outbox event published: id={}, topic={}, key={}, eventType={}",
                    event.getId(), event.getTopic(), event.getMessageKey(), event.getEventType());
        } catch (Exception exception) {
            int nextRetryCount = safeInt(event.getRetryCount()) + 1;
            event.setRetryCount(nextRetryCount);
            event.setErrorMessage(truncateError(exception.getMessage()));

            int maxRetries = event.getMaxRetries() == null ? resolveConfiguredMaxRetries() : event.getMaxRetries();
            if (nextRetryCount > maxRetries) {
                event.setStatus(OutboxEventStatus.DEAD);
                event.setNextRetryAt(null);
                log.error("Outbox event dead-lettered: id={}, topic={}, key={}, retries={}",
                        event.getId(), event.getTopic(), event.getMessageKey(), nextRetryCount, exception);
                return;
            }

            event.setStatus(OutboxEventStatus.FAILED);
            event.setNextRetryAt(LocalDateTime.now().plusSeconds(resolveDelaySeconds(nextRetryCount)));
            log.warn("Outbox event publish failed: id={}, topic={}, key={}, retries={}/{}",
                    event.getId(), event.getTopic(), event.getMessageKey(), nextRetryCount, maxRetries, exception);
        }
    }

    private int resolveConfiguredBatchSize() {
        return configuredBatchSize > 0 ? configuredBatchSize : DEFAULT_BATCH_SIZE;
    }

    private int resolveConfiguredMaxRetries() {
        return configuredMaxRetries > 0 ? configuredMaxRetries : DEFAULT_MAX_RETRIES;
    }

    private long resolveConfiguredSendTimeoutMs() {
        return configuredSendTimeoutMs > 0 ? configuredSendTimeoutMs : DEFAULT_SEND_TIMEOUT_MS;
    }

    private long resolveDelaySeconds(int retryCount) {
        int index = Math.clamp(retryCount - 1, 0, RETRY_DELAYS_SECONDS.length - 1);
        return RETRY_DELAYS_SECONDS[index];
    }

    private String truncateError(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return null;
        }
        return errorMessage.length() > 2000 ? errorMessage.substring(0, 2000) : errorMessage;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
