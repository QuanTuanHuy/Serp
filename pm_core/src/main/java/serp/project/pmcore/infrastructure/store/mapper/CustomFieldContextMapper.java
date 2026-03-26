/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.CustomFieldContextEntity;
import serp.project.pmcore.infrastructure.store.model.CustomFieldContextModel;

import java.util.Collections;
import java.util.List;

@Component
public class CustomFieldContextMapper extends BaseMapper {

    public CustomFieldContextEntity toEntity(CustomFieldContextModel model) {
        if (model == null) {
            return null;
        }
        return CustomFieldContextEntity.builder()
                .id(model.getId())
                .customFieldId(model.getCustomFieldId())
                .name(model.getName())
                .description(model.getDescription())
                .issueTypeKey(model.getIssueTypeKey())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public List<CustomFieldContextEntity> toEntities(List<CustomFieldContextModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }
}
