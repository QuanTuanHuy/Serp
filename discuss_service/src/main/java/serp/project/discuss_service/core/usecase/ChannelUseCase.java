/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel use case
 */

package serp.project.discuss_service.core.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.enums.MemberRole;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.service.IChannelMemberService;
import serp.project.discuss_service.core.service.IChannelService;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;

import java.util.List;
import java.util.Set;

/**
 * Use case for channel operations.
 * Orchestrates channel-related business logic across multiple services.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChannelUseCase {

    private final IChannelService channelService;
    private final IChannelMemberService memberService;
    private final IDiscussEventPublisher eventPublisher;

    /**
     * Create a new GROUP channel with the creator as owner
     */
    @Transactional
    public ChannelEntity createGroupChannel(Long tenantId, Long createdBy, String name,
                                            String description, boolean isPrivate,
                                            List<Long> initialMemberIds) {
        // Create channel
        ChannelEntity channel = channelService.createGroupChannel(tenantId, createdBy, name, description, isPrivate);

        // Add creator as owner
        memberService.addOwner(channel.getId(), createdBy, tenantId);

        // Add initial members
        if (initialMemberIds != null && !initialMemberIds.isEmpty()) {
            // Remove creator from initial members to avoid duplicate
            List<Long> filteredMembers = initialMemberIds.stream()
                    .filter(id -> !id.equals(createdBy))
                    .toList();
            memberService.addMembers(channel.getId(), filteredMembers, tenantId);
            
            // Update member count
            channel.setMemberCount(1 + filteredMembers.size());
        }

        // Publish event
        eventPublisher.publishChannelCreated(channel);
        
        log.info("Created GROUP channel {} with {} members", channel.getId(), 
                initialMemberIds != null ? initialMemberIds.size() + 1 : 1);
        return channel;
    }

    /**
     * Create or get a DIRECT channel between two users
     */
    @Transactional
    public ChannelEntity getOrCreateDirectChannel(Long tenantId, Long userId1, Long userId2) {
        ChannelEntity channel = channelService.getOrCreateDirectChannel(tenantId, userId1, userId2);
        
        // Check if we need to add members (new channel)
        if (!memberService.isMember(channel.getId(), userId1)) {
            memberService.addOwner(channel.getId(), userId1, tenantId);
        }
        if (!memberService.isMember(channel.getId(), userId2)) {
            memberService.addMember(channel.getId(), userId2, tenantId, MemberRole.MEMBER);
        }

        return channel;
    }

    /**
     * Create a TOPIC channel linked to an entity
     */
    @Transactional
    public ChannelEntity createTopicChannel(Long tenantId, Long createdBy, String name,
                                           String entityType, Long entityId,
                                           List<Long> initialMemberIds) {
        ChannelEntity channel = channelService.createTopicChannel(tenantId, createdBy, name, entityType, entityId);

        // Add creator as owner if not already a member
        if (!memberService.isMember(channel.getId(), createdBy)) {
            memberService.addOwner(channel.getId(), createdBy, tenantId);
        }

        // Add initial members
        if (initialMemberIds != null && !initialMemberIds.isEmpty()) {
            List<Long> filteredMembers = initialMemberIds.stream()
                    .filter(id -> !id.equals(createdBy))
                    .toList();
            memberService.addMembers(channel.getId(), filteredMembers, tenantId);
        }

        eventPublisher.publishChannelCreated(channel);
        return channel;
    }

    /**
     * Get channel with members
     */
    public ChannelEntity getChannelWithMembers(Long channelId) {
        ChannelEntity channel = channelService.getChannelByIdOrThrow(channelId);
        List<ChannelMemberEntity> members = memberService.getActiveMembers(channelId);
        channel.setMembers(members);
        return channel;
    }

    /**
     * Get user's channels
     */
    public List<ChannelEntity> getUserChannels(Long userId, Long tenantId) {
        List<ChannelMemberEntity> memberships = memberService.getUserChannels(userId);
        List<Long> channelIds = memberships.stream()
                .map(ChannelMemberEntity::getChannelId)
                .toList();
        
        return channelService.getChannelsByTenantId(tenantId).stream()
                .filter(c -> channelIds.contains(c.getId()))
                .toList();
    }

    /**
     * Update channel info
     */
    @Transactional
    public ChannelEntity updateChannel(Long channelId, Long userId, String name, String description) {
        // Verify user can manage channel
        if (!memberService.canManageChannel(channelId, userId)) {
            throw new AppException(ErrorCode.CHANNEL_UPDATE_FORBIDDEN);
        }

        ChannelEntity channel = channelService.updateChannel(channelId, name, description);
        eventPublisher.publishChannelUpdated(channel);
        return channel;
    }

    /**
     * Archive channel
     */
    @Transactional
    public ChannelEntity archiveChannel(Long channelId, Long userId) {
        if (!memberService.canManageChannel(channelId, userId)) {
            throw new AppException(ErrorCode.CHANNEL_ARCHIVE_FORBIDDEN);
        }

        ChannelEntity channel = channelService.archiveChannel(channelId);
        eventPublisher.publishChannelArchived(channel);
        return channel;
    }

    /**
     * Add member to channel
     */
    @Transactional
    public ChannelMemberEntity addMember(Long channelId, Long userId, Long addedBy, Long tenantId) {
        // Verify adder has permission
        if (!memberService.canManageChannel(channelId, addedBy)) {
            throw new AppException(ErrorCode.CANNOT_ADD_MEMBERS);
        }

        ChannelEntity channel = channelService.getChannelByIdOrThrow(channelId);
        ChannelMemberEntity member = memberService.addMember(channelId, userId, tenantId, MemberRole.MEMBER);
        
        // Update channel member count
        channel.incrementMemberCount();
        channelService.updateChannel(channelId, channel.getName(), channel.getDescription());

        eventPublisher.publishMemberJoined(member);
        return member;
    }

    /**
     * Remove member from channel
     */
    @Transactional
    public ChannelMemberEntity removeMember(Long channelId, Long userId, Long removerId) {
        // Verify remover has permission
        if (!memberService.canManageChannel(channelId, removerId)) {
            throw new AppException(ErrorCode.CANNOT_REMOVE_MEMBERS);
        }

        ChannelEntity channel = channelService.getChannelByIdOrThrow(channelId);
        ChannelMemberEntity member = memberService.removeMember(channelId, userId, removerId);
        
        // Update channel member count
        channel.decrementMemberCount();
        channelService.updateChannel(channelId, channel.getName(), channel.getDescription());

        eventPublisher.publishMemberRemoved(member);
        return member;
    }

    /**
     * Leave channel
     */
    @Transactional
    public ChannelMemberEntity leaveChannel(Long channelId, Long userId) {
        ChannelEntity channel = channelService.getChannelByIdOrThrow(channelId);
        ChannelMemberEntity member = memberService.leaveChannel(channelId, userId);
        
        channel.decrementMemberCount();
        channelService.updateChannel(channelId, channel.getName(), channel.getDescription());

        eventPublisher.publishMemberLeft(member);
        return member;
    }

    /**
     * Delete channel
     */
    @Transactional
    public void deleteChannel(Long channelId, Long userId) {
        if (!memberService.canManageChannel(channelId, userId)) {
            throw new AppException(ErrorCode.CHANNEL_DELETE_FORBIDDEN);
        }

        channelService.deleteChannel(channelId);
        log.info("Deleted channel {} by user {}", channelId, userId);
    }

    /**
     * Get online members in channel
     */
    public Set<Long> getOnlineMembers(Long channelId) {
        // This would integrate with presence service
        return memberService.getMemberIds(channelId);
    }
}
