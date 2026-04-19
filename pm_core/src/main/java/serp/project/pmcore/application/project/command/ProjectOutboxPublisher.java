/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.kernel.utils.JsonUtils;

@Component
@RequiredArgsConstructor
public class ProjectOutboxPublisher {

    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;

    public void publishProjectUpdated(Long tenantId, ProjectEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.Project.EventType.PROJECT_UPDATED);
    }

    private void saveOutboxEvent(Long tenantId,
                                 ProjectEventPayload payload,
                                 String eventType) {
        long now = System.currentTimeMillis();
        OutboxEventEntity event = OutboxEventEntity.builder()
                .tenantId(tenantId)
                .aggregateType(EventConstants.Project.AGGREGATE)
                .aggregateId(payload.projectId())
                .eventType(eventType)
                .topic(EventConstants.Project.TOPIC)
                .partitionKey(String.valueOf(payload.projectId()))
                .payload(jsonUtils.toJson(payload))
                .status(OutboxEventStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        outboxEventService.saveEvent(event);
    }
}
