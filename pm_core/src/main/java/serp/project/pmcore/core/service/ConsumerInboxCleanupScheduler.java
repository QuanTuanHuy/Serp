/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.kernel.property.KafkaConsumerProperties;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsumerInboxCleanupScheduler {

    private final IConsumerInboxService consumerInboxService;
    private final KafkaConsumerProperties kafkaConsumerProperties;

    @Scheduled(cron = "${app.kafka.consumer.inbox-cleanup-cron:0 30 3 * * *}")
    public void cleanupTerminalEvents() {
        long cutoff = Instant.now()
                .minus(kafkaConsumerProperties.getInboxRetentionDays(), ChronoUnit.DAYS)
                .toEpochMilli();

        int deleted = consumerInboxService.deleteTerminalEventsBefore(cutoff);
        if (deleted > 0) {
            log.info(
                    "Consumer inbox cleanup: deleted {} terminal events older than {} days",
                    deleted,
                    kafkaConsumerProperties.getInboxRetentionDays());
        }
    }
}
