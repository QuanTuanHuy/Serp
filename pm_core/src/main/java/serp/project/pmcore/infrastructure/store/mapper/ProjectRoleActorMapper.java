/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;
import serp.project.pmcore.infrastructure.store.model.ProjectRoleActorModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectRoleActorMapper extends BaseMapper {

    public ProjectRoleActorEntity toEntity(ProjectRoleActorModel model) {
        if (model == null) {
            return null;
        }
        return ProjectRoleActorEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .projectId(model.getProjectId())
                .projectRoleId(model.getProjectRoleId())
                .subjectType(model.getSubjectType())
                .subjectId(model.getSubjectId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public ProjectRoleActorModel toModel(ProjectRoleActorEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProjectRoleActorModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .projectId(entity.getProjectId())
                .projectRoleId(entity.getProjectRoleId())
                .subjectType(entity.getSubjectType())
                .subjectId(entity.getSubjectId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<ProjectRoleActorEntity> toEntities(List<ProjectRoleActorModel> models) {
        if (models == null) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
