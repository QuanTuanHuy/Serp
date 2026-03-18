package serp.project.pmcore.application.command.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.application.command.project.validator.CreateProjectValidator;
import serp.project.pmcore.domain.dto.request.CreateProjectRequest;
import serp.project.pmcore.domain.dto.response.ProjectResponse;
import serp.project.pmcore.domain.service.IProjectService;
import serp.project.pmcore.domain.service.ISchemeProvisioningService;
import serp.project.pmcore.domain.validator.WorkflowSchemeCompatibilityValidator;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateProjectCommand {
    private final CreateProjectValidator projectValidator;
    private final WorkflowSchemeCompatibilityValidator workflowSchemeCompatibilityValidator;

    private final IProjectService projectService;
    private final ISchemeProvisioningService schemeProvisioningService;

    public ProjectResponse execute(CreateProjectRequest request, Long userId, Long tenantId) {
        projectValidator.validate(request, tenantId);
        return null;
    }
}
