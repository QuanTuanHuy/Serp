/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuesecurity.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelEntity;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelMemberEntity;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelMemberPort;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueSecurityService implements IIssueSecurityService {

    private final IIssueSecurityLevelPort issueSecurityLevelPort;
    private final IIssueSecurityLevelMemberPort issueSecurityLevelMemberPort;
    private final IIssueSecuritySchemePort issueSecuritySchemePort;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;

    @Override
    public void checkSecurityAccessIfNeeded(ProjectEntity project,
                                            WorkItemEntity workItem,
                                            ProjectPermissionEvaluationContext actorContext,
                                            Long tenantId) {
        if (workItem.getSecurityLevelId() == null) {
            return;
        }

        if (project.getIssueSecuritySchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                    "Work item has security level but project has no issue security scheme: projectId="
                            + project.getId() + ", workItemId=" + workItem.getId()
            );
        }

        long securityLevelId = workItem.getSecurityLevelId();
        IssueSecurityLevelEntity level = issueSecurityLevelPort
                .getIssueSecurityLevelByIdAndSchemeId(securityLevelId, project.getIssueSecuritySchemeId(), tenantId)
                .orElseThrow(() -> new BusinessRuleViolationException(
                        DomainErrorCode.SECURITY_LEVEL_NOT_IN_SCHEME,
                        "Security level is not valid for project issue security scheme: projectId="
                                + project.getId() + ", securityLevelId=" + securityLevelId
                ));

        List<IssueSecurityLevelMemberEntity> members = issueSecurityLevelMemberPort
                .getIssueSecurityLevelMembersByLevelId(level.getId(), tenantId);

        boolean granted = members.stream().anyMatch(member ->
                matchesMember(project, workItem, actorContext, member, tenantId));
        log.info("Checked issue security access for workItemId={}, securityLevelId={}, userId={}, granted={}",
                workItem.getId(), securityLevelId, actorContext.getUserId(), granted);
        if (!granted) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.WORK_ITEM_SECURITY_ACCESS_DENIED,
                    "Issue security access denied for workItemId=" + workItem.getId()
                            + ", securityLevelId=" + workItem.getSecurityLevelId()
            );
        }
    }

    private boolean matchesMember(ProjectEntity project,
                                  WorkItemEntity workItem,
                                  ProjectPermissionEvaluationContext actorContext,
                                  IssueSecurityLevelMemberEntity member,
                                  Long tenantId) {
        String subjectType = normalizeToken(member.getSubjectType());
        Long currentUserId = actorContext.getUserId();
        Long reporterUserId = actorContext.getReporterUserId() != null
                ? actorContext.getReporterUserId()
                : workItem.getReporterId();
        Long assigneeUserId = actorContext.getAssigneeUserId() != null
                ? actorContext.getAssigneeUserId()
                : workItem.getAssigneeId();

        return switch (subjectType) {
            case "user" -> currentUserId != null && String.valueOf(currentUserId).equals(member.getSubjectRef());
            case "group" -> safeGroupKeys(actorContext.getGroupKeys()).contains(normalizeToken(member.getSubjectRef()));
            case "project_lead" -> currentUserId != null && currentUserId.equals(project.getLeadUserId());
            case "reporter" -> currentUserId != null && currentUserId.equals(reporterUserId);
            case "assignee" -> currentUserId != null && currentUserId.equals(assigneeUserId);
            case "project_role" -> matchesProjectRole(project, actorContext, member.getSubjectRef());
            case "user_custom_field_value", "group_custom_field_value" -> {
                log.debug("Issue security custom-field subject is not implemented yet: memberId={}", member.getId());
                yield false;
            }
            default -> {
                log.warn("Unsupported issue security subject type: {}", member.getSubjectType());
                yield false;
            }
        };
    }

    @Override
    public Long resolveDefaultSecurityLevelId(Long issueSecuritySchemeId, Long tenantId) {
        if (issueSecuritySchemeId == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                    "Issue security scheme binding is required"
            );
        }

        return issueSecuritySchemePort
                .getIssueSecuritySchemeById(issueSecuritySchemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                        "Issue security scheme not found: id=" + issueSecuritySchemeId
                ))
                .getDefaultLevelId();
    }

    @Override
    public Long validateSecurityLevelId(Long issueSecuritySchemeId, Long requestedSecurityLevelId, Long tenantId) {
        if (requestedSecurityLevelId == null) {
            return null;
        }
        if (issueSecuritySchemeId == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                    "Issue security scheme binding is required"
            );
        }

        boolean inScheme = issueSecurityLevelPort
                .getIssueSecurityLevelByIdAndSchemeId(requestedSecurityLevelId, issueSecuritySchemeId, tenantId)
                .isPresent();
        if (!inScheme) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.SECURITY_LEVEL_NOT_IN_SCHEME,
                    "Security level is not allowed in issue security scheme: schemeId=" + issueSecuritySchemeId
                            + ", securityLevelId=" + requestedSecurityLevelId
            );
        }

        return requestedSecurityLevelId;
    }

    private boolean matchesProjectRole(ProjectEntity project,
                                       ProjectPermissionEvaluationContext actorContext,
                                       String roleName) {
        if (roleName == null || roleName.isBlank() || actorContext.getUserId() == null) {
            return false;
        }

        List<ProjectRoleEntity> roles = projectRoleService.getProjectRolesByNameIncludingSystem(roleName, project.getTenantId());
        if (roles.isEmpty()) {
            return false;
        }

        for (ProjectRoleEntity role : roles) {
            if (projectRoleActorService.hasRoleAssignment(
                    project.getTenantId(),
                    project.getId(),
                    role.getId(),
                    ProjectRoleActorSubjectType.USER.name(),
                    String.valueOf(actorContext.getUserId())
            )) {
                return true;
            }

            for (String groupKey : safeGroupKeys(actorContext.getGroupKeys())) {
                if (projectRoleActorService.hasRoleAssignment(
                        project.getTenantId(),
                        project.getId(),
                        role.getId(),
                        ProjectRoleActorSubjectType.GROUP.name(),
                        groupKey
                )) {
                    return true;
                }
            }
        }

        return false;
    }

    private Set<String> safeGroupKeys(Set<String> groupKeys) {
        if (groupKeys == null || groupKeys.isEmpty()) {
            return Set.of();
        }
        return groupKeys.stream()
                .map(this::normalizeToken)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private String normalizeToken(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        normalized = normalized.replace('-', '_').replace(' ', '_');
        normalized = normalized.toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }
}
