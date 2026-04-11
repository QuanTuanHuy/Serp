/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.roleactor.remove;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.project.command.roleactor.RoleActorEventPayload;
import serp.project.pmcore.application.project.command.roleactor.RoleActorOutboxPublisher;
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

@Service
@RequiredArgsConstructor
public class RemoveProjectRoleActorCommandHandler
        implements ICommandHandler<RemoveProjectRoleActorCommand, Unit> {

    private final IProjectService projectService;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final RemoveProjectRoleActorValidator removeProjectRoleActorValidator;
    private final RoleActorOutboxPublisher roleActorOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Unit handle(RemoveProjectRoleActorCommand command) {
        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        ensureProjectWritable(project);

        projectRoleService.getProjectRoleByIdIncludingSystem(command.roleId(), command.tenantId());

        var evaluationContext = ProjectPermissionEvaluationContext.builder()
                .userId(command.userId())
                .groupKeys(command.groupKeys())
                .build();
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                evaluationContext,
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );

        RemoveProjectRoleActorValidator.ValidatedRoleActorSubject validatedSubject =
                removeProjectRoleActorValidator.validate(command);

        projectRoleActorService.removeActor(
                command.tenantId(),
                command.projectId(),
                command.roleId(),
                validatedSubject.subjectType(),
                validatedSubject.subjectId(),
                command.userId()
        );

        roleActorOutboxPublisher.publishRoleActorRemoved(
                command.tenantId(),
                RoleActorEventPayload.builder()
                        .projectId(command.projectId())
                        .roleId(command.roleId())
                        .subjectType(validatedSubject.subjectType())
                        .subjectId(validatedSubject.subjectId())
                        .actorId(null)
                        .performedBy(command.userId())
                        .occurredAt(System.currentTimeMillis())
                        .build()
        );

        return Unit.VALUE;
    }

    private void ensureProjectWritable(ProjectEntity project) {
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
    }
}
