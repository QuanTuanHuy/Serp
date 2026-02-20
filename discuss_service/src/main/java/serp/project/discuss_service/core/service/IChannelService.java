/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel service interface
 */

package serp.project.discuss_service.core.service;

import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.enums.ChannelType;

import java.util.List;
import java.util.Optional;

public interface IChannelService {

    ChannelEntity createChannel(ChannelEntity channel);

    ChannelEntity getOrCreateDirectChannel(Long tenantId, Long userId1, Long userId2);

    ChannelEntity createGroupChannel(Long tenantId, Long createdBy, String name, 
                                     String description, boolean isPrivate);

    ChannelEntity createTopicChannel(Long tenantId, Long createdBy, String name,
                                     String entityType, Long entityId);

    Optional<ChannelEntity> getChannelById(Long id);

    ChannelEntity getChannelByIdOrThrow(Long id);

    List<ChannelEntity> getChannelsByTenantId(Long tenantId);

    List<ChannelEntity> getChannelsByType(Long tenantId, ChannelType type);

    Optional<ChannelEntity> getChannelByEntity(Long tenantId, String entityType, Long entityId);

    ChannelEntity updateChannel(Long channelId, String name, String description);

    ChannelEntity incrementMemberCount(Long channelId);

    ChannelEntity decrementMemberCount(Long channelId);

    ChannelEntity archiveChannel(Long channelId);

    ChannelEntity unarchiveChannel(Long channelId);

    void recordMessage(Long channelId);

    void recordMessage(ChannelEntity channel);

    void deleteChannel(Long channelId);
}
