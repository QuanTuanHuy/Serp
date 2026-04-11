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
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.util.WorkItemFieldValueUtils;

@Service
@RequiredArgsConstructor
public class UpdateWorkItemConfigurationResolver {

    private final IPrioritySchemeService prioritySchemeService;
    private final IIssueSecurityService issueSecurityService;

    public Long resolvePriorityId(Long projectId,
                                  Long prioritySchemeId,
                                  Long currentPriorityId,
                                  UpdateWorkItemData data,
                                  Long tenantId) {
        if (!data.hasSystemField(WorkItemFieldConstants.PRIORITY_ID)) {
            return currentPriorityId;
        }

        Long requestedPriorityId = WorkItemFieldValueUtils.asNullablePositiveLong(
                data.getSystemField(WorkItemFieldConstants.PRIORITY_ID));
        if (requestedPriorityId == null) {
            return null;
        }
        if (prioritySchemeId == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND,
                    "Project has no priority scheme binding: projectId=" + projectId
            );
        }

        return prioritySchemeService.validatePriorityIdInScheme(prioritySchemeId, requestedPriorityId, tenantId);
    }

    public Long resolveSecurityLevelId(Long projectId,
                                       Long issueSecuritySchemeId,
                                       Long currentSecurityLevelId,
                                       UpdateWorkItemData data,
                                       Long tenantId) {
        if (!data.hasSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID)) {
            return currentSecurityLevelId;
        }

        Long requestedSecurityLevelId = WorkItemFieldValueUtils.asNullablePositiveLong(
                data.getSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID));
        if (requestedSecurityLevelId == null) {
            return null;
        }
        if (issueSecuritySchemeId == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                    "Project has no issue security scheme binding: projectId=" + projectId
            );
        }

        return issueSecurityService.validateSecurityLevelId(
                issueSecuritySchemeId,
                requestedSecurityLevelId,
                tenantId
        );
    }
}
