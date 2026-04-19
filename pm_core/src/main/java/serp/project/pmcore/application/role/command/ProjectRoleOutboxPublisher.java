/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.kernel.utils.JsonUtils;

@Component
@RequiredArgsConstructor
public class ProjectRoleOutboxPublisher {

    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;

    public void publishCreated(Long tenantId, ProjectRoleEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.ProjectRole.EventType.PROJECT_ROLE_CREATED);
    }

    public void publishUpdated(Long tenantId, ProjectRoleEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.ProjectRole.EventType.PROJECT_ROLE_UPDATED);
    }

    public void publishDeleted(Long tenantId, ProjectRoleEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.ProjectRole.EventType.PROJECT_ROLE_DELETED);
    }

    private void saveOutboxEvent(Long tenantId,
                                 ProjectRoleEventPayload payload,
                                 String eventType) {
        long now = System.currentTimeMillis();
        OutboxEventEntity event = OutboxEventEntity.builder()
                .tenantId(tenantId)
                .aggregateType(EventConstants.ProjectRole.AGGREGATE)
                .aggregateId(payload.roleId())
                .eventType(eventType)
                .topic(EventConstants.ProjectRole.TOPIC)
                .partitionKey(String.valueOf(payload.roleId()))
                .payload(jsonUtils.toJson(payload))
                .status(OutboxEventStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        outboxEventService.saveEvent(event);
    }
}
