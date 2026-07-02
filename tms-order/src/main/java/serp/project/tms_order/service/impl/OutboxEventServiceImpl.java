/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.tms_order.domain.OutboxEvent;
import serp.project.tms_order.enums.OutboxEventStatus;
import serp.project.tms_order.repository.OutboxEventRepository;
import serp.project.tms_order.service.OutboxEventService;

@Service
@RequiredArgsConstructor
public class OutboxEventServiceImpl implements OutboxEventService {
    private final OutboxEventRepository outboxEventRepository;

    @Value("${app.kafka.outbox.max-retries:5}")
    private int configuredMaxRetries;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enqueue(
            String aggregateType,
            String aggregateId,
            String eventType,
            String topic,
            String messageKey,
            String payload,
            Long tenantId
    ) {
        outboxEventRepository.save(OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .topic(topic)
                .messageKey(messageKey)
                .payload(payload)
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .maxRetries(resolveConfiguredMaxRetries())
                .tenantId(tenantId)
                .build());
    }

    private int resolveConfiguredMaxRetries() {
        return configuredMaxRetries > 0 ? configuredMaxRetries : 5;
    }
}
