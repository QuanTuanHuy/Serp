/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import java.util.Objects;

import org.springframework.stereotype.Service;

import serp.project.account.core.domain.entity.OutboxEventEntity;
import serp.project.account.core.domain.enums.OutboxEventStatus;
import serp.project.account.core.port.store.IOutboxEventPort;
import serp.project.account.core.service.IOutboxEventService;

@Service
public class OutboxEventService implements IOutboxEventService {
    private final IOutboxEventPort outboxEventPort;

    public OutboxEventService(IOutboxEventPort outboxEventPort) {
        this.outboxEventPort = outboxEventPort;
    }

    @Override
    public OutboxEventEntity saveEvent(OutboxEventEntity event) {
        Objects.requireNonNull(event, "Outbox event is required");
        Objects.requireNonNull(event.getTenantId(), "Outbox event tenantId is required");
        Objects.requireNonNull(event.getAggregateType(), "Outbox event aggregateType is required");
        Objects.requireNonNull(event.getAggregateId(), "Outbox event aggregateId is required");
        Objects.requireNonNull(event.getEventType(), "Outbox event eventType is required");
        Objects.requireNonNull(event.getTopic(), "Outbox event topic is required");
        Objects.requireNonNull(event.getPayload(), "Outbox event payload is required");

        long now = System.currentTimeMillis();
        if (event.getStatus() == null) {
            event.setStatus(OutboxEventStatus.PENDING);
        }
        if (event.getRetryCount() == null) {
            event.setRetryCount(0);
        }
        if (event.getMaxRetries() == null || event.getMaxRetries() < 1) {
            event.setMaxRetries(OutboxEventEntity.defaultMaxRetries());
        }
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(now);
        }
        if (event.getUpdatedAt() == null) {
            event.setUpdatedAt(event.getCreatedAt());
        }

        return outboxEventPort.save(event);
    }
}
