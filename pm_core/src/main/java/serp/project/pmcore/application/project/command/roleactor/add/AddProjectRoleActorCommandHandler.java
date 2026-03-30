/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.roleactor.add;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.project.command.roleactor.RoleActorEventPayload;
import serp.project.pmcore.application.project.command.roleactor.RoleActorOutboxPublisher;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

@Service
@RequiredArgsConstructor
public class AddProjectRoleActorCommandHandler
        implements ICommandHandler<AddProjectRoleActorCommand, AddProjectRoleActorResult> {

    private final IProjectService projectService;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    private final AddProjectRoleActorValidator addProjectRoleActorValidator;
    private final RoleActorOutboxPublisher roleActorOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddProjectRoleActorResult handle(AddProjectRoleActorCommand command) {
        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        ensureProjectWritable(project);

        projectRoleService.getProjectRoleByIdIncludingSystem(command.roleId(), command.tenantId());

        var evaluationContext = ProjectPermissionEvaluationContext.builder()
                .userId(command.userId())
                .groupKeys(command.groupKeys())
                .build();
        projectPermissionEvaluationService.checkPermission(
                project,
                evaluationContext,
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );

        AddProjectRoleActorValidator.ValidatedRoleActorSubject validatedSubject =
                addProjectRoleActorValidator.validate(command);

        ProjectRoleActorEntity actor = projectRoleActorService.assignActor(
                command.tenantId(),
                command.projectId(),
                command.roleId(),
                validatedSubject.subjectType(),
                validatedSubject.subjectId(),
                command.userId()
        );

        roleActorOutboxPublisher.publishRoleActorAdded(
                command.tenantId(),
                RoleActorEventPayload.builder()
                        .projectId(command.projectId())
                        .roleId(command.roleId())
                        .subjectType(actor.getSubjectType())
                        .subjectId(actor.getSubjectId())
                        .actorId(actor.getId())
                        .performedBy(command.userId())
                        .occurredAt(System.currentTimeMillis())
                        .build()
        );

        return AddProjectRoleActorResult.from(actor);
    }

    private void ensureProjectWritable(ProjectEntity project) {
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
    }
}
