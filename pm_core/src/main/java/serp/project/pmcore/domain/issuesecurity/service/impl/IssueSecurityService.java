/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuesecurity.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelMemberEntity;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelMemberPort;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;
import serp.project.pmcore.domain.project.port.IProjectRoleActorPort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueSecurityService implements IIssueSecurityService {

    private final IIssueSecurityLevelMemberPort issueSecurityLevelMemberPort;
    private final IProjectRoleActorPort projectRoleActorPort;

    @Override
    public void checkSecurityAccessIfNeeded(ProjectEntity project,
                                            WorkItemEntity workItem,
                                            ProjectPermissionEvaluationContext actorContext,
                                            Long tenantId) {
        if (workItem.getSecurityLevelId() == null) {
            return;
        }
        long securityLevelId = workItem.getSecurityLevelId();

        List<IssueSecurityLevelMemberEntity> members = issueSecurityLevelMemberPort
                .getIssueSecurityLevelMembersByLevelId(securityLevelId, tenantId);

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

        return switch (subjectType) {
            case "user" -> currentUserId != null && String.valueOf(currentUserId).equals(member.getSubjectRef());
            case "group" -> member.getSubjectRef() != null && actorContext.getGroupKeys().contains(member.getSubjectRef());
            case "project_lead" -> currentUserId != null && currentUserId.equals(project.getLeadUserId());
            case "reporter" -> currentUserId != null && currentUserId.equals(workItem.getReporterId());
            case "assignee" -> currentUserId != null && currentUserId.equals(workItem.getAssigneeId());
            case "project_role" -> matchesProjectRole(project.getId(), member.getSubjectRef(), actorContext, tenantId);
            default -> {
                log.warn("Unsupported issue security subject type: {}", member.getSubjectType());
                yield false;
            }
        };
    }

    private boolean matchesProjectRole(Long projectId,
                                       String roleIdRaw,
                                       ProjectPermissionEvaluationContext actorContext,
                                       Long tenantId) {
        if (roleIdRaw == null || roleIdRaw.isBlank()) {
            return false;
        }

        long roleId;
        try {
            roleId = Long.parseLong(roleIdRaw);
        } catch (NumberFormatException ex) {
            log.warn("Invalid project role id in issue security member: {}", roleIdRaw);
            return false;
        }

        List<ProjectRoleActorEntity> actors = projectRoleActorPort
                .getProjectRoleActorsByProjectIdAndRoleId(projectId, roleId, tenantId);
        for (ProjectRoleActorEntity actor : actors) {
            String subjectType = normalizeToken(actor.getSubjectType());
            if ("user".equals(subjectType) && String.valueOf(actorContext.getUserId()).equals(actor.getSubjectId())) {
                return true;
            }
            if ("group".equals(subjectType) && actorContext.getGroupKeys().contains(actor.getSubjectId())) {
                return true;
            }
        }
        return false;
    }

    private String normalizeToken(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        normalized = normalized.replace('-', '_').replace(' ', '_');
        return normalized.toLowerCase(Locale.ROOT);
    }
}
