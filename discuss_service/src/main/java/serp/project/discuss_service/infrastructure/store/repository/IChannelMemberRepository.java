/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel member repository
 */

package serp.project.discuss_service.infrastructure.store.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.discuss_service.core.domain.enums.MemberStatus;
import serp.project.discuss_service.infrastructure.store.model.ChannelMemberModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IChannelMemberRepository extends IBaseRepository<ChannelMemberModel> {

    Optional<ChannelMemberModel> findByChannelIdAndUserId(Long channelId, Long userId);

    List<ChannelMemberModel> findByChannelIdAndStatus(Long channelId, MemberStatus status);

    List<ChannelMemberModel> findByChannelId(Long channelId);

    List<ChannelMemberModel> findByUserIdAndStatus(Long userId, MemberStatus status);

    @Query("SELECT cm FROM ChannelMemberModel cm WHERE cm.userId = :userId " +
           "AND cm.status = 'ACTIVE' AND cm.unreadCount > 0 ORDER BY cm.updatedAt DESC")
    List<ChannelMemberModel> findChannelsWithUnread(@Param("userId") Long userId);

    List<ChannelMemberModel> findByUserIdAndIsPinnedTrueAndStatus(Long userId, MemberStatus status);

    long countByChannelIdAndStatus(Long channelId, MemberStatus status);

    boolean existsByChannelIdAndUserIdAndStatus(Long channelId, Long userId, MemberStatus status);

    @Modifying
    @Query("UPDATE ChannelMemberModel cm SET cm.unreadCount = cm.unreadCount + 1 " +
           "WHERE cm.channelId = :channelId AND cm.userId != :senderId AND cm.status = 'ACTIVE'")
    int incrementUnreadForChannel(@Param("channelId") Long channelId, @Param("senderId") Long senderId);

    @Modifying
    @Query("UPDATE ChannelMemberModel cm SET cm.unreadCount = 0, cm.lastReadMsgId = :messageId " +
           "WHERE cm.channelId = :channelId AND cm.userId = :userId")
    int markAsRead(@Param("channelId") Long channelId, @Param("userId") Long userId, @Param("messageId") Long messageId);

    void deleteByChannelId(Long channelId);
}
