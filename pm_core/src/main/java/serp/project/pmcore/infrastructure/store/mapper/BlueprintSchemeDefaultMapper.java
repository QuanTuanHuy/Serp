/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.BlueprintSchemeDefaultEntity;
import serp.project.pmcore.infrastructure.store.model.BlueprintSchemeDefaultModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BlueprintSchemeDefaultMapper extends BaseMapper {

    public BlueprintSchemeDefaultEntity toEntity(BlueprintSchemeDefaultModel model) {
        if (model == null) {
            return null;
        }
        return BlueprintSchemeDefaultEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .blueprintId(model.getBlueprintId())
                .schemeType(model.getSchemeType())
                .schemeId(model.getSchemeId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public BlueprintSchemeDefaultModel toModel(BlueprintSchemeDefaultEntity entity) {
        if (entity == null) {
            return null;
        }
        return BlueprintSchemeDefaultModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .blueprintId(entity.getBlueprintId())
                .schemeType(entity.getSchemeType())
                .schemeId(entity.getSchemeId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<BlueprintSchemeDefaultEntity> toEntities(List<BlueprintSchemeDefaultModel> models) {
        if (models == null) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
