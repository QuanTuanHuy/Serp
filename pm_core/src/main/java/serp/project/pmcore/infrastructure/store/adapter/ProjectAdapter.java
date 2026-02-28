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
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.ProjectEntity;
import serp.project.pmcore.core.port.store.IProjectPort;
import serp.project.pmcore.infrastructure.store.mapper.ProjectMapper;
import serp.project.pmcore.infrastructure.store.model.ProjectModel;
import serp.project.pmcore.infrastructure.store.repository.IProjectRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectAdapter implements IProjectPort {

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
    public Pair<List<ProjectEntity>, Long> getProjects(Long tenantId, String search,
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
        return Pair.of(entities, result.getTotalElements());
    }

    @Override
    public void deleteProjectById(Long id, Long tenantId) {
        projectRepository.softDeleteByIdAndTenantId(id, tenantId);
    }
}
