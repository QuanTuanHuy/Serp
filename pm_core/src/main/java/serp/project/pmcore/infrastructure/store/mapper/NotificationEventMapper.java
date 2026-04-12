/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.notification.entity.NotificationEventEntity;
import serp.project.pmcore.infrastructure.store.model.NotificationEventModel;

@Component
public class NotificationEventMapper extends BaseMapper {

    public NotificationEventEntity toEntity(NotificationEventModel model) {
        if (model == null) { return null; }
        return NotificationEventEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .eventKey(model.getEventKey())
                .name(model.getName())
                .description(model.getDescription())
                .isSystem(model.getIsSystem())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }
}
