/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel member service interface
 */

package serp.project.discuss_service.core.service;

import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.enums.MemberRole;
import serp.project.discuss_service.core.domain.enums.NotificationLevel;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for channel member business operations.
 * Handles membership management and member-related business logic.
 */
public interface IChannelMemberService {

    /**
     * Add member to channel
     */
    ChannelMemberEntity addMember(Long channelId, Long userId, Long tenantId, MemberRole role);

    /**
     * Add owner to channel (for new channels)
     */
    ChannelMemberEntity addOwner(Long channelId, Long userId, Long tenantId);

    /**
     * Add multiple members to channel
     */
    List<ChannelMemberEntity> addMembers(Long channelId, List<Long> userIds, Long tenantId);

    /**
     * Get member by channel and user
     */
    Optional<ChannelMemberEntity> getMember(Long channelId, Long userId);

    /**
     * Get member, throw exception if not found
     */
    ChannelMemberEntity getMemberOrThrow(Long channelId, Long userId);

    /**
     * Get all active members in channel
     */
    List<ChannelMemberEntity> getActiveMembers(Long channelId);

    /**
     * Get member IDs in channel
     */
    Set<Long> getMemberIds(Long channelId);

    /**
     * Get channels for user
     */
    List<ChannelMemberEntity> getUserChannels(Long userId);

    /**
     * Get pinned channels for user
     */
    List<ChannelMemberEntity> getPinnedChannels(Long userId);

    /**
     * Get channels with unread messages
     */
    List<ChannelMemberEntity> getChannelsWithUnread(Long userId);

    /**
     * Check if user is member
     */
    boolean isMember(Long channelId, Long userId);

    /**
     * Check if user can send messages
     */
    boolean canSendMessages(Long channelId, Long userId);

    /**
     * Get member entity and verify can send messages
     *
     * @param channelId Channel ID
     * @param userId    User ID
     * @return Member entity if found and can send messages
     * @throws AppException if user is not a member or cannot send messages
     */
    ChannelMemberEntity getMemberWithSendPermission(Long channelId, Long userId);

    /**
     * Check if user can manage channel
     */
    boolean canManageChannel(Long channelId, Long userId);

    /**
     * Promote member to admin
     */
    ChannelMemberEntity promoteToAdmin(Long channelId, Long userId);

    /**
     * Demote admin to member
     */
    ChannelMemberEntity demoteToMember(Long channelId, Long userId);

    /**
     * Transfer channel ownership
     */
    void transferOwnership(Long channelId, Long currentOwnerId, Long newOwnerId);

    /**
     * Member leaves channel
     */
    ChannelMemberEntity leaveChannel(Long channelId, Long userId);

    /**
     * Remove member from channel (by admin/owner)
     */
    ChannelMemberEntity removeMember(Long channelId, Long userId, Long removerId);

    /**
     * Toggle mute for member
     */
    ChannelMemberEntity toggleMute(Long channelId, Long userId);

    /**
     * Toggle pin for member
     */
    ChannelMemberEntity togglePin(Long channelId, Long userId);

    /**
     * Update notification level
     */
    ChannelMemberEntity updateNotificationLevel(Long channelId, Long userId, NotificationLevel level);

    /**
     * Mark messages as read
     */
    ChannelMemberEntity markAsRead(Long channelId, Long userId, Long messageId);

    /**
     * Increment unread count for all members except sender
     */
    void incrementUnreadForChannel(Long channelId, Long senderId);

    /**
     * Count active members in channel
     */
    long countActiveMembers(Long channelId);
}
