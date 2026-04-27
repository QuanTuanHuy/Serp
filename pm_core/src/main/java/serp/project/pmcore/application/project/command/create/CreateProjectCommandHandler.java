/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.create;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.dto.ProjectProvisioningRequest;
import serp.project.pmcore.domain.project.dto.ProjectProvisioningResult;
import serp.project.pmcore.domain.project.dto.ProjectSchemeBindings;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.validator.ProjectSchemeCompatibilityValidator;
import serp.project.pmcore.domain.project.provisioning.ISchemeProvisioningService;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.PermissionSeedConstants;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.shared.enums.ProvisioningMode;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateProjectCommandHandler implements ICommandHandler<CreateProjectCommand, CreateProjectResult> {
    private final CreateProjectValidator projectValidator;
    private final IProjectService projectService;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final ISchemeProvisioningService schemeProvisioningService;
    private final ProjectSchemeCompatibilityValidator projectSchemeCompatibilityValidator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateProjectResult handle(CreateProjectCommand command) {
        projectValidator.validate(command);

        ProjectSchemeBindings schemeBindings = command.toSchemeBindings();
        log.info("Creating project key={} tenantId={} schemeBindings={}",
                command.key(), command.tenantId(), schemeBindings.toSchemeMap());

        ProjectEntity shellProject = buildProjectEntity(command);
        ProjectEntity savedProject = projectService.createProject(shellProject, command.tenantId(), command.userId());

        ProjectProvisioningResult provisioningResult = schemeProvisioningService.provisionProjectSchemes(
                savedProject,
                buildProvisioningRequest(command, savedProject, schemeBindings)
        );
        provisioningResult.applyEffectiveBindings(savedProject);

        projectSchemeCompatibilityValidator.validate(savedProject, command.tenantId());

        ProjectEntity finalProject = projectService.saveProject(savedProject, command.userId());

        assignLeadToAdministratorsRole(finalProject, command.userId());
        ensureLeadAdminAccess(finalProject);

        log.info("Created project id={} key={} tenantId={}",
                finalProject.getId(), finalProject.getKey(), command.tenantId());
        return CreateProjectResult.from(finalProject);
    }

    private ProjectEntity buildProjectEntity(CreateProjectCommand command) {
        return ProjectEntity.builder()
                .key(command.key())
                .name(command.name())
                .description(command.description())
                .url(command.url())
                .leadUserId(command.leadUserId())
                .avatarId(command.avatarId())
                .categoryId(command.categoryId())
                .projectTypeKey(command.projectTypeKey())
                .build();
    }

    private ProjectProvisioningRequest buildProvisioningRequest(CreateProjectCommand command,
                                                               ProjectEntity project,
                                                               ProjectSchemeBindings schemeBindings) {
        return ProjectProvisioningRequest.builder()
                .tenantId(command.tenantId())
                .userId(command.userId())
                .projectId(project.getId())
                .projectKey(project.getKey())
                .blueprintId(command.blueprintId())
                .provisioningMode(command.provisioningMode() == null
                        ? ProvisioningMode.TEMPLATE_DEFAULT
                        : command.provisioningMode())
                .requestedSchemeBindings(schemeBindings)
                .build();
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
                    "Project lead does not have ADMINISTER_PROJECTS after bootstrap: projectId="
                            + project.getId() + ", leadUserId=" + project.getLeadUserId()
            );
        }
    }
}
