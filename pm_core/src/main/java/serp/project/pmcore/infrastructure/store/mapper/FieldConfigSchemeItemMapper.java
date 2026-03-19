/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.FieldConfigSchemeItemEntity;
import serp.project.pmcore.infrastructure.store.model.FieldConfigSchemeItemModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FieldConfigSchemeItemMapper extends BaseMapper {

    public FieldConfigSchemeItemEntity toEntity(FieldConfigSchemeItemModel model) {
        if (model == null) { return null; }
        return FieldConfigSchemeItemEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .schemeId(model.getSchemeId())
                .issueTypeId(model.getIssueTypeId())
                .fieldConfigId(model.getFieldConfigId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public FieldConfigSchemeItemModel toModel(FieldConfigSchemeItemEntity entity) {
        if (entity == null) { return null; }
        return FieldConfigSchemeItemModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .schemeId(entity.getSchemeId())
                .issueTypeId(entity.getIssueTypeId())
                .fieldConfigId(entity.getFieldConfigId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<FieldConfigSchemeItemEntity> toEntities(List<FieldConfigSchemeItemModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<FieldConfigSchemeItemModel> toModels(List<FieldConfigSchemeItemEntity> entities) {
        if (entities == null) { return Collections.emptyList(); }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
