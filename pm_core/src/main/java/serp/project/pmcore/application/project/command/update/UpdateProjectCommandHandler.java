/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.update;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import serp.project.pmcore.application.project.command.ProjectEventPayload;
import serp.project.pmcore.application.project.command.ProjectOutboxPublisher;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.PermissionSeedConstants;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UpdateProjectCommandHandler implements ICommandHandler<UpdateProjectCommand, UpdateProjectResult> {

    private final IProjectService projectService;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final UpdateProjectValidator updateProjectValidator;
    private final ProjectOutboxPublisher projectOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateProjectResult handle(UpdateProjectCommand command) {
        ProjectEntity existingProject = projectService.getProjectById(command.projectId(), command.tenantId());
        ensureProjectWritable(existingProject);

        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(existingProject),
                ProjectPermissionEvaluationContext.builder()
                        .userId(command.userId())
                        .groupKeys(command.groupKeys())
                        .build(),
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );

        updateProjectValidator.validate(command, existingProject);

        Long previousLeadUserId = existingProject.getLeadUserId();
        ProjectEntity updatedProject = projectService.updateProject(
                command.projectId(),
                command.data(),
                command.tenantId(),
                command.userId()
        );

        if (leadChanged(previousLeadUserId, updatedProject.getLeadUserId())) {
            assignLeadToAdministratorsRole(updatedProject, command.userId());
            ensureLeadAdminAccess(updatedProject);
        }

        projectOutboxPublisher.publishProjectUpdated(
                command.tenantId(),
                ProjectEventPayload.from(updatedProject, command.userId())
        );

        return UpdateProjectResult.from(updatedProject);
    }

    private boolean leadChanged(Long previousLeadUserId, Long updatedLeadUserId) {
        return !Objects.equals(previousLeadUserId, updatedLeadUserId) && updatedLeadUserId != null;
    }

    private void ensureProjectWritable(ProjectEntity project) {
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
    }

    private void assignLeadToAdministratorsRole(ProjectEntity project, Long userId) {
        Long administratorsRoleId = projectRoleService
                .getProjectRoleByNameIncludingSystem(
                        PermissionSeedConstants.PROJECT_ROLE_ADMINISTRATORS,
                        project.getTenantId()
                )
                .map(ProjectRoleEntity::getId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ROLE_NOT_FOUND,
                        "Default project role not found: name=" + PermissionSeedConstants.PROJECT_ROLE_ADMINISTRATORS
                ));

        projectRoleActorService.assignActorIfAbsent(
                project.getTenantId(),
                project.getId(),
                administratorsRoleId,
                ProjectRoleActorSubjectType.USER.name(),
                String.valueOf(project.getLeadUserId()),
                userId
        );
    }

    private void ensureLeadAdminAccess(ProjectEntity project) {
        if (project.getLeadUserId() == null) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PROJECT_PERMISSION_DENIED,
                    "Project lead is required to bootstrap admin access for projectId=" + project.getId()
            );
        }

        ProjectPermissionEvaluationContext leadContext = ProjectPermissionEvaluationContext.builder()
                .userId(project.getLeadUserId())
                .build();

        boolean hasAdminPermission = projectPermissionEvaluationService.hasPermission(
                ProjectPermissionSubject.from(project),
                leadContext,
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );

        if (!hasAdminPermission) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PROJECT_PERMISSION_DENIED,
                    "Project lead does not have ADMINISTER_PROJECTS after update: projectId="
                            + project.getId() + ", leadUserId=" + project.getLeadUserId()
            );
        }
    }
}
