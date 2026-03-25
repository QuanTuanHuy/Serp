/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.project.ProjectRoleActorEntity;
import serp.project.pmcore.domain.port.store.IProjectRoleActorPort;
import serp.project.pmcore.infrastructure.store.mapper.ProjectRoleActorMapper;
import serp.project.pmcore.infrastructure.store.repository.IProjectRoleActorRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectRoleActorAdapter implements IProjectRoleActorPort {

    private final IProjectRoleActorRepository projectRoleActorRepository;
    private final ProjectRoleActorMapper projectRoleActorMapper;

    @Override
    public ProjectRoleActorEntity saveProjectRoleActor(ProjectRoleActorEntity actor) {
        return projectRoleActorMapper.toEntity(projectRoleActorRepository.save(projectRoleActorMapper.toModel(actor)));
    }

    @Override
    public Optional<ProjectRoleActorEntity> findActiveAssignment(Long tenantId,
                                                                 Long projectId,
                                                                 Long projectRoleId,
                                                                 String subjectType,
                                                                 String subjectId) {
        return projectRoleActorRepository
                .findByTenantIdAndProjectIdAndProjectRoleIdAndSubjectTypeAndSubjectId(
                        tenantId,
                        projectId,
                        projectRoleId,
                        subjectType,
                        subjectId
                )
                .map(projectRoleActorMapper::toEntity);
    }

    @Override
    public boolean existsActiveAssignment(Long tenantId,
                                          Long projectId,
                                          Long projectRoleId,
                                          String subjectType,
                                          String subjectId) {
        return projectRoleActorRepository.existsByTenantIdAndProjectIdAndProjectRoleIdAndSubjectTypeAndSubjectId(
                tenantId,
                projectId,
                projectRoleId,
                subjectType,
                subjectId
        );
    }

    @Override
    public int softDeleteActiveAssignment(Long tenantId,
                                          Long projectId,
                                          Long projectRoleId,
                                          String subjectType,
                                          String subjectId,
                                          Long updatedBy) {
        return projectRoleActorRepository.softDeleteActiveAssignment(
                tenantId,
                projectId,
                projectRoleId,
                subjectType,
                subjectId,
                updatedBy
        );
    }

    @Override
    public List<ProjectRoleActorEntity> getProjectRoleActorsByProjectIdAndRoleId(Long projectId,
                                                                                  Long projectRoleId,
                                                                                  Long tenantId) {
        return projectRoleActorMapper.toEntities(
                projectRoleActorRepository.findAllByProjectIdAndProjectRoleIdAndTenantId(projectId, projectRoleId, tenantId)
        );
    }
}
