/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.infrastructure.store.model.ProjectComponentModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectComponentMapper extends BaseMapper {

    public ProjectComponentEntity toEntity(ProjectComponentModel model) {
        if (model == null) {
            return null;
        }
        return ProjectComponentEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .projectId(model.getProjectId())
                .name(model.getName())
                .description(model.getDescription())
                .leadUserId(model.getLeadUserId())
                .assigneeType(model.getAssigneeType())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .deletedAt(localDateTimeToLong(model.getDeletedAt()))
                .build();
    }

    public ProjectComponentModel toModel(ProjectComponentEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProjectComponentModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .projectId(entity.getProjectId())
                .name(entity.getName())
                .description(entity.getDescription())
                .leadUserId(entity.getLeadUserId())
                .assigneeType(entity.getAssigneeType())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .deletedAt(longToLocalDateTime(entity.getDeletedAt()))
                .build();
    }

    public List<ProjectComponentEntity> toEntities(List<ProjectComponentModel> models) {
        if (models == null) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
