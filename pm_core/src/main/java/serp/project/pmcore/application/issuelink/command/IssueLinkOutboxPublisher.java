/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.kernel.utils.JsonUtils;

@Component
@RequiredArgsConstructor
public class IssueLinkOutboxPublisher {

    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;

    public void publishIssueLinkCreated(Long tenantId, IssueLinkEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.IssueLink.EventType.ISSUE_LINK_CREATED);
    }

    public void publishIssueLinkDeleted(Long tenantId, IssueLinkEventPayload payload) {
        saveOutboxEvent(tenantId, payload, EventConstants.IssueLink.EventType.ISSUE_LINK_DELETED);
    }

    private void saveOutboxEvent(Long tenantId, IssueLinkEventPayload payload, String eventType) {
        long now = System.currentTimeMillis();
        OutboxEventEntity event = OutboxEventEntity.builder()
                .tenantId(tenantId)
                .aggregateType(EventConstants.IssueLink.AGGREGATE)
                .aggregateId(payload.issueLinkId())
                .eventType(eventType)
                .topic(EventConstants.IssueLink.TOPIC)
                .partitionKey(String.valueOf(payload.sourceId()))
                .payload(jsonUtils.toJson(payload))
                .status(OutboxEventStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        outboxEventService.saveEvent(event);
    }
}
