/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.project.ProjectRoleEntity;
import serp.project.pmcore.infrastructure.store.model.ProjectRoleModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectRoleMapper extends BaseMapper {

    public ProjectRoleEntity toEntity(ProjectRoleModel model) {
        if (model == null) {
            return null;
        }
        return ProjectRoleEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .isSystem(model.getIsSystem())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public ProjectRoleModel toModel(ProjectRoleEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProjectRoleModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isSystem(Boolean.TRUE.equals(entity.getIsSystem()))
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<ProjectRoleEntity> toEntities(List<ProjectRoleModel> models) {
        if (models == null) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
