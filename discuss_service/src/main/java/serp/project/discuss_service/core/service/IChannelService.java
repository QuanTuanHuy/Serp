/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel service interface
 */

package serp.project.discuss_service.core.service;

import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.enums.ChannelType;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for channel business operations.
 * Handles channel CRUD and business logic.
 */
public interface IChannelService {

    /**
     * Create a new channel
     */
    ChannelEntity createChannel(ChannelEntity channel);

    /**
     * Create or get existing DIRECT channel between two users
     */
    ChannelEntity getOrCreateDirectChannel(Long tenantId, Long userId1, Long userId2);

    /**
     * Create a GROUP channel
     */
    ChannelEntity createGroupChannel(Long tenantId, Long createdBy, String name, 
                                     String description, boolean isPrivate);

    /**
     * Create a TOPIC channel linked to an entity
     */
    ChannelEntity createTopicChannel(Long tenantId, Long createdBy, String name,
                                     String entityType, Long entityId);

    /**
     * Get channel by ID
     */
    Optional<ChannelEntity> getChannelById(Long id);

    /**
     * Get channel by ID, throw exception if not found
     */
    ChannelEntity getChannelByIdOrThrow(Long id);

    /**
     * Get channels for tenant
     */
    List<ChannelEntity> getChannelsByTenantId(Long tenantId);

    /**
     * Get channels by type
     */
    List<ChannelEntity> getChannelsByType(Long tenantId, ChannelType type);

    /**
     * Get TOPIC channel by entity
     */
    Optional<ChannelEntity> getChannelByEntity(Long tenantId, String entityType, Long entityId);

    /**
     * Update channel info
     */
    ChannelEntity updateChannel(Long channelId, String name, String description);

    /**
     * Increment member count
     */
    ChannelEntity incrementMemberCount(Long channelId);

    /**
     * Decrement member count
     */
    ChannelEntity decrementMemberCount(Long channelId);

    /**
     * Archive channel
     */
    ChannelEntity archiveChannel(Long channelId);

    /**
     * Unarchive channel
     */
    ChannelEntity unarchiveChannel(Long channelId);

    /**
     * Record new message in channel (update stats)
     */
    void recordMessage(Long channelId);

    /**
     * Record new message in channel with an already-loaded ChannelEntity.
     *
     * @param channel The channel entity (already loaded)
     */
    void recordMessage(ChannelEntity channel);

    /**
     * Delete channel
     */
    void deleteChannel(Long channelId);
}
