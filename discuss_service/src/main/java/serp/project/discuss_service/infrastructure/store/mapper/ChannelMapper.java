/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel entity/model mapper
 */

package serp.project.discuss_service.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.infrastructure.store.model.ChannelModel;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChannelMapper extends BaseMapper {

    public ChannelEntity toEntity(ChannelModel model) {
        if (model == null) {
            return null;
        }

        return ChannelEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .createdBy(model.getCreatedBy())
                .name(model.getName())
                .description(model.getDescription())
                .type(model.getType())
                .entityType(model.getEntityType())
                .entityId(model.getEntityId())
                .isPrivate(model.getIsPrivate())
                .isArchived(model.getIsArchived())
                .memberCount(model.getMemberCount())
                .messageCount(model.getMessageCount())
                .lastMessageAt(localDateTimeToLong(model.getLastMessageAt()))
                .metadata(model.getMetadata())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public ChannelModel toModel(ChannelEntity entity) {
        if (entity == null) {
            return null;
        }

        return ChannelModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .createdBy(entity.getCreatedBy())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .isPrivate(entity.getIsPrivate())
                .isArchived(entity.getIsArchived())
                .memberCount(entity.getMemberCount())
                .messageCount(entity.getMessageCount())
                .lastMessageAt(longToLocalDateTime(entity.getLastMessageAt()))
                .metadata(entity.getMetadata())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<ChannelEntity> toEntityList(List<ChannelModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<ChannelModel> toModelList(List<ChannelEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
