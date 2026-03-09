/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel member port interface
 */

package serp.project.discuss_service.core.port.store;

import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.enums.MemberStatus;

import java.util.List;
import java.util.Optional;

public interface IChannelMemberPort {

    /**
     * Save a channel member (create or update)
     */
    ChannelMemberEntity save(ChannelMemberEntity member);

    /**
     * Find member by channel and user
     */
    Optional<ChannelMemberEntity> findByChannelIdAndUserId(Long channelId, Long userId);

    /**
     * Find all members in a channel with specific status
     */
    List<ChannelMemberEntity> findByChannelIdAndStatus(Long channelId, MemberStatus status);

    /**
     * Find all members in a channel (any status)
     */
    List<ChannelMemberEntity> findByChannelId(Long channelId);

    /**
     * Find all channels for a user with specific status
     */
    List<ChannelMemberEntity> findByUserIdAndStatus(Long userId, MemberStatus status);

    /**
     * Find channels with unread messages for a user
     */
    List<ChannelMemberEntity> findChannelsWithUnread(Long userId);

    /**
     * Find pinned channels for a user
     */
    List<ChannelMemberEntity> findPinnedChannels(Long userId);

    /**
     * Count active members in a channel
     */
    long countActiveMembers(Long channelId);

    /**
     * Check if user is member of channel
     */
    boolean isMember(Long channelId, Long userId);

    /**
     * Increment unread count for all members except sender
     */
    int incrementUnreadForChannel(Long channelId, Long senderId);

    /**
     * Mark messages as read for user in channel
     */
    int markAsRead(Long channelId, Long userId, Long messageId);

    /**
     * Delete member (hard delete)
     */
    void delete(ChannelMemberEntity member);

    /**
     * Delete all members in a channel
     */
    void deleteByChannelId(Long channelId);
}
