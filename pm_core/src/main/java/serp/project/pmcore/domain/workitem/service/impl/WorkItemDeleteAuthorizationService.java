/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelEntity;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelMemberEntity;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelMemberPort;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemDeleteAuthorizationService;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkItemDeleteAuthorizationService implements IWorkItemDeleteAuthorizationService {

    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    private final IIssueSecurityLevelPort issueSecurityLevelPort;
    private final IIssueSecurityLevelMemberPort issueSecurityLevelMemberPort;

    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;

    @Override
    public void checkDeletePermission(ProjectEntity project,
                                      ProjectPermissionEvaluationContext actorContext) {
        projectPermissionEvaluationService.checkPermission(
                project,
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS
        );
        projectPermissionEvaluationService.checkPermission(
                project,
                actorContext,
                ProjectPermissionKeys.DELETE_ISSUES
        );
    }

    @Override
    public void checkDeleteSecurityAccess(ProjectEntity project,
                                          WorkItemEntity workItem,
                                          ProjectPermissionEvaluationContext actorContext) {
        if (workItem.getSecurityLevelId() == null) {
            log.debug("[WorkItemDeleteAuthorizationService] Work item {} has no security level, no need to check security access",
                    workItem.getId());
            return;
        }
        if (project.getIssueSecuritySchemeId() == null) {
            log.error("[WorkItemDeleteAuthorizationService] Work item {} has security level but project {} has no issue security scheme",
                    workItem.getId(), project.getId());
            throw new BusinessRuleViolationException(
                    DomainErrorCode.SECURITY_LEVEL_NOT_IN_SCHEME,
                    "Work item has security level but project has no issue security scheme: projectId="
                            + project.getId() + ", workItemId=" + workItem.getId()
            );
        }

        IssueSecurityLevelEntity targetLevel = issueSecurityLevelPort.
                getIssueSecurityLevelByIdAndSchemeId(
                        workItem.getSecurityLevelId(),
                        project.getIssueSecuritySchemeId(),
                        project.getTenantId())
                .orElseThrow(() -> new BusinessRuleViolationException(
                        DomainErrorCode.SECURITY_LEVEL_NOT_IN_SCHEME,
                        "Security level is not valid for project issue security scheme: projectId="
                                + project.getId() + ", securityLevelId=" + workItem.getSecurityLevelId()
                ));

        List<IssueSecurityLevelMemberEntity> members =
                issueSecurityLevelMemberPort.getIssueSecurityLevelMembersByLevelId(
                        targetLevel.getId(),
                        project.getTenantId()
                );
        boolean allowed = members.stream()
                .anyMatch(member -> matchesMember(project, actorContext, member));
        if (!allowed) {
            throw AccessDeniedException.securityLevel(workItem.getId());
        }
    }

    private boolean matchesMember(ProjectEntity project,
                                  ProjectPermissionEvaluationContext actorContext,
                                  IssueSecurityLevelMemberEntity member) {
        String subjectType = normalizeUpper(member.getSubjectType());
        if (subjectType == null) {
            return false;
        }
        return switch (subjectType) {
            case "USER" -> Objects.equals(String.valueOf(actorContext.getUserId()), member.getSubjectRef());
            case "GROUP" -> safeGroupKeys(actorContext.getGroupKeys()).contains(normalizeToken(member.getSubjectRef()));
            case "PROJECT_LEAD" -> Objects.equals(project.getLeadUserId(), actorContext.getUserId());
            case "REPORTER" -> Objects.equals(actorContext.getReporterUserId(), actorContext.getUserId());
            case "ASSIGNEE" -> Objects.equals(actorContext.getAssigneeUserId(), actorContext.getUserId());
            case "PROJECT_ROLE" -> matchesProjectRoleMember(project, actorContext, member.getSubjectRef());
            case "USER_CUSTOM_FIELD_VALUE", "GROUP_CUSTOM_FIELD_VALUE" -> {
                log.debug("Issue security custom-field subject is not implemented in delete flow yet: memberId={}",
                        member.getId());
                yield false;
            }
            default -> {
                log.debug("Unsupported issue security subject type: memberId={}, subjectType={}",
                        member.getId(), member.getSubjectType());
                yield false;
            }
        };
    }

    private boolean matchesProjectRoleMember(ProjectEntity project,
                                             ProjectPermissionEvaluationContext actorContext,
                                             String roleName) {
        if (roleName == null || roleName.isBlank() || actorContext.getUserId() == null) {
            return false;
        }

        List<ProjectRoleEntity> roles =
                projectRoleService.getProjectRolesByNameIncludingSystem(roleName, project.getTenantId());
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
        String normalized = value.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeUpper(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        return normalized.isEmpty() ? null : normalized;
    }

}
