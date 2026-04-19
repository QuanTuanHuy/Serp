/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.kernel.utils.JsonUtils;

@Component
@RequiredArgsConstructor
public class ProjectCategoryOutboxPublisher {

    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;

    public void publishCreated(Long tenantId, ProjectCategoryEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.ProjectCategory.EventType.PROJECT_CATEGORY_CREATED);
    }

    public void publishUpdated(Long tenantId, ProjectCategoryEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.ProjectCategory.EventType.PROJECT_CATEGORY_UPDATED);
    }

    public void publishDeleted(Long tenantId, ProjectCategoryEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.ProjectCategory.EventType.PROJECT_CATEGORY_DELETED);
    }

    private void saveOutboxEvent(Long tenantId,
                                 ProjectCategoryEventPayload payload,
                                 String eventType) {
        long now = System.currentTimeMillis();
        OutboxEventEntity event = OutboxEventEntity.builder()
                .tenantId(tenantId)
                .aggregateType(EventConstants.ProjectCategory.AGGREGATE)
                .aggregateId(payload.categoryId())
                .eventType(eventType)
                .topic(EventConstants.ProjectCategory.TOPIC)
                .partitionKey(String.valueOf(payload.categoryId()))
                .payload(jsonUtils.toJson(payload))
                .status(OutboxEventStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        outboxEventService.saveEvent(event);
    }
}
