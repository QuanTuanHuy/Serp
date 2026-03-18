package serp.project.pmcore.application.query.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.domain.dto.request.project.ProjectExpandOptions;
import serp.project.pmcore.domain.dto.response.project.CategorySummary;
import serp.project.pmcore.domain.dto.response.project.ProjectDetailResponse;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.IProjectCategoryPort;
import serp.project.pmcore.domain.port.store.IProjectPort;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetProjectQuery {
    private final IProjectPort projectPort;
    private final IProjectCategoryPort projectCategoryPort;

    @Transactional(readOnly = true)
    public ProjectDetailResponse executeById(Long projectId, Long tenantId) {
        return executeById(projectId, tenantId, Set.of());
    }

    @Transactional(readOnly = true)
    public ProjectDetailResponse executeById(Long projectId,
                                              Long tenantId,
                                              Set<ProjectExpandOptions> expand) {
        ProjectEntity project = projectPort.getProjectById(projectId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.project(projectId));
        return enrich(project, tenantId, expand);
    }

    @Transactional(readOnly = true)
    public ProjectDetailResponse executeByKey(String key, Long tenantId) {
        return executeByKey(key, tenantId, Set.of());
    }

    @Transactional(readOnly = true)
    public ProjectDetailResponse executeByKey(String key,
                                              Long tenantId,
                                              Set<ProjectExpandOptions> expand) {
        ProjectEntity project = projectPort.getProjectByKey(key, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.projectByKey(key));
        return enrich(project, tenantId, expand);
    }

    private ProjectDetailResponse enrich(ProjectEntity project,
                                         Long tenantId,
                                         Set<ProjectExpandOptions> expand) {
        CategorySummary category = expand.contains(ProjectExpandOptions.CATEGORY)
                ? resolveCategory(project.getCategoryId(), tenantId)
                : null;
        return ProjectDetailResponse.from(project, category);
    }

    private CategorySummary resolveCategory(Long categoryId, Long tenantId) {
        if (categoryId == null) {
            return null;
        }
        return projectCategoryPort.getCategoryById(categoryId, tenantId)
                .map(c -> new CategorySummary(c.getId(), c.getName()))
                .orElse(null);
    }
}
