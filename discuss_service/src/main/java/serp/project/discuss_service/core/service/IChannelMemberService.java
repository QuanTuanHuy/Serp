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

public interface IChannelMemberService {

    ChannelMemberEntity addMember(Long channelId, Long userId, Long tenantId, MemberRole role);

    ChannelMemberEntity addOwner(Long channelId, Long userId, Long tenantId);

    List<ChannelMemberEntity> addMembers(Long channelId, List<Long> userIds, Long tenantId);

    Optional<ChannelMemberEntity> getMember(Long channelId, Long userId);

    ChannelMemberEntity getMemberOrThrow(Long channelId, Long userId);

    List<ChannelMemberEntity> getActiveMembers(Long channelId);

    Set<Long> getMemberIds(Long channelId);

    List<ChannelMemberEntity> getUserChannels(Long userId);

    List<ChannelMemberEntity> getPinnedChannels(Long userId);

    List<ChannelMemberEntity> getChannelsWithUnread(Long userId);

    boolean isMember(Long channelId, Long userId);

    boolean canSendMessages(Long channelId, Long userId);

    ChannelMemberEntity getMemberWithSendPermission(Long channelId, Long userId);

    boolean canManageChannel(Long channelId, Long userId);

    ChannelMemberEntity promoteToAdmin(Long channelId, Long userId);

    ChannelMemberEntity demoteToMember(Long channelId, Long userId);

    void transferOwnership(Long channelId, Long currentOwnerId, Long newOwnerId);

    ChannelMemberEntity leaveChannel(Long channelId, Long userId);

    ChannelMemberEntity removeMember(Long channelId, Long userId, Long removerId);

    ChannelMemberEntity toggleMute(Long channelId, Long userId);

    ChannelMemberEntity togglePin(Long channelId, Long userId);

    ChannelMemberEntity updateNotificationLevel(Long channelId, Long userId, NotificationLevel level);

    ChannelMemberEntity markAsRead(Long channelId, Long userId, Long messageId);

    void incrementUnreadForChannel(Long channelId, Long senderId);

    long countActiveMembers(Long channelId);
}
