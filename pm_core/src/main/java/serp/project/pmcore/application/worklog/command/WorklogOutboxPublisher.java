/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.kernel.utils.JsonUtils;

@Component
@RequiredArgsConstructor
public class WorklogOutboxPublisher {

    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;

    public void publishWorklogCreated(Long tenantId, WorklogEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.Worklog.EventType.WORKLOG_CREATED);
    }

    public void publishWorklogUpdated(Long tenantId, WorklogEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.Worklog.EventType.WORKLOG_UPDATED);
    }

    public void publishWorklogDeleted(Long tenantId, WorklogEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.Worklog.EventType.WORKLOG_DELETED);
    }

    private void saveOutboxEvent(Long tenantId, WorklogEventPayload payload, String eventType) {
        long now = System.currentTimeMillis();
        OutboxEventEntity event = OutboxEventEntity.builder()
                .tenantId(tenantId)
                .aggregateType(EventConstants.Worklog.AGGREGATE)
                .aggregateId(payload.worklogId())
                .eventType(eventType)
                .topic(EventConstants.Worklog.TOPIC)
                .partitionKey(String.valueOf(payload.projectId()))
                .payload(jsonUtils.toJson(payload))
                .status(OutboxEventStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        outboxEventService.saveEvent(event);
    }
}
