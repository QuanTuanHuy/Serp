/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.application.workitem.command.create.model.CreateWorkItemData;
import serp.project.pmcore.application.workitem.command.create.model.ResolvedWorkItemCreateConfiguration;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelEntity;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecuritySchemeEntity;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.port.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemePort;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemePort;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowVersionPort;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkItemCreateConfigurationResolver {

    private final IIssueTypePort issueTypePort;
    private final IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    private final IWorkflowSchemePort workflowSchemePort;
    private final IWorkflowSchemeItemPort workflowSchemeItemPort;
    private final IWorkflowPort workflowPort;
    private final IWorkflowVersionPort workflowVersionPort;
    private final IWorkflowStepPort workflowStepPort;
    private final IPrioritySchemePort prioritySchemePort;
    private final IPrioritySchemeItemPort prioritySchemeItemPort;
    private final IIssueSecuritySchemePort issueSecuritySchemePort;
    private final IIssueSecurityLevelPort issueSecurityLevelPort;

    public ResolvedWorkItemCreateConfiguration resolve(ProjectEntity project,
                                                       CreateWorkItemData request,
                                                       Long tenantId) {
        ensureProjectWritable(project);

        IssueTypeEntity issueType = issueTypePort.getIssueTypeById(request.getIssueTypeId(), tenantId)
                .orElseThrow(() -> ResourceNotFoundException.issueType(request.getIssueTypeId()));
        validateIssueTypeInProjectScheme(project, issueType.getId(), tenantId);
        validateParentRequirement(issueType, request.getParentId());

        WorkflowStepEntity initialStep = resolveInitialWorkflowStep(project, issueType.getId(), tenantId);
        Long priorityId = resolvePriorityId(project, request.getPriorityId(), tenantId);

        return new ResolvedWorkItemCreateConfiguration(issueType, initialStep, priorityId);
    }

    public Long resolveSecurityLevelId(ProjectEntity project,
                                       Long requestedSecurityLevelId,
                                       Long tenantId) {
        if (project.getIssueSecuritySchemeId() == null) {
            return requestedSecurityLevelId == null ? null : missingIssueSecurityScheme(project.getId());
        }

        IssueSecuritySchemeEntity issueSecurityScheme = issueSecuritySchemePort
                .getIssueSecuritySchemeById(project.getIssueSecuritySchemeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                        "Issue security scheme not found: id=" + project.getIssueSecuritySchemeId()
                ));

        if (requestedSecurityLevelId == null) {
            return issueSecurityScheme.getDefaultLevelId();
        }

        boolean inScheme = issueSecurityLevelPort.getIssueSecurityLevelsBySchemeId(
                        project.getIssueSecuritySchemeId(),
                        tenantId
                )
                .stream()
                .map(IssueSecurityLevelEntity::getId)
                .anyMatch(requestedSecurityLevelId::equals);

        if (!inScheme) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.SECURITY_LEVEL_NOT_IN_SCHEME,
                    "Security level is not allowed in project scheme: projectId=" + project.getId()
                            + ", securityLevelId=" + requestedSecurityLevelId
            );
        }

        return requestedSecurityLevelId;
    }

    private void ensureProjectWritable(ProjectEntity project) {
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
    }

    private void validateIssueTypeInProjectScheme(ProjectEntity project, Long issueTypeId, Long tenantId) {
        if (project.getIssueTypeSchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_TYPE_SCHEME_NOT_FOUND,
                    "Project has no issue type scheme binding: projectId=" + project.getId()
            );
        }

        boolean exists = issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(
                        project.getIssueTypeSchemeId(),
                        tenantId
                )
                .stream()
                .map(IssueTypeSchemeItemEntity::getIssueTypeId)
                .anyMatch(issueTypeId::equals);

        if (!exists) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.ISSUE_TYPE_NOT_IN_SCHEME,
                    "Issue type is not allowed in project scheme: projectId=" + project.getId() + ", issueTypeId=" + issueTypeId
            );
        }
    }

    private void validateParentRequirement(IssueTypeEntity issueType, Long parentId) {
        if (issueType.getHierarchyLevel() == null) {
            throw new DomainValidationException(
                    DomainErrorCode.INVALID_PARENT_HIERARCHY,
                    "Issue type hierarchy level is missing: issueTypeId=" + issueType.getId()
            );
        }

        if (issueType.getHierarchyLevel() == 0 && parentId == null) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.INVALID_PARENT_HIERARCHY,
                    "Subtask issue type requires parent_id: issueTypeId=" + issueType.getId()
            );
        }

        if (issueType.getHierarchyLevel() >= 2 && parentId != null) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.INVALID_PARENT_HIERARCHY,
                    "Issue types with hierarchy level >= 2 cannot set parent_id: issueTypeId=" + issueType.getId()
                            + ", hierarchyLevel=" + issueType.getHierarchyLevel()
            );
        }
    }

    private WorkflowStepEntity resolveInitialWorkflowStep(ProjectEntity project, Long issueTypeId, Long tenantId) {
        if (project.getWorkflowSchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.WORKFLOW_SCHEME_NOT_FOUND,
                    "Project has no workflow scheme binding: projectId=" + project.getId()
            );
        }

        WorkflowSchemeEntity workflowScheme = workflowSchemePort
                .getWorkflowSchemeById(project.getWorkflowSchemeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.WORKFLOW_SCHEME_NOT_FOUND,
                        "Workflow scheme not found: id=" + project.getWorkflowSchemeId()
                ));

        Long workflowId = workflowSchemeItemPort.getWorkflowSchemeItemsBySchemeId(
                        project.getWorkflowSchemeId(),
                        tenantId
                )
                .stream()
                .filter(item -> issueTypeId.equals(item.getIssueTypeId()))
                .map(WorkflowSchemeItemEntity::getWorkflowId)
                .findFirst()
                .orElse(workflowScheme.getDefaultWorkflowId());

        if (workflowId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_SCHEME_COVERAGE_MISSING,
                    "Workflow scheme does not cover issueTypeId=" + issueTypeId + " for projectId=" + project.getId()
            );
        }

        WorkflowEntity workflow = workflowPort.getWorkflowById(workflowId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.workflow(workflowId));

        if (workflow.getCurrentPublishedVersionId() == null) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_NOT_ACTIVE,
                    "Workflow has no published version: workflowId=" + workflowId
            );
        }

        WorkflowVersionEntity publishedVersion = workflowVersionPort
                .getWorkflowVersionById(workflow.getCurrentPublishedVersionId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.WORKFLOW_NOT_ACTIVE,
                        "Published workflow version not found: id=" + workflow.getCurrentPublishedVersionId()
                ));

        if (!publishedVersion.isActive()) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_NOT_ACTIVE,
                    "Workflow current version is not published: workflowId=" + workflowId
                            + ", versionId=" + publishedVersion.getId()
            );
        }

        return workflowStepPort.getInitialStepByWorkflowVersionId(workflow.getCurrentPublishedVersionId(), tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.WORKFLOW_NO_INITIAL_STEP,
                        "Workflow must have exactly one initial step: workflowId=" + workflowId
                ));
    }

    private Long resolvePriorityId(ProjectEntity project, Long requestedPriorityId, Long tenantId) {
        if (project.getPrioritySchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND,
                    "Project has no priority scheme binding: projectId=" + project.getId()
            );
        }

        PrioritySchemeEntity priorityScheme = prioritySchemePort
                .getPrioritySchemeById(project.getPrioritySchemeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND,
                        "Priority scheme not found: id=" + project.getPrioritySchemeId()
                ));

        List<PrioritySchemeItemEntity> priorityItems = prioritySchemeItemPort
                .getPrioritySchemeItemsBySchemeId(project.getPrioritySchemeId(), tenantId);

        if (requestedPriorityId != null) {
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

        if (priorityScheme.getDefaultPriorityId() == null) {
            throw new DomainValidationException(
                    DomainErrorCode.DEFAULT_PRIORITY_NOT_CONFIGURED,
                    "Priority scheme has no default priority: schemeId=" + project.getPrioritySchemeId()
            );
        }

        return priorityScheme.getDefaultPriorityId();
    }

    private Long missingIssueSecurityScheme(Long projectId) {
        throw new ResourceNotFoundException(
                DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                "Project has no issue security scheme binding: projectId=" + projectId
        );
    }
}
