/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.ProjectCategoryEntity;
import serp.project.pmcore.infrastructure.store.model.ProjectCategoryModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectCategoryMapper extends BaseMapper {

    public ProjectCategoryEntity toEntity(ProjectCategoryModel model) {
        if (model == null) {
            return null;
        }
        return ProjectCategoryEntity.builder()
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

    public ProjectCategoryModel toModel(ProjectCategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProjectCategoryModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isSystem(entity.getIsSystem())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<ProjectCategoryEntity> toEntities(List<ProjectCategoryModel> models) {
        if (models == null) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
