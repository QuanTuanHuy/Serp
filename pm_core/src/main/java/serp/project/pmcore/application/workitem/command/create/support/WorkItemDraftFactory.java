/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support;

import org.springframework.stereotype.Component;
import serp.project.pmcore.application.workitem.command.create.model.CreateWorkItemData;
import serp.project.pmcore.application.workitem.command.create.model.ResolvedWorkItemCreateConfiguration;
import serp.project.pmcore.domain.entity.workitem.WorkItemEntity;

@Component
public class WorkItemDraftFactory {

    public WorkItemEntity buildDraft(Long projectId,
                                     CreateWorkItemData request,
                                     ResolvedWorkItemCreateConfiguration resolvedConfiguration,
                                     Long issueNo,
                                     String key,
                                     String rank,
                                     Long assigneeId,
                                     Long reporterId,
                                     Long securityLevelId) {
        return WorkItemEntity.builder()
                .projectId(projectId)
                .issueTypeId(request.getIssueTypeId())
                .issueNo(issueNo)
                .key(key)
                .summary(request.getSummary())
                .description(request.getDescription())
                .workflowStepId(resolvedConfiguration.initialStep().getId())
                .statusId(resolvedConfiguration.initialStep().getStatusId())
                .priorityId(resolvedConfiguration.priorityId())
                .resolutionId(null)
                .assigneeId(assigneeId)
                .reporterId(reporterId)
                .parentId(request.getParentId())
                .securityLevelId(securityLevelId)
                .dueDate(request.getDueDate())
                .rank(rank)
                .timeOriginalEstimate(request.getTimeOriginalEstimate())
                .timeRemainingEstimate(request.getTimeOriginalEstimate())
                .timeSpent(0L)
                .build();
    }
}
