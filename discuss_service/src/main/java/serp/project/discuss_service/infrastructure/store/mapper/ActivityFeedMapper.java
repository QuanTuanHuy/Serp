/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Activity feed entity/model mapper
 */

package serp.project.discuss_service.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.ActivityFeedEntity;
import serp.project.discuss_service.infrastructure.store.model.ActivityFeedModel;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ActivityFeedMapper extends BaseMapper {

    public ActivityFeedEntity toEntity(ActivityFeedModel model) {
        if (model == null) {
            return null;
        }

        return ActivityFeedEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .userId(model.getUserId())
                .actionType(model.getActionType())
                .actorId(model.getActorId())
                .entityType(model.getEntityType())
                .entityId(model.getEntityId())
                .channelId(model.getChannelId())
                .messageId(model.getMessageId())
                .title(model.getTitle())
                .description(model.getDescription())
                .isRead(model.getIsRead())
                .readAt(model.getReadAt())
                .occurredAt(model.getOccurredAt())
                .metadata(model.getMetadata())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .build();
    }

    public ActivityFeedModel toModel(ActivityFeedEntity entity) {
        if (entity == null) {
            return null;
        }

        return ActivityFeedModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .userId(entity.getUserId())
                .actionType(entity.getActionType())
                .actorId(entity.getActorId())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .channelId(entity.getChannelId())
                .messageId(entity.getMessageId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .isRead(entity.getIsRead())
                .readAt(entity.getReadAt())
                .occurredAt(entity.getOccurredAt())
                .metadata(entity.getMetadata())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<ActivityFeedEntity> toEntityList(List<ActivityFeedModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<ActivityFeedModel> toModelList(List<ActivityFeedEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
