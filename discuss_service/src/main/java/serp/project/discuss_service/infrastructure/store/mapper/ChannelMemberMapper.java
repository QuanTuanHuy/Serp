/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel member entity/model mapper
 */

package serp.project.discuss_service.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.infrastructure.store.model.ChannelMemberModel;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChannelMemberMapper extends BaseMapper {

    public ChannelMemberEntity toEntity(ChannelMemberModel model) {
        if (model == null) {
            return null;
        }

        return ChannelMemberEntity.builder()
                .id(model.getId())
                .channelId(model.getChannelId())
                .userId(model.getUserId())
                .tenantId(model.getTenantId())
                .role(model.getRole())
                .status(model.getStatus())
                .joinedAt(localDateTimeToLong(model.getJoinedAt()))
                .leftAt(localDateTimeToLong(model.getLeftAt()))
                .removedBy(model.getRemovedBy())
                .lastReadMsgId(model.getLastReadMsgId())
                .unreadCount(model.getUnreadCount())
                .isMuted(model.getIsMuted())
                .isPinned(model.getIsPinned())
                .notificationLevel(model.getNotificationLevel())
                .metadata(model.getMetadata())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public ChannelMemberModel toModel(ChannelMemberEntity entity) {
        if (entity == null) {
            return null;
        }

        return ChannelMemberModel.builder()
                .id(entity.getId())
                .channelId(entity.getChannelId())
                .userId(entity.getUserId())
                .tenantId(entity.getTenantId())
                .role(entity.getRole())
                .status(entity.getStatus())
                .joinedAt(longToLocalDateTime(entity.getJoinedAt()))
                .leftAt(longToLocalDateTime(entity.getLeftAt()))
                .removedBy(entity.getRemovedBy())
                .lastReadMsgId(entity.getLastReadMsgId())
                .unreadCount(entity.getUnreadCount())
                .isMuted(entity.getIsMuted())
                .isPinned(entity.getIsPinned())
                .notificationLevel(entity.getNotificationLevel())
                .metadata(entity.getMetadata())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<ChannelMemberEntity> toEntityList(List<ChannelMemberModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<ChannelMemberModel> toModelList(List<ChannelMemberEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
