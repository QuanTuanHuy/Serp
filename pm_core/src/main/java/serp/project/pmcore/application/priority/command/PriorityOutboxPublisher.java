/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.kernel.utils.JsonUtils;

@Component
@RequiredArgsConstructor
public class PriorityOutboxPublisher {

    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;

    public void publishPriorityCreated(Long tenantId, PriorityEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.Priority.EventType.PRIORITY_CREATED);
    }

    public void publishPriorityUpdated(Long tenantId, PriorityEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.Priority.EventType.PRIORITY_UPDATED);
    }

    public void publishPriorityDeleted(Long tenantId, PriorityEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.Priority.EventType.PRIORITY_DELETED);
    }

    private void saveOutboxEvent(Long tenantId,
                                 PriorityEventPayload payload,
                                 String eventType) {
        long now = System.currentTimeMillis();
        OutboxEventEntity event = OutboxEventEntity.builder()
                .tenantId(tenantId)
                .aggregateType(EventConstants.Priority.AGGREGATE)
                .aggregateId(payload.priorityId())
                .eventType(eventType)
                .topic(EventConstants.Priority.TOPIC)
                .partitionKey(String.valueOf(payload.priorityId()))
                .payload(jsonUtils.toJson(payload))
                .status(OutboxEventStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        outboxEventService.saveEvent(event);
    }
}
