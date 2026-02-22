/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.IssueTypeEntity;
import serp.project.pmcore.infrastructure.store.model.IssueTypeModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IssueTypeMapper extends BaseMapper {

    public IssueTypeEntity toEntity(IssueTypeModel model) {
        if (model == null) { return null; }
        return IssueTypeEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .typeKey(model.getTypeKey())
                .name(model.getName())
                .description(model.getDescription())
                .iconUrl(model.getIconUrl())
                .hierarchyLevel(model.getHierarchyLevel())
                .isSystem(Boolean.TRUE.equals(model.getIsSystem()))
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public IssueTypeModel toModel(IssueTypeEntity entity) {
        if (entity == null) { return null; }
        return IssueTypeModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .typeKey(entity.getTypeKey())
                .name(entity.getName())
                .description(entity.getDescription())
                .iconUrl(entity.getIconUrl())
                .hierarchyLevel(entity.getHierarchyLevel())
                .isSystem(entity.isSystem())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<IssueTypeEntity> toEntities(List<IssueTypeModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
