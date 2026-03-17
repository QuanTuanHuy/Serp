/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.scheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import serp.project.account.core.domain.entity.OutboxEventEntity;
import serp.project.account.core.domain.enums.OutboxEventStatus;
import serp.project.account.core.port.client.IKafkaProducer;
import serp.project.account.core.port.store.IOutboxEventPort;
import serp.project.account.kernel.property.OutboxProperties;

@Component
@Slf4j
public class OutboxPollingPublisher {
    private final IOutboxEventPort outboxEventPort;
    private final IKafkaProducer kafkaProducer;

    private final OutboxProperties outboxProperties;

    public OutboxPollingPublisher(IOutboxEventPort outboxEventPort, IKafkaProducer kafkaProducer,
            OutboxProperties outboxProperties) {
        this.outboxEventPort = outboxEventPort;
        this.kafkaProducer = kafkaProducer;
        this.outboxProperties = outboxProperties;
    }

    @Transactional
    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
    public void pollAndPublish() {
        List<OutboxEventEntity> events = outboxEventPort.getEventsByStatuses(
                List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED),
                outboxProperties.getBatchSize());
        if (events.isEmpty()) {
            return;
        }
        for (var event : events) {
            try {
                kafkaProducer.sendMessageSync(
                        event.getPartitionKey(),
                        event.getPayload(),
                        event.getTopic());
                event.markPublished();
            } catch (Exception ex) {
                event.markFailed(ex.getMessage());
                log.error("[OutboxPollingPublisher] Failed to publish event id={}, type={}, retry={}/{}",
                        event.getId(),
                        event.getEventType(),
                        event.getRetryCount(),
                        event.getMaxRetries(),
                        ex);
            }
        }
        outboxEventPort.batchUpdateStatus(events);
    }

    @Transactional
    @Scheduled(cron = "${app.outbox.cleanup-cron:0 0 3 * * *}")
    public void cleanupPublishedEvents() {
        long cutoffTime = Instant.now()
                .minus(outboxProperties.getRetentionDays(), ChronoUnit.DAYS)
                .toEpochMilli();
        int deletedCount = outboxEventPort.deletePublishedEventsBefore(cutoffTime);
        if (deletedCount > 0) {
            log.info("Cleaned up {} published outbox events before timestamp {}", deletedCount, cutoffTime);
        }
    }
}
