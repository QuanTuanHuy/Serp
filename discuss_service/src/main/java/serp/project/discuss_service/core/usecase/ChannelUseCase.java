/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel use case
 */

package serp.project.discuss_service.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.enums.MemberRole;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.service.IChannelMemberService;
import serp.project.discuss_service.core.service.IChannelService;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;
import serp.project.discuss_service.core.service.IPresenceService;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChannelUseCase {

    private final IChannelService channelService;
    private final IChannelMemberService memberService;
    private final IDiscussEventPublisher eventPublisher;
    private final IPresenceService presenceService;

    @Transactional
    public ChannelEntity createGroupChannel(Long tenantId, Long createdBy, String name,
                                            String description, boolean isPrivate,
                                            List<Long> initialMemberIds) {
        ChannelEntity channel = channelService.createGroupChannel(tenantId, createdBy, name, description, isPrivate);

        memberService.addOwner(channel.getId(), createdBy, tenantId);

        if (initialMemberIds != null && !initialMemberIds.isEmpty()) {
            List<Long> filteredMembers = initialMemberIds.stream()
                    .filter(id -> !id.equals(createdBy))
                    .toList();
            memberService.addMembers(channel.getId(), filteredMembers, tenantId);
            
            channel.setMemberCount(1 + filteredMembers.size());
        }

        eventPublisher.publishChannelCreated(channel);
        
        log.info("Created GROUP channel {} with {} members", channel.getId(), 
                initialMemberIds != null ? initialMemberIds.size() + 1 : 1);
        return channel;
    }

    @Transactional
    public ChannelEntity getOrCreateDirectChannel(Long tenantId, Long userId1, Long userId2) {
        ChannelEntity channel = channelService.getOrCreateDirectChannel(tenantId, userId1, userId2);
        
        if (!memberService.isMember(channel.getId(), userId1)) {
            memberService.addOwner(channel.getId(), userId1, tenantId);
        }
        if (!memberService.isMember(channel.getId(), userId2)) {
            memberService.addMember(channel.getId(), userId2, tenantId, MemberRole.MEMBER);
        }

        return channel;
    }

    @Transactional
    public ChannelEntity createTopicChannel(Long tenantId, Long createdBy, String name,
                                           String entityType, Long entityId,
                                           List<Long> initialMemberIds) {
        ChannelEntity channel = channelService.createTopicChannel(tenantId, createdBy, name, entityType, entityId);

        if (!memberService.isMember(channel.getId(), createdBy)) {
            memberService.addOwner(channel.getId(), createdBy, tenantId);
        }

        if (initialMemberIds != null && !initialMemberIds.isEmpty()) {
            List<Long> filteredMembers = initialMemberIds.stream()
                    .filter(id -> !id.equals(createdBy))
                    .toList();
            memberService.addMembers(channel.getId(), filteredMembers, tenantId);
        }

        eventPublisher.publishChannelCreated(channel);
        return channel;
    }

    @Transactional(readOnly = true)
    public ChannelEntity getChannelWithMembers(Long channelId) {
        ChannelEntity channel = channelService.getChannelByIdOrThrow(channelId);
        List<ChannelMemberEntity> members = memberService.getActiveMembers(channelId);
        channel.setMembers(members);
        return channel;
    }

    @Transactional(readOnly = true)
    public List<ChannelMemberEntity> getChannelMembers(Long channelId, Long userId) {
        if (!memberService.isMember(channelId, userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }
        return memberService.getActiveMembers(channelId);
    }

    @Transactional(readOnly = true)
    public List<ChannelEntity> getUserChannels(Long userId, Long tenantId) {
        List<ChannelMemberEntity> memberships = memberService.getUserChannels(userId);
        List<Long> channelIds = memberships.stream()
                .map(ChannelMemberEntity::getChannelId)
                .toList();
        
        return channelService.getChannelsByTenantId(tenantId).stream()
                .filter(c -> channelIds.contains(c.getId()))
                .toList();
    }

    @Transactional
    public ChannelEntity updateChannel(Long channelId, Long userId, String name, String description) {
        if (!memberService.canManageChannel(channelId, userId)) {
            throw new AppException(ErrorCode.CHANNEL_UPDATE_FORBIDDEN);
        }

        ChannelEntity channel = channelService.updateChannel(channelId, name, description);
        eventPublisher.publishChannelUpdated(channel);
        return channel;
    }

    @Transactional
    public ChannelEntity archiveChannel(Long channelId, Long userId) {
        if (!memberService.canManageChannel(channelId, userId)) {
            throw new AppException(ErrorCode.CHANNEL_ARCHIVE_FORBIDDEN);
        }

        ChannelEntity channel = channelService.archiveChannel(channelId);
        eventPublisher.publishChannelArchived(channel);
        return channel;
    }

    @Transactional
    public ChannelMemberEntity addMember(Long channelId, Long userId, Long addedBy, Long tenantId) {
        if (!memberService.canManageChannel(channelId, addedBy)) {
            throw new AppException(ErrorCode.CANNOT_ADD_MEMBERS);
        }

        ChannelMemberEntity member = memberService.addMember(channelId, userId, tenantId, MemberRole.MEMBER);        
        channelService.incrementMemberCount(channelId);

        eventPublisher.publishMemberJoined(member);
        return member;
    }

    @Transactional
    public ChannelMemberEntity removeMember(Long channelId, Long userId, Long removerId) {
        if (!memberService.canManageChannel(channelId, removerId)) {
            throw new AppException(ErrorCode.CANNOT_REMOVE_MEMBERS);
        }

        ChannelMemberEntity member = memberService.removeMember(channelId, userId, removerId);        
        channelService.decrementMemberCount(channelId);

        eventPublisher.publishMemberRemoved(member);
        return member;
    }

    @Transactional
    public ChannelMemberEntity leaveChannel(Long channelId, Long userId) {
        ChannelMemberEntity member = memberService.leaveChannel(channelId, userId);
        channelService.decrementMemberCount(channelId);

        eventPublisher.publishMemberLeft(member);
        return member;
    }

    @Transactional
    public void deleteChannel(Long channelId, Long userId) {
        if (!memberService.canManageChannel(channelId, userId)) {
            throw new AppException(ErrorCode.CHANNEL_DELETE_FORBIDDEN);
        }

        channelService.deleteChannel(channelId);
        log.info("Deleted channel {} by user {}", channelId, userId);
    }

    @Transactional(readOnly = true)
    public Set<Long> getOnlineMembers(Long channelId) {
        Set<Long> memberIds = memberService.getMemberIds(channelId);
        return presenceService.getOnlineUsers(memberIds);
    }
}
