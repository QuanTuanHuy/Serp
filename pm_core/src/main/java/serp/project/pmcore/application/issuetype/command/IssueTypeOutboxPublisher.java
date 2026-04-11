/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.kernel.utils.JsonUtils;

@Component
@RequiredArgsConstructor
public class IssueTypeOutboxPublisher {

    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;

    public void publishIssueTypeCreated(Long tenantId, IssueTypeEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.IssueType.EventType.ISSUE_TYPE_CREATED);
    }

    public void publishIssueTypeUpdated(Long tenantId, IssueTypeEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.IssueType.EventType.ISSUE_TYPE_UPDATED);
    }

    public void publishIssueTypeDeleted(Long tenantId, IssueTypeEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.IssueType.EventType.ISSUE_TYPE_DELETED);
    }

    private void saveOutboxEvent(Long tenantId,
                                 IssueTypeEventPayload payload,
                                 String eventType) {
        long now = System.currentTimeMillis();
        OutboxEventEntity event = OutboxEventEntity.builder()
                .tenantId(tenantId)
                .aggregateType(EventConstants.IssueType.AGGREGATE)
                .aggregateId(payload.issueTypeId())
                .eventType(eventType)
                .topic(EventConstants.IssueType.TOPIC)
                .partitionKey(String.valueOf(payload.issueTypeId()))
                .payload(jsonUtils.toJson(payload))
                .status(OutboxEventStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        outboxEventService.saveEvent(event);
    }
}
