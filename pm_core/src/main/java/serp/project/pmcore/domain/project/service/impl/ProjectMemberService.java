/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.IProjectRoleActorPort;
import serp.project.pmcore.domain.project.service.IProjectMemberService;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectMemberService implements IProjectMemberService {

    private final IProjectRoleActorPort projectRoleActorPort;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Override
    public List<Long> listAssignableMembers(ProjectEntity project) {
        if (project == null || project.getTenantId() == null || project.getId() == null) {
            return List.of();
        }
        ProjectPermissionSubject subject = ProjectPermissionSubject.from(project);
        return projectRoleActorPort
                .getProjectRoleActorsByProjectIdAndSubjectType(
                        project.getId(),
                        ProjectRoleActorSubjectType.USER.name(),
                        project.getTenantId())
                .stream()
                .map(actor -> parseUserId(actor.getSubjectId()))
                .filter(Objects::nonNull)
                .distinct()
                .filter(userId -> projectPermissionEvaluationService.hasPermission(
                        subject,
                        ProjectPermissionEvaluationContext.builder().userId(userId).build(),
                        ProjectPermissionKeys.ASSIGNABLE_USER))
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private Long parseUserId(String subjectId) {
        if (subjectId == null || subjectId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(subjectId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
