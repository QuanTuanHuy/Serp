/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel member service implementation
 */

package serp.project.discuss_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.enums.MemberRole;
import serp.project.discuss_service.core.domain.enums.MemberStatus;
import serp.project.discuss_service.core.domain.enums.NotificationLevel;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.port.store.IChannelMemberPort;
import serp.project.discuss_service.core.service.IChannelMemberService;
import serp.project.discuss_service.core.service.IDiscussCacheService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of channel member service.
 * Handles member business operations with caching.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelMemberService implements IChannelMemberService {

    private final IChannelMemberPort memberPort;
    private final IDiscussCacheService cacheService;

    @Override
    public ChannelMemberEntity addMember(Long channelId, Long userId, Long tenantId, MemberRole role) {
        Optional<ChannelMemberEntity> existing = memberPort.findByChannelIdAndUserId(channelId, userId);
        if (existing.isPresent()) {
            ChannelMemberEntity member = existing.get();
            if (member.getStatus() == MemberStatus.LEFT) {
                member.rejoin();
                ChannelMemberEntity saved = memberPort.save(member);
                updateCaches(channelId, userId);
                log.info("User {} rejoined channel {}", userId, channelId);
                return saved;
            }
            log.debug("User {} is already member of channel {}", userId, channelId);
            return member;
        }

        ChannelMemberEntity member = role == MemberRole.OWNER
                ? ChannelMemberEntity.createOwner(channelId, userId, tenantId)
                : ChannelMemberEntity.createMember(channelId, userId, tenantId);
        
        if (role == MemberRole.ADMIN) {
            member.setRole(MemberRole.ADMIN);
        } else if (role == MemberRole.GUEST) {
            member.setRole(MemberRole.GUEST);
        }

        ChannelMemberEntity saved = memberPort.save(member);
        updateCaches(channelId, userId);
        log.info("Added user {} to channel {} with role {}", userId, channelId, role);
        return saved;
    }

    @Override
    public ChannelMemberEntity addOwner(Long channelId, Long userId, Long tenantId) {
        return addMember(channelId, userId, tenantId, MemberRole.OWNER);
    }

    @Override
    public List<ChannelMemberEntity> addMembers(Long channelId, List<Long> userIds, Long tenantId) {
        List<ChannelMemberEntity> added = new ArrayList<>();
        for (Long userId : userIds) {
            ChannelMemberEntity member = addMember(channelId, userId, tenantId, MemberRole.MEMBER);
            added.add(member);
        }
        return added;
    }

    @Override
    public Optional<ChannelMemberEntity> getMember(Long channelId, Long userId) {
        return memberPort.findByChannelIdAndUserId(channelId, userId);
    }

    @Override
    public ChannelMemberEntity getMemberOrThrow(Long channelId, Long userId) {
        return getMember(channelId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Override
    public List<ChannelMemberEntity> getActiveMembers(Long channelId) {
        return memberPort.findByChannelIdAndStatus(channelId, MemberStatus.ACTIVE);
    }

    @Override
    public Set<Long> getMemberIds(Long channelId) {
        Set<Long> cached = cacheService.getCachedChannelMembers(channelId);
        if (!cached.isEmpty()) {
            return cached;
        }

        List<ChannelMemberEntity> members = getActiveMembers(channelId);
        Set<Long> memberIds = members.stream()
                .map(ChannelMemberEntity::getUserId)
                .collect(Collectors.toSet());
        
        cacheService.cacheChannelMembers(channelId, memberIds);
        return memberIds;
    }

    @Override
    public List<ChannelMemberEntity> getUserChannels(Long userId) {
        return memberPort.findByUserIdAndStatus(userId, MemberStatus.ACTIVE);
    }

    @Override
    public List<ChannelMemberEntity> getPinnedChannels(Long userId) {
        return memberPort.findPinnedChannels(userId);
    }

    @Override
    public List<ChannelMemberEntity> getChannelsWithUnread(Long userId) {
        return memberPort.findChannelsWithUnread(userId);
    }

    @Override
    public boolean isMember(Long channelId, Long userId) {
        if (cacheService.isMemberCached(channelId, userId)) {
            return true;
        }
        return memberPort.isMember(channelId, userId);
    }

    @Override
    public boolean canSendMessages(Long channelId, Long userId) {
        return getMember(channelId, userId)
                .map(ChannelMemberEntity::canSendMessages)
                .orElse(false);
    }

    @Override
    public ChannelMemberEntity getMemberWithSendPermission(Long channelId, Long userId) {
        ChannelMemberEntity member = getMember(channelId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_CHANNEL_MEMBER));
        
        if (!member.canSendMessages()) {
            throw new AppException(ErrorCode.CANNOT_SEND_MESSAGES);
        }
        
        return member;
    }

    @Override
    public boolean canManageChannel(Long channelId, Long userId) {
        return getMember(channelId, userId)
                .map(ChannelMemberEntity::canManageChannel)
                .orElse(false);
    }

    @Override
    public ChannelMemberEntity promoteToAdmin(Long channelId, Long userId) {
        ChannelMemberEntity member = getMemberOrThrow(channelId, userId);
        member.promoteToAdmin();
        ChannelMemberEntity saved = memberPort.save(member);
        log.info("Promoted user {} to admin in channel {}", userId, channelId);
        return saved;
    }

    @Override
    public ChannelMemberEntity demoteToMember(Long channelId, Long userId) {
        ChannelMemberEntity member = getMemberOrThrow(channelId, userId);
        member.demoteToMember();
        ChannelMemberEntity saved = memberPort.save(member);
        log.info("Demoted user {} to member in channel {}", userId, channelId);
        return saved;
    }

    @Override
    public void transferOwnership(Long channelId, Long currentOwnerId, Long newOwnerId) {
        ChannelMemberEntity currentOwner = getMemberOrThrow(channelId, currentOwnerId);
        ChannelMemberEntity newOwner = getMemberOrThrow(channelId, newOwnerId);

        currentOwner.relinquishOwnership();
        newOwner.becomeOwner();

        memberPort.save(currentOwner);
        memberPort.save(newOwner);
        
        log.info("Transferred ownership of channel {} from {} to {}", channelId, currentOwnerId, newOwnerId);
    }

    @Override
    public ChannelMemberEntity leaveChannel(Long channelId, Long userId) {
        ChannelMemberEntity member = getMemberOrThrow(channelId, userId);
        member.leave();
        ChannelMemberEntity saved = memberPort.save(member);
        
        cacheService.removeMemberFromChannelCache(channelId, userId);
        cacheService.removeChannelFromUserCache(userId, channelId);
        
        log.info("User {} left channel {}", userId, channelId);
        return saved;
    }

    @Override
    public ChannelMemberEntity removeMember(Long channelId, Long userId, Long removerId) {
        ChannelMemberEntity member = getMemberOrThrow(channelId, userId);
        member.removeBy(removerId);
        ChannelMemberEntity saved = memberPort.save(member);
        
        cacheService.removeMemberFromChannelCache(channelId, userId);
        cacheService.removeChannelFromUserCache(userId, channelId);
        
        log.info("User {} was removed from channel {} by {}", userId, channelId, removerId);
        return saved;
    }

    @Override
    public ChannelMemberEntity toggleMute(Long channelId, Long userId) {
        ChannelMemberEntity member = getMemberOrThrow(channelId, userId);
        member.toggleMute();
        ChannelMemberEntity saved = memberPort.save(member);
        log.debug("Toggled mute for user {} in channel {}: muted={}", userId, channelId, member.getIsMuted());
        return saved;
    }

    @Override
    public ChannelMemberEntity togglePin(Long channelId, Long userId) {
        ChannelMemberEntity member = getMemberOrThrow(channelId, userId);
        member.togglePin();
        ChannelMemberEntity saved = memberPort.save(member);
        log.debug("Toggled pin for user {} in channel {}: pinned={}", userId, channelId, member.getIsPinned());
        return saved;
    }

    @Override
    public ChannelMemberEntity updateNotificationLevel(Long channelId, Long userId, NotificationLevel level) {
        ChannelMemberEntity member = getMemberOrThrow(channelId, userId);
        member.setNotificationPreference(level);
        ChannelMemberEntity saved = memberPort.save(member);
        log.debug("Updated notification level for user {} in channel {}: {}", userId, channelId, level);
        return saved;
    }

    @Override
    public ChannelMemberEntity markAsRead(Long channelId, Long userId, Long messageId) {
        ChannelMemberEntity member = getMemberOrThrow(channelId, userId);
        member.markAsRead(messageId);
        ChannelMemberEntity saved = memberPort.save(member);
        
        cacheService.resetUnreadCount(userId, channelId);
        log.debug("Marked messages as read for user {} in channel {} up to message {}", userId, channelId, messageId);
        return saved;
    }

    @Override
    public void incrementUnreadForChannel(Long channelId, Long senderId) {
        memberPort.incrementUnreadForChannel(channelId, senderId);
        
        Set<Long> memberIds = getMemberIds(channelId);
        Set<Long> otherMembers = memberIds.stream()
                .filter(memberId -> !memberId.equals(senderId))
                .collect(Collectors.toSet());
        
        if (!otherMembers.isEmpty()) {
            cacheService.incrementUnreadCountBatch(otherMembers, channelId);
        }
    }

    @Override
    public long countActiveMembers(Long channelId) {
        return memberPort.countActiveMembers(channelId);
    }

    private void updateCaches(Long channelId, Long userId) {
        cacheService.addMemberToChannelCache(channelId, userId);
        cacheService.addChannelToUserCache(userId, channelId);
    }
}
