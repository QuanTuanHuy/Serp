/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel service implementation
 */

package serp.project.discuss_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import serp.project.discuss_service.core.domain.dto.request.GetChannelsParams;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.enums.ChannelType;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.port.store.IChannelPort;
import serp.project.discuss_service.core.service.IChannelService;
import serp.project.discuss_service.core.service.IDiscussCacheService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelService implements IChannelService {

    private final IChannelPort channelPort;
    private final IDiscussCacheService cacheService;

    @Override
    public ChannelEntity createChannel(ChannelEntity channel) {
        channel.validateForCreation();
        ChannelEntity saved = channelPort.save(channel);
        cacheService.cacheChannel(saved);
        log.info("Created channel: {} (type: {})", saved.getId(), saved.getType());
        return saved;
    }

    @Override
    public ChannelEntity getOrCreateDirectChannel(Long tenantId, Long userId1, Long userId2) {
        Optional<ChannelEntity> existing = channelPort.findDirectChannel(tenantId, userId1, userId2);
        if (existing.isPresent()) {
            log.debug("Found existing DIRECT channel between {} and {}", userId1, userId2);
            return existing.get();
        }

        ChannelEntity channel = ChannelEntity.createDirect(tenantId, userId1, userId2);
        ChannelEntity saved = channelPort.save(channel);
        cacheService.cacheChannel(saved);
        log.info("Created new DIRECT channel {} between {} and {}", saved.getId(), userId1, userId2);
        return saved;
    }

    @Override
    public ChannelEntity createGroupChannel(Long tenantId, Long createdBy, String name,
            String description, boolean isPrivate) {
        ChannelEntity channel = ChannelEntity.createGroup(tenantId, createdBy, name, description, isPrivate);
        ChannelEntity saved = channelPort.save(channel);
        cacheService.cacheChannel(saved);
        log.info("Created GROUP channel: {} (name: {})", saved.getId(), name);
        return saved;
    }

    @Override
    public ChannelEntity createTopicChannel(Long tenantId, Long createdBy, String name,
            String entityType, Long entityId) {
        Optional<ChannelEntity> existing = channelPort.findByEntity(tenantId, entityType, entityId);
        if (existing.isPresent()) {
            log.debug("Found existing TOPIC channel for {}:{}", entityType, entityId);
            return existing.get();
        }

        ChannelEntity channel = ChannelEntity.createTopic(tenantId, createdBy, name, entityType, entityId);
        ChannelEntity saved = channelPort.save(channel);
        cacheService.cacheChannel(saved);
        log.info("Created TOPIC channel: {} for {}:{}", saved.getId(), entityType, entityId);
        return saved;
    }

    @Override
    public Optional<ChannelEntity> getChannelById(Long id) {
        Optional<ChannelEntity> cached = cacheService.getCachedChannel(id);
        if (cached.isPresent()) {
            log.debug("Cache hit for channel: {}", id);
            return cached;
        }

        Optional<ChannelEntity> channel = channelPort.findById(id);
        channel.ifPresent(cacheService::cacheChannel);
        return channel;
    }

    @Override
    public ChannelEntity getChannelByIdOrThrow(Long id) {
        return getChannelById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHANNEL_NOT_FOUND));
    }

    @Override
    public List<ChannelEntity> getChannelsByTenantId(Long tenantId) {
        return channelPort.findByTenantId(tenantId);
    }

    @Override
    public Pair<Long, List<ChannelEntity>> getUserChannels(Long userId, Long tenantId, GetChannelsParams params) {
        GetChannelsParams normalizedParams = normalizeGetChannelsParams(params);

        return channelPort.findUserChannelsPaginated(
                userId,
                tenantId,
                normalizedParams.getPage(),
                normalizedParams.getPageSize(),
                normalizedParams.getType(),
                normalizedParams.getIsArchived(),
                normalizedParams.getEntityType(),
                normalizedParams.getEntityId(),
                normalizedParams.getSearchQuery());
    }

    private GetChannelsParams normalizeGetChannelsParams(GetChannelsParams params) {
        GetChannelsParams normalized = params == null ? new GetChannelsParams() : params;

        if (params == null) {
            normalized.setPage(0);
            normalized.setPageSize(20);
        }

        if (normalized.getPage() < 0) {
            normalized.setPage(0);
        }
        if (normalized.getPageSize() <= 0) {
            normalized.setPageSize(20);
        }
        if (normalized.getPageSize() > 100) {
            normalized.setPageSize(100);
        }

        if (normalized.getIsArchived() == null) {
            normalized.setIsArchived(false);
        }

        if (normalized.getType() == ChannelType.DIRECT || normalized.getType() == ChannelType.GROUP) {
            normalized.setEntityId(null);
            normalized.setEntityType(null);
        }

        if (normalized.getEntityType() != null) {
            String entityType = normalized.getEntityType().trim();
            normalized.setEntityType(entityType.isEmpty() ? null : entityType);
        }

        if ((normalized.getEntityId() == null) != (normalized.getEntityType() == null)) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "entityId and entityType must be provided together");
        }

        if (normalized.getSearchQuery() != null) {
            String searchQuery = normalized.getSearchQuery().trim();
            normalized.setSearchQuery(searchQuery.isEmpty() ? null : searchQuery);
        }

        return normalized;
    }

    @Override
    public List<ChannelEntity> getChannelsByType(Long tenantId, ChannelType type) {
        return channelPort.findByTenantIdAndType(tenantId, type);
    }

    @Override
    public Optional<ChannelEntity> getChannelByEntity(Long tenantId, String entityType, Long entityId) {
        return channelPort.findByEntity(tenantId, entityType, entityId);
    }

    @Override
    public ChannelEntity updateChannel(Long channelId, String name, String description) {
        ChannelEntity channel = getChannelByIdOrThrow(channelId);
        channel.updateInfo(name, description);
        ChannelEntity saved = channelPort.save(channel);
        cacheService.cacheChannel(saved);
        log.info("Updated channel: {}", channelId);
        return saved;
    }

    @Override
    public ChannelEntity incrementMemberCount(Long channelId) {
        ChannelEntity channel = getChannelByIdOrThrow(channelId);
        channel.incrementMemberCount();
        ChannelEntity saved = channelPort.save(channel);
        cacheService.cacheChannel(saved);
        log.info("Incremented member count for channel: {}", channelId);
        return saved;
    }

    @Override
    public ChannelEntity decrementMemberCount(Long channelId) {
        ChannelEntity channel = getChannelByIdOrThrow(channelId);
        channel.decrementMemberCount();
        ChannelEntity saved = channelPort.save(channel);
        cacheService.cacheChannel(saved);
        log.info("Decremented member count for channel: {}", channelId);
        return saved;
    }

    @Override
    public ChannelEntity archiveChannel(Long channelId) {
        ChannelEntity channel = getChannelByIdOrThrow(channelId);
        channel.archive();
        ChannelEntity saved = channelPort.save(channel);
        cacheService.invalidateChannel(channelId);
        log.info("Archived channel: {}", channelId);
        return saved;
    }

    @Override
    public ChannelEntity unarchiveChannel(Long channelId) {
        ChannelEntity channel = getChannelByIdOrThrow(channelId);
        channel.unarchive();
        ChannelEntity saved = channelPort.save(channel);
        cacheService.cacheChannel(saved);
        log.info("Unarchived channel: {}", channelId);
        return saved;
    }

    @Override
    public void recordMessage(Long channelId) {
        ChannelEntity channel = getChannelByIdOrThrow(channelId);
        recordMessage(channel);
    }

    @Override
    public void recordMessage(ChannelEntity channel) {
        channel.recordMessage();
        channelPort.save(channel);
        cacheService.invalidateChannel(channel.getId());
    }

    @Override
    public void deleteChannel(Long channelId) {
        cacheService.invalidateChannel(channelId);
        channelPort.deleteById(channelId);
        log.info("Deleted channel: {}", channelId);
    }
}
