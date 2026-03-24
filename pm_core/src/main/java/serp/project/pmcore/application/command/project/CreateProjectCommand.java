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
import serp.project.pmcore.domain.dto.project.ProjectProvisioningRequest;
import serp.project.pmcore.domain.dto.project.ProjectProvisioningResult;
import serp.project.pmcore.domain.dto.request.project.CreateProjectRequest;
import serp.project.pmcore.domain.dto.response.project.ProjectResponse;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.project.ProjectSchemeBindings;
import serp.project.pmcore.domain.enums.ProvisioningMode;
import serp.project.pmcore.domain.service.IProjectService;
import serp.project.pmcore.domain.service.ISchemeProvisioningService;
import serp.project.pmcore.domain.validator.ProjectSchemeCompatibilityValidator;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateProjectCommand {
    private final CreateProjectValidator projectValidator;
    private final IProjectService projectService;
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
}
