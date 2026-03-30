/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.domain.KafkaDlqMessage;
import serp.project.first_mile.dto.message.SyncUserFirstMileEvent;
import serp.project.first_mile.enums.KafkaDlqMessageStatus;
import serp.project.first_mile.repository.KafkaDlqMessageRepository;
import serp.project.first_mile.service.KafkaDlqService;
import serp.project.first_mile.service.PostOfficeStaffSyncService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaDlqServiceImpl implements KafkaDlqService {
    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final int DEFAULT_MAX_RETRIES = 5;
    private static final long[] RETRY_DELAYS_SECONDS = {30L, 120L, 300L, 900L, 1800L};

    private final KafkaDlqMessageRepository kafkaDlqMessageRepository;
    private final ObjectMapper objectMapper;
    private final PostOfficeStaffSyncService postOfficeStaffSyncService;

    @Value("${app.kafka.dlq.batch-size:50}")
    private int configuredBatchSize;

    @Value("${app.kafka.dlq.max-retries:5}")
    private int configuredMaxRetries;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFailedMessage(String topic, String messageKey, String payload, String errorMessage, Long tenantId) {
        KafkaDlqMessage message = KafkaDlqMessage.builder()
                .topic(topic)
                .messageKey(messageKey)
                .payload(payload)
                .errorMessage(truncateError(errorMessage))
                .status(KafkaDlqMessageStatus.PENDING)
                .retryCount(0)
                .maxRetries(resolveConfiguredMaxRetries())
                .nextRetryAt(null)
                .tenantId(tenantId)
                .build();

        kafkaDlqMessageRepository.save(message);
        log.warn("Saved Kafka message to DLQ: topic={}, key={}, tenantId={}", topic, messageKey, tenantId);
    }

    @Override
    @Scheduled(fixedDelayString = "${app.kafka.dlq.retry-interval-ms:30000}")
    @Transactional(rollbackFor = Exception.class)
    public void retryPendingMessages() {
        Pageable pageable = PageRequest.of(0, resolveConfiguredBatchSize());
        List<KafkaDlqMessage> retryableMessages = kafkaDlqMessageRepository.findRetryableMessages(
                List.of(KafkaDlqMessageStatus.PENDING, KafkaDlqMessageStatus.FAILED),
                LocalDateTime.now(),
                pageable
        );

        if (retryableMessages.isEmpty()) {
            return;
        }

        for (KafkaDlqMessage message : retryableMessages) {
            retrySingleMessage(message);
        }
    }

    private void retrySingleMessage(KafkaDlqMessage message) {
        try {
            SyncUserFirstMileEvent event = objectMapper.readValue(message.getPayload(), SyncUserFirstMileEvent.class);
            postOfficeStaffSyncService.syncUser(event);

            message.setStatus(KafkaDlqMessageStatus.PROCESSED);
            message.setErrorMessage(null);
            message.setNextRetryAt(null);
            log.info("Kafka DLQ retry succeeded: dlqId={}, topic={}, key={}",
                    message.getId(),
                    message.getTopic(),
                    message.getMessageKey());
        } catch (Exception exception) {
            int nextRetryCount = safeInt(message.getRetryCount()) + 1;
            message.setRetryCount(nextRetryCount);
            message.setErrorMessage(truncateError(exception.getMessage()));

            int maxRetries = message.getMaxRetries() == null ? resolveConfiguredMaxRetries() : message.getMaxRetries();
            if (nextRetryCount > maxRetries) {
                message.setStatus(KafkaDlqMessageStatus.DEAD);
                message.setNextRetryAt(null);
                log.error("Kafka DLQ retry dead-lettered: dlqId={}, topic={}, key={}, retries={}",
                        message.getId(),
                        message.getTopic(),
                        message.getMessageKey(),
                        nextRetryCount,
                        exception);
                return;
            }

            message.setStatus(KafkaDlqMessageStatus.FAILED);
            message.setNextRetryAt(LocalDateTime.now().plusSeconds(resolveDelaySeconds(nextRetryCount)));
            log.warn("Kafka DLQ retry failed: dlqId={}, topic={}, key={}, retries={}/{}", 
                    message.getId(),
                    message.getTopic(),
                    message.getMessageKey(),
                    nextRetryCount,
                    maxRetries,
                    exception);
        }
    }

    private String truncateError(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return null;
        }

        return errorMessage.length() > 2000 ? errorMessage.substring(0, 2000) : errorMessage;
    }

    private int resolveConfiguredBatchSize() {
        return configuredBatchSize > 0 ? configuredBatchSize : DEFAULT_BATCH_SIZE;
    }

    private int resolveConfiguredMaxRetries() {
        return configuredMaxRetries > 0 ? configuredMaxRetries : DEFAULT_MAX_RETRIES;
    }

    private long resolveDelaySeconds(int retryCount) {
        int index = Math.clamp(retryCount - 1, 0, RETRY_DELAYS_SECONDS.length - 1);
        return RETRY_DELAYS_SECONDS[index];
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
