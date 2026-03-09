/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Discuss-specific cache service interface
 */

package serp.project.discuss_service.core.service;

import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.domain.entity.UserPresenceEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IDiscussCacheService {

    // ==================== CACHE KEY PREFIXES ====================

    String CHANNEL_PREFIX = "discuss:channel:";
    String MESSAGE_PREFIX = "discuss:msg:";
    String CHANNEL_MEMBERS_PREFIX = "discuss:members:";
    String TYPING_PREFIX = "discuss:typing:";
    String UNREAD_PREFIX = "discuss:unread:";
    String SESSION_PREFIX = "discuss:session:";
    String USER_SESSIONS_PREFIX = "discuss:user_sessions:";
    String CHANNEL_SUBSCRIBERS_PREFIX = "discuss:channel_subscribers:";
    String USER_SUBSCRIPTIONS_PREFIX = "discuss:user_subscriptions:";
    String ATTACHMENT_URL_PREFIX = "discuss:attachment_url:";
    String CHANNEL_MESSAGES_PREFIX = "discuss:channel_messages:";

    String USER_PRESENCE_HASH_PREFIX = "discuss:presence:user:";

    // TTL Constants (in seconds)
    long CHANNEL_TTL = 3600;           // 1 hour
    long MESSAGE_TTL = 300;            // 5 minutes
    long CHANNEL_MESSAGES_TTL = 60;    // 1 minute
    long PRESENCE_HASH_TTL = 604800;   // 7 days
    long TYPING_TTL = 5;               // 5 seconds
    long SESSION_TTL = 86400;          // 24 hours
    long ATTACHMENT_URL_TTL = 561600;  // 6.5 days

    // ==================== CHANNEL CACHE ====================

    void cacheChannel(ChannelEntity channel);

    Optional<ChannelEntity> getCachedChannel(Long channelId);

    void invalidateChannel(Long channelId);

    // ==================== MESSAGE CACHE ====================

    void cacheMessage(MessageEntity message);

    Optional<MessageEntity> getCachedMessage(Long messageId);

    void invalidateMessage(Long messageId);

    // ==================== CHANNEL MESSAGES PAGE CACHE ====================

    /**
     * Only caches the first page (page=0) for hot data access.
     */
    void cacheChannelMessagesPage(Long channelId, int page, int size,
                                  List<MessageEntity> messages, long totalCount);

    Optional<CachedMessagesPage> getCachedChannelMessagesPage(Long channelId, int page, int size);

    void invalidateChannelMessagesPage(Long channelId);

    /**
     * Invalidate cached channel messages page using non-blocking SCAN.
     * This is safer for production as it doesn't block Redis like KEYS command.
     */
    void invalidateChannelMessagesPageAsync(Long channelId);

    /**
     * This avoids a DB round-trip on the next read request.
     */
    boolean prependMessageToFirstPage(Long channelId, MessageEntity message, int pageSize);

    boolean removeMessageFromFirstPage(Long channelId, Long messageId);

    // ==================== PRESENCE ====================

    void setUserPresence(UserPresenceEntity presence);

    Optional<UserPresenceEntity> getUserPresence(Long userId);

    Map<Long, UserPresenceEntity> getUserPresenceBatch(Set<Long> userIds);


    // ==================== TYPING INDICATORS ====================

    void setUserTyping(Long channelId, Long userId);

    void clearUserTyping(Long channelId, Long userId);

    Set<Long> getTypingUsers(Long channelId);

    // ==================== UNREAD COUNTS ====================

    void cacheUnreadCount(Long userId, Long channelId, int count);

    void incrementUnreadCount(Long userId, Long channelId);

    void incrementUnreadCountBatch(Set<Long> userIds, Long channelId);

    void resetUnreadCount(Long userId, Long channelId);

    Optional<Integer> getCachedUnreadCount(Long userId, Long channelId);

    long getTotalUnreadCount(Long userId);

    // ==================== SESSION MANAGEMENT ====================

    void storeSession(String sessionId, Long userId, String instanceId);

    Optional<SessionInfo> getSession(String sessionId);

    Set<String> getUserSessions(Long userId);

    void removeSession(String sessionId, Long userId);

    int getActiveSessionCount(Long userId);

    // ==================== CHANNEL SUBSCRIPTIONS ====================
    void addUserChannelSubscription(Long userId, Long channelId);

    void removeUserChannelSubscription(Long userId, Long channelId);

    Set<Long> getChannelSubscribers(Long channelId);

    Set<Long> getUserSubscribedChannels(Long userId);

    void removeAllUserSubscriptions(Long userId);

    // ==================== CHANNEL MEMBERS CACHE ====================

    void cacheChannelMembers(Long channelId, Set<Long> memberIds);

    Set<Long> getCachedChannelMembers(Long channelId);

    void addMemberToChannelCache(Long channelId, Long userId);

    void removeMemberFromChannelCache(Long channelId, Long userId);

    boolean isMemberCached(Long channelId, Long userId);

    // ==================== ATTACHMENT URL CACHE ====================

    void cacheAttachmentUrl(Long attachmentId, CachedAttachmentUrl urlInfo);

    Optional<CachedAttachmentUrl> getCachedAttachmentUrl(Long attachmentId);

    void invalidateAttachmentUrl(Long attachmentId);

    // ==================== VALUE OBJECTS ====================

    record SessionInfo(String sessionId, Long userId, String instanceId, long createdAt) {
    }

    record CachedAttachmentUrl(String downloadUrl, String thumbnailUrl, long expiresAt) {
    }

    record CachedMessagesPage(long totalCount, List<MessageEntity> messages) {
    }
}
