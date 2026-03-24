/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.NotificationSchemeEntryEntity;
import serp.project.pmcore.infrastructure.store.model.NotificationSchemeEntryModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationSchemeEntryMapper extends BaseMapper {

    public NotificationSchemeEntryEntity toEntity(NotificationSchemeEntryModel model) {
        if (model == null) { return null; }
        return NotificationSchemeEntryEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .schemeId(model.getSchemeId())
                .eventId(model.getEventId())
                .recipientType(model.getRecipientType())
                .recipientRef(model.getRecipientRef())
                .customFieldId(model.getCustomFieldId())
                .channel(model.getChannel())
                .templateId(model.getTemplateId())
                .isEnabled(model.getIsEnabled())
                .conditionsJson(model.getConditionsJson())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public NotificationSchemeEntryModel toModel(NotificationSchemeEntryEntity entity) {
        if (entity == null) { return null; }
        return NotificationSchemeEntryModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .schemeId(entity.getSchemeId())
                .eventId(entity.getEventId())
                .recipientType(entity.getRecipientType())
                .recipientRef(entity.getRecipientRef())
                .customFieldId(entity.getCustomFieldId())
                .channel(entity.getChannel())
                .templateId(entity.getTemplateId())
                .isEnabled(entity.getIsEnabled())
                .conditionsJson(entity.getConditionsJson())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<NotificationSchemeEntryEntity> toEntities(List<NotificationSchemeEntryModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<NotificationSchemeEntryModel> toModels(List<NotificationSchemeEntryEntity> entities) {
        if (entities == null) { return Collections.emptyList(); }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
