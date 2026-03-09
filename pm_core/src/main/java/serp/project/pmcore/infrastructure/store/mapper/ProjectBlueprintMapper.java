/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.ProjectBlueprintEntity;
import serp.project.pmcore.infrastructure.store.model.ProjectBlueprintModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectBlueprintMapper extends BaseMapper {

    public ProjectBlueprintEntity toEntity(ProjectBlueprintModel model) {
        if (model == null) {
            return null;
        }
        return ProjectBlueprintEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .typeKey(model.getTypeKey())
                .avatarUrl(model.getAvatarUrl())
                .isSystem(model.getIsSystem())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public ProjectBlueprintModel toModel(ProjectBlueprintEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProjectBlueprintModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .typeKey(entity.getTypeKey())
                .avatarUrl(entity.getAvatarUrl())
                .isSystem(entity.getIsSystem())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<ProjectBlueprintEntity> toEntities(List<ProjectBlueprintModel> models) {
        if (models == null) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
