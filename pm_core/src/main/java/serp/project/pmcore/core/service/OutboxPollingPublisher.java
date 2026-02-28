/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.core.domain.entity.OutboxEventEntity;
import serp.project.pmcore.core.domain.enums.OutboxEventStatus;
import serp.project.pmcore.core.port.client.IKafkaPublisher;
import serp.project.pmcore.core.port.store.IOutboxEventPort;
import serp.project.pmcore.kernel.property.OutboxProperties;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPollingPublisher {
    private final IOutboxEventPort outboxEventPort;
    private final IKafkaPublisher kafkaPublisher;
    private final OutboxProperties outboxProperties;

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
    @Transactional
    public void pollAndPublish() {
        List<OutboxEventEntity> events = outboxEventPort.getEventsByStatuses(
            List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED),
            outboxProperties.getBatchSize());

        if (events.isEmpty()) {
            return;
        }

        log.info("Polled {} outbox events for publishing", events.size());
        for (OutboxEventEntity event : events) {
            try {
                kafkaPublisher.sendMessageSync(
                    event.getPartitionKey(),
                    event.getPayload(),
                    event.getTopic()
                );
                event.markPublished();
                log.info("Outbox event published: id={}, type={}, aggregate={}:{}",
                    event.getId(), event.getEventType(),
                    event.getAggregateType(), event.getAggregateId());
            } catch (Exception e) {
                event.markFailed(e.getMessage());
                log.warn("Outbox event publish failed: id={}, type={}, retry={}/{}",
                    event.getId(), event.getEventType(),
                    event.getRetryCount(), event.getMaxRetries(), e);
            }
        }

        outboxEventPort.batchUpdateStatus(events);
    }

    @Scheduled(cron = "${app.outbox.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void cleanupPublishedEvents() {
        long cutoff = Instant.now()
            .minus(outboxProperties.getRetentionDays(), ChronoUnit.DAYS)
            .toEpochMilli();
        int deleted = outboxEventPort.deletePublishedEventsBefore(cutoff);
        if (deleted > 0) {
            log.info("Outbox cleanup: deleted {} published events older than {} days",
                deleted, outboxProperties.getRetentionDays());
        }
    }

}
