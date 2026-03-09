/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.core.domain.dto.request.CreateWorkItemRequest;
import serp.project.pmcore.core.domain.dto.response.WorkItemResponse;
import serp.project.pmcore.core.domain.entity.OutboxEventEntity;
import serp.project.pmcore.core.domain.entity.ProjectEntity;
import serp.project.pmcore.core.domain.entity.WorkItemEntity;
import serp.project.pmcore.core.domain.entity.WorkflowStepEntity;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.core.service.IIssueTypeSchemeService;
import serp.project.pmcore.core.service.IOutboxEventService;
import serp.project.pmcore.core.service.IPrioritySchemeService;
import serp.project.pmcore.core.service.IProjectService;
import serp.project.pmcore.core.service.IWorkItemService;
import serp.project.pmcore.core.service.IWorkflowSchemeService;
import serp.project.pmcore.core.service.IWorkflowService;
import serp.project.pmcore.kernel.utils.JsonUtils;
import serp.project.pmcore.kernel.utils.LexorankUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkItemUseCase {

    private static final String WORK_ITEM_EVENTS_TOPIC = "serp.pm.workitem.events";

    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final IWorkflowSchemeService workflowSchemeService;
    private final IIssueTypeSchemeService issueTypeSchemeService;
    private final IPrioritySchemeService prioritySchemeService;
    private final IWorkflowService workflowService;
    private final IOutboxEventService outboxEventService;

    private final JsonUtils jsonUtils;

    @Transactional(rollbackFor = Exception.class)
    public WorkItemResponse createWorkItem(CreateWorkItemRequest request, Long tenantId, Long userId) {
        ProjectEntity project = projectService.getProjectById(request.getProjectId(), tenantId);
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new AppException(ErrorCode.PROJECT_ARCHIVED);
        }

        issueTypeSchemeService.validateIssueTypeInScheme(
                project.getIssueTypeSchemeId(),
                request.getIssueTypeId(),
                tenantId);

        Long statusId = request.getStatusId();
        if (statusId == null) {
            Long workflowId = workflowSchemeService.resolveWorkflowId(project.getWorkflowSchemeId(),
                    request.getIssueTypeId(), tenantId);
            WorkflowStepEntity initialStep = workflowService.getInitialWorkflowStep(workflowId, tenantId);
            statusId = initialStep.getStatusId();
        }

        Long priorityId = request.getPriorityId();
        if (priorityId == null) {
            priorityId = prioritySchemeService.resolvePriorityId(project.getPrioritySchemeId(), tenantId);
        }

        workItemService.validateParentHierarchy(
                request.getParentId(),
                request.getIssueTypeId(),
                request.getProjectId(),
                tenantId);

        Long issueNo = workItemService.getNextIssueNumber(request.getProjectId(), tenantId);
        String key = project.getKey() + "-" + issueNo;
        String rank = LexorankUtils.generateInitialRank();

        WorkItemEntity workItem = WorkItemEntity.builder()
                .projectId(request.getProjectId())
                .issueTypeId(request.getIssueTypeId())
                .issueNo(issueNo)
                .key(key)
                .summary(request.getSummary())
                .description(request.getDescription())
                .statusId(statusId)
                .priorityId(priorityId)
                .assigneeId(request.getAssigneeId())
                .reporterId(userId)
                .parentId(request.getParentId())
                .dueDate(request.getDueDate())
                .rank(rank)
                .timeOriginalEstimate(request.getTimeOriginalEstimate())
                .timeRemainingEstimate(request.getTimeOriginalEstimate())
                .timeSpent(0L)
                .build();

        WorkItemEntity saved = workItemService.createWorkItem(workItem, tenantId, userId);

        publishWorkItemEvent("WORK_ITEM_CREATED", saved, tenantId);

        log.info("Work item created: id={}, key={}, projectId={}, tenantId={}",
                saved.getId(), saved.getKey(), saved.getProjectId(), tenantId);

        return toResponse(saved);
    }

    private void publishWorkItemEvent(String eventType, WorkItemEntity workItem, Long tenantId) {
        Map<String, Object> eventPayload = Map.of(
                "eventType", eventType,
                "workItemId", workItem.getId(),
                "workItemKey", workItem.getKey(),
                "projectId", workItem.getProjectId(),
                "tenantId", tenantId,
                "timestamp", System.currentTimeMillis());

        outboxEventService.saveEvent(
                OutboxEventEntity.builder()
                        .tenantId(tenantId)
                        .aggregateType("WORK_ITEM")
                        .aggregateId(workItem.getId())
                        .eventType(eventType)
                        .topic(WORK_ITEM_EVENTS_TOPIC)
                        .partitionKey(workItem.getProjectId().toString())
                        .payload(jsonUtils.toJson(eventPayload))
                        .build());

        log.info("Outbox event saved: {} for work item id={}, key={}",
                eventType, workItem.getId(), workItem.getKey());
    }

    private WorkItemResponse toResponse(WorkItemEntity entity) {
        return WorkItemResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .issueTypeId(entity.getIssueTypeId())
                .issueNo(entity.getIssueNo())
                .key(entity.getKey())
                .summary(entity.getSummary())
                .description(entity.getDescription())
                .statusId(entity.getStatusId())
                .priorityId(entity.getPriorityId())
                .resolutionId(entity.getResolutionId())
                .assigneeId(entity.getAssigneeId())
                .reporterId(entity.getReporterId())
                .parentId(entity.getParentId())
                .dueDate(entity.getDueDate())
                .rank(entity.getRank())
                .timeOriginalEstimate(entity.getTimeOriginalEstimate())
                .timeRemainingEstimate(entity.getTimeRemainingEstimate())
                .timeSpent(entity.getTimeSpent())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
