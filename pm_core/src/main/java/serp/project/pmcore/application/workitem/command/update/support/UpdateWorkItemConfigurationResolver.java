/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.update.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.application.workitem.command.update.internal.UpdateWorkItemData;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.util.WorkItemFieldValueUtils;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

@Service
@RequiredArgsConstructor
public class UpdateWorkItemConfigurationResolver {

    private final IPrioritySchemeService prioritySchemeService;
    private final IIssueSecurityService issueSecurityService;

    public Long resolvePriorityId(ProjectEntity project,
                                  WorkItemEntity workItem,
                                  UpdateWorkItemData data,
                                  Long tenantId) {
        if (!data.hasSystemField(WorkItemFieldConstants.PRIORITY_ID)) {
            return workItem.getPriorityId();
        }

        Long requestedPriorityId = WorkItemFieldValueUtils.asNullablePositiveLong(
                data.getSystemField(WorkItemFieldConstants.PRIORITY_ID));
        if (requestedPriorityId == null) {
            return null;
        }
        if (project.getPrioritySchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND,
                    "Project has no priority scheme binding: projectId=" + project.getId()
            );
        }

        return prioritySchemeService.validatePriorityIdInScheme(project.getPrioritySchemeId(), requestedPriorityId, tenantId);
    }

    public Long resolveSecurityLevelId(ProjectEntity project,
                                       WorkItemEntity workItem,
                                       UpdateWorkItemData data,
                                       Long tenantId) {
        if (!data.hasSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID)) {
            return workItem.getSecurityLevelId();
        }

        Long requestedSecurityLevelId = WorkItemFieldValueUtils.asNullablePositiveLong(
                data.getSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID));
        if (requestedSecurityLevelId == null) {
            return null;
        }
        if (project.getIssueSecuritySchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                    "Project has no issue security scheme binding: projectId=" + project.getId()
            );
        }

        return issueSecurityService.validateSecurityLevelId(
                project.getIssueSecuritySchemeId(),
                requestedSecurityLevelId,
                tenantId
        );
    }
}
