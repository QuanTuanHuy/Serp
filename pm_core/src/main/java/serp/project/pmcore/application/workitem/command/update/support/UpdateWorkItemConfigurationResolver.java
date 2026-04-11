/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.update.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.application.workitem.command.update.internal.UpdateWorkItemData;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.port.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateWorkItemConfigurationResolver {

    private final IPrioritySchemeItemPort prioritySchemeItemPort;
    private final IIssueSecurityLevelPort issueSecurityLevelPort;

    public Long resolvePriorityId(ProjectEntity project,
                                  WorkItemEntity workItem,
                                  UpdateWorkItemData data,
                                  Long tenantId) {
        if (!data.hasSystemField(WorkItemFieldConstants.PRIORITY_ID)) {
            return workItem.getPriorityId();
        }

        Long requestedPriorityId = asNullablePositiveLong(data.getSystemField(WorkItemFieldConstants.PRIORITY_ID));
        if (requestedPriorityId == null) {
            return null;
        }
        if (project.getPrioritySchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND,
                    "Project has no priority scheme binding: projectId=" + project.getId()
            );
        }

        List<PrioritySchemeItemEntity> priorityItems = prioritySchemeItemPort
                .getPrioritySchemeItemsBySchemeId(project.getPrioritySchemeId(), tenantId);
        boolean inScheme = priorityItems.stream()
                .map(PrioritySchemeItemEntity::getPriorityId)
                .anyMatch(requestedPriorityId::equals);
        if (!inScheme) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PRIORITY_NOT_IN_SCHEME,
                    "Priority is not allowed in project scheme: projectId=" + project.getId() + ", priorityId=" + requestedPriorityId
            );
        }

        return requestedPriorityId;
    }

    public Long resolveSecurityLevelId(ProjectEntity project,
                                       WorkItemEntity workItem,
                                       UpdateWorkItemData data,
                                       Long tenantId) {
        if (!data.hasSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID)) {
            return workItem.getSecurityLevelId();
        }

        Long requestedSecurityLevelId = asNullablePositiveLong(data.getSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID));
        if (requestedSecurityLevelId == null) {
            return null;
        }
        if (project.getIssueSecuritySchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                    "Project has no issue security scheme binding: projectId=" + project.getId()
            );
        }

        boolean inScheme = issueSecurityLevelPort
                .getIssueSecurityLevelByIdAndSchemeId(requestedSecurityLevelId, project.getIssueSecuritySchemeId(), tenantId)
                .isPresent();
        if (!inScheme) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.SECURITY_LEVEL_NOT_IN_SCHEME,
                    "Security level is not allowed in project scheme: projectId=" + project.getId() + ", securityLevelId=" + requestedSecurityLevelId
            );
        }

        return requestedSecurityLevelId;
    }

    private Long asNullablePositiveLong(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (!(rawValue instanceof Number number)) {
            throw new IllegalArgumentException("Expected positive numeric value");
        }
        long value = number.longValue();
        if (value <= 0) {
            throw new IllegalArgumentException("Expected positive numeric value");
        }
        return value;
    }
}
