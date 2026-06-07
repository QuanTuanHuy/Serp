/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.people.replace;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.Unit;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.user.service.IUserService;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReplaceProjectPersonRolesCommandHandler implements ICommandHandler<ReplaceProjectPersonRolesCommand, Unit> {

    private static final String USER_SUBJECT_TYPE = "USER";

    private final IProjectService projectService;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IUserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Unit handle(ReplaceProjectPersonRolesCommand command) {
        Set<Long> roleIds = validateRoleIds(command);
        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        ensureProjectWritable(project);
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                buildEvaluationContext(command.userId(), command.groupKeys()),
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );
        validateUserExists(command.personUserId());
        roleIds.forEach(roleId -> projectRoleService.getProjectRoleByIdIncludingSystem(roleId, command.tenantId()));

        String subjectId = String.valueOf(command.personUserId());
        projectRoleActorService.removeUserActorsByProject(command.tenantId(), command.projectId(), subjectId, command.userId());
        roleIds.forEach(roleId -> projectRoleActorService.assignActorIfAbsent(
                command.tenantId(),
                command.projectId(),
                roleId,
                USER_SUBJECT_TYPE,
                subjectId,
                command.userId()
        ));
        return Unit.VALUE;
    }

    private Set<Long> validateRoleIds(ReplaceProjectPersonRolesCommand command) {
        if (command.roleIds() == null || command.roleIds().isEmpty()) {
            throw new DomainValidationException(DomainErrorCode.ROLE_ACTOR_SUBJECT_INVALID, "roleIds is required");
        }
        return new LinkedHashSet<>(command.roleIds());
    }

    private void validateUserExists(Long userId) {
        if (userService.getUserById(userId) == null) {
            throw ResourceNotFoundException.user(userId);
        }
    }

    private void ensureProjectWritable(ProjectEntity project) {
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
    }

    private ProjectPermissionEvaluationContext buildEvaluationContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys)
                .build();
    }
}
