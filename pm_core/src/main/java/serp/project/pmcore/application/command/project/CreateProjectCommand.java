/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.command.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.command.project.validator.CreateProjectValidator;
import serp.project.pmcore.domain.constant.PermissionSeedConstants;
import serp.project.pmcore.domain.dto.project.ProjectProvisioningRequest;
import serp.project.pmcore.domain.dto.project.ProjectProvisioningResult;
import serp.project.pmcore.domain.dto.request.project.CreateProjectRequest;
import serp.project.pmcore.domain.dto.response.project.ProjectResponse;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.project.ProjectRoleEntity;
import serp.project.pmcore.domain.entity.project.ProjectSchemeBindings;
import serp.project.pmcore.domain.enums.ProvisioningMode;
import serp.project.pmcore.domain.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.service.IProjectService;
import serp.project.pmcore.domain.service.IProjectRoleActorService;
import serp.project.pmcore.domain.service.IProjectRoleService;
import serp.project.pmcore.domain.service.ISchemeProvisioningService;
import serp.project.pmcore.domain.validator.ProjectSchemeCompatibilityValidator;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateProjectCommand {
    private final CreateProjectValidator projectValidator;
    private final IProjectService projectService;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;
    private final ISchemeProvisioningService schemeProvisioningService;
    private final ProjectSchemeCompatibilityValidator projectSchemeCompatibilityValidator;

    @Transactional(rollbackFor = Exception.class)
    public ProjectResponse execute(CreateProjectRequest request, Long tenantId, Long userId) {
        projectValidator.validate(request, tenantId);

        ProjectSchemeBindings schemeBindings = ProjectSchemeBindings.fromRequest(request);
        log.info("Creating project key={} tenantId={} schemeBindings={}",
                request.getKey(), tenantId, schemeBindings.toSchemeMap());

        ProjectEntity shellProject = buildProjectEntity(request);
        ProjectEntity savedProject = projectService.createProject(shellProject, tenantId, userId);

        ProjectProvisioningResult provisioningResult = schemeProvisioningService.provisionProjectSchemes(
                savedProject,
                buildProvisioningRequest(request, savedProject, tenantId, userId, schemeBindings)
        );
        provisioningResult.applyEffectiveBindings(savedProject);

        projectSchemeCompatibilityValidator.validate(savedProject, tenantId);

        ProjectEntity finalProject = projectService.saveProject(savedProject, userId);

        assignLeadToAdministratorsRole(finalProject, userId);

        log.info("Created project id={} key={} tenantId={}",
                finalProject.getId(), finalProject.getKey(), tenantId);
        return ProjectResponse.from(finalProject);
    }

    private ProjectEntity buildProjectEntity(CreateProjectRequest request) {
        return ProjectEntity.builder()
                .key(request.getKey())
                .name(request.getName())
                .description(request.getDescription())
                .url(request.getUrl())
                .leadUserId(request.getLeadUserId())
                .avatarId(request.getAvatarId())
                .categoryId(request.getCategoryId())
                .projectTypeKey(request.getProjectTypeKey())
                .build();
    }

    private ProjectProvisioningRequest buildProvisioningRequest(CreateProjectRequest request,
                                                               ProjectEntity project,
                                                               Long tenantId,
                                                               Long userId,
                                                               ProjectSchemeBindings schemeBindings) {
        return ProjectProvisioningRequest.builder()
                .tenantId(tenantId)
                .userId(userId)
                .projectId(project.getId())
                .projectKey(project.getKey())
                .blueprintId(request.getBlueprintId())
                .provisioningMode(request.getProvisioningMode() == null
                        ? ProvisioningMode.TEMPLATE_DEFAULT
                        : request.getProvisioningMode())
                .requestedSchemeBindings(schemeBindings)
                .build();
    }

    private void assignLeadToAdministratorsRole(ProjectEntity project, Long userId) {
        Long administratorsRoleId = projectRoleService
                .getProjectRoleByNameIncludingSystem(PermissionSeedConstants.PROJECT_ROLE_ADMINISTRATORS, project.getTenantId())
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
}
