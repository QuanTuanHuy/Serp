/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.port.write.IProjectWritePort;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.infrastructure.store.mapper.ProjectMapper;
import serp.project.pmcore.infrastructure.store.model.ProjectModel;
import serp.project.pmcore.infrastructure.store.repository.IProjectRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectAdapter implements IProjectReadPort, IProjectWritePort {

    private final IProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Override
    public ProjectEntity saveProject(ProjectEntity project) {
        return projectMapper.toEntity(
            projectRepository.save(projectMapper.toModel(project))
        );
    }

    @Override
    public Optional<ProjectEntity> getProjectById(Long id, Long tenantId) {
        return projectRepository.findByIdAndTenantId(id, tenantId)
                .map(projectMapper::toEntity);
    }

    @Override
    public Optional<ProjectEntity> getProjectByKey(String key, Long tenantId) {
        return projectRepository.findByKeyAndTenantId(key, tenantId)
                .map(projectMapper::toEntity);
    }

    @Override
    public boolean existsByKeyAndTenantId(String key, Long tenantId) {
        return projectRepository.existsByKeyAndTenantId(key, tenantId);
    }

    @Override
    public boolean existsActiveProjectByCategoryId(Long categoryId, Long tenantId) {
        return projectRepository.existsActiveProjectByCategoryId(categoryId, tenantId);
    }

    @Override
    public boolean existsActiveProjectByIssueTypeSchemeId(Long issueTypeSchemeId, Long tenantId) {
        return projectRepository.existsActiveProjectByIssueTypeSchemeId(issueTypeSchemeId, tenantId);
    }

    @Override
    public List<Long> getActiveProjectIdsByIssueTypeSchemeId(Long issueTypeSchemeId, Long tenantId) {
        return projectRepository.findActiveProjectIdsByIssueTypeSchemeId(issueTypeSchemeId, tenantId);
    }

    @Override
    public boolean existsActiveProjectByPrioritySchemeId(Long prioritySchemeId, Long tenantId) {
        return projectRepository.existsActiveProjectByPrioritySchemeId(prioritySchemeId, tenantId);
    }

    @Override
    public List<Long> getActiveProjectIdsByPrioritySchemeId(Long prioritySchemeId, Long tenantId) {
        return projectRepository.findActiveProjectIdsByPrioritySchemeId(prioritySchemeId, tenantId);
    }

    @Override
    public PageResult<ProjectEntity> getProjects(Long tenantId, String search,
                                                 Long categoryId, String projectTypeKey,
                                                 Boolean archived, int page, int size,
                                                 String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<ProjectModel> result = projectRepository.findAllWithFilters(
                tenantId, search, categoryId, projectTypeKey, archived, pageable);

        List<ProjectEntity> entities = projectMapper.toEntities(result.getContent());
        return new PageResult<>(entities, result.getTotalElements());
    }

    @Override
    public void deleteProjectById(Long id, Long tenantId) {
        projectRepository.softDeleteByIdAndTenantId(id, tenantId);
    }
}
