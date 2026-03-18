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
import serp.project.pmcore.domain.dto.request.project.CreateProjectRequest;
import serp.project.pmcore.domain.dto.response.project.ProjectResponse;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.project.ProjectSchemeBindings;
import serp.project.pmcore.domain.service.IProjectService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateProjectCommand {
    private final CreateProjectValidator projectValidator;
    private final IProjectService projectService;

    @Transactional(rollbackFor = Exception.class)
    public ProjectResponse execute(CreateProjectRequest request, Long tenantId, Long userId) {
        projectValidator.validate(request, tenantId);

        ProjectSchemeBindings schemeBindings = ProjectSchemeBindings.fromRequest(request);
        log.info("Creating project key={} tenantId={} schemeBindings={}",
                request.getKey(), tenantId, schemeBindings.toSchemeMap());

        ProjectEntity project = buildProjectEntity(request, schemeBindings);
        ProjectEntity saved = projectService.createProject(project, tenantId, userId);

        log.info("Created project id={} key={} tenantId={}",
                saved.getId(), saved.getKey(), tenantId);
        return ProjectResponse.from(saved);
    }

    private ProjectEntity buildProjectEntity(CreateProjectRequest request,
                                             ProjectSchemeBindings schemeBindings) {
        ProjectEntity project = ProjectEntity.builder()
                .key(request.getKey())
                .name(request.getName())
                .description(request.getDescription())
                .url(request.getUrl())
                .leadUserId(request.getLeadUserId())
                .avatarId(request.getAvatarId())
                .categoryId(request.getCategoryId())
                .projectTypeKey(request.getProjectTypeKey())
                .build();

        schemeBindings.applyTo(project);
        return project;
    }
}
