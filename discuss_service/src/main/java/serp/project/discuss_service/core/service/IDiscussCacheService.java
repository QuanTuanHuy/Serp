/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Discuss-specific cache service interface
 */

package serp.project.discuss_service.core.service;

import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.MessageEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Discuss-specific cache service that provides high-level caching operations.
 * Uses ICachePort for low-level Redis operations.
 */
public interface IDiscussCacheService {

    // ==================== CACHE KEY PREFIXES ====================
    
    String CHANNEL_PREFIX = "discuss:channel:";
    String MESSAGE_PREFIX = "discuss:msg:";
    String RECENT_MESSAGES_PREFIX = "discuss:recent:";
    String USER_CHANNELS_PREFIX = "discuss:user_channels:";
    String CHANNEL_MEMBERS_PREFIX = "discuss:members:";
    String PRESENCE_PREFIX = "discuss:presence:";
    String CHANNEL_ONLINE_PREFIX = "discuss:online:";
    String TYPING_PREFIX = "discuss:typing:";
    String UNREAD_PREFIX = "discuss:unread:";
    String SESSION_PREFIX = "discuss:session:";
    String USER_SESSIONS_PREFIX = "discuss:user_sessions:";
    String ATTACHMENT_URL_PREFIX = "discuss:attachment_url:";
    String CHANNEL_MESSAGES_PREFIX = "discuss:channel_messages:";
    
    // TTL Constants (in seconds)
    long CHANNEL_TTL = 3600;          // 1 hour
    long MESSAGE_TTL = 300;            // 5 minutes
    long RECENT_MESSAGES_TTL = 600;    // 10 minutes
    long CHANNEL_MESSAGES_TTL = 60;    // 1 minute for paginated channel messages (hot data)
    long PRESENCE_TTL = 120;           // 2 minutes
    long TYPING_TTL = 5;               // 5 seconds
    long SESSION_TTL = 86400;          // 24 hours
    long ATTACHMENT_URL_TTL = 561600;  // 6.5 days (buffer before 7-day URL expiry)

    // ==================== CHANNEL CACHE ====================

    /**
     * Cache a channel
     */
    void cacheChannel(ChannelEntity channel);

    /**
     * Get cached channel
     */
    Optional<ChannelEntity> getCachedChannel(Long channelId);

    /**
     * Invalidate channel cache
     */
    void invalidateChannel(Long channelId);

    /**
     * Invalidate all channel caches for a tenant
     */
    void invalidateAllChannels(Long tenantId);

    // ==================== MESSAGE CACHE ====================

    /**
     * Cache a single message
     */
    void cacheMessage(MessageEntity message);

    /**
     * Get cached message
     */
    Optional<MessageEntity> getCachedMessage(Long messageId);

    /**
     * Cache recent messages for a channel (hot messages)
     */
    void cacheRecentMessages(Long channelId, List<MessageEntity> messages);

    /**
     * Get cached recent messages
     */
    List<MessageEntity> getCachedRecentMessages(Long channelId);

    /**
     * Add message to recent messages cache (LIFO - newest first)
     */
    void addToRecentMessages(Long channelId, MessageEntity message);

    /**
     * Invalidate message cache
     */
    void invalidateMessage(Long messageId);

    /**
     * Invalidate all message caches for a channel
     */
    void invalidateChannelMessages(Long channelId);

    // ==================== CHANNEL MESSAGES PAGE CACHE ====================

    /**
     * Cache a page of channel messages with total count.
     * Only caches the first page (page=0) for hot data access.
     *
     * @param channelId The channel ID
     * @param page      The page number (0-based)
     * @param size      The page size
     * @param messages  The messages to cache
     * @param totalCount The total message count
     */
    void cacheChannelMessagesPage(Long channelId, int page, int size, 
                                   List<MessageEntity> messages, long totalCount);

    /**
     * Get cached page of channel messages with total count.
     *
     * @param channelId The channel ID
     * @param page      The page number (0-based)
     * @param size      The page size
     * @return Optional containing Pair of (totalCount, messages) if cached
     */
    Optional<CachedMessagesPage> getCachedChannelMessagesPage(Long channelId, int page, int size);

    /**
     * Invalidate cached channel messages page (call after new message sent)
     *
     * @param channelId The channel ID to invalidate
     */
    void invalidateChannelMessagesPage(Long channelId);

    // ==================== PRESENCE / ONLINE STATUS ====================

    /**
     * Set user online status (with heartbeat)
     */
    void setUserOnline(Long userId);

    /**
     * Set user offline
     */
    void setUserOffline(Long userId);

    /**
     * Add user to channel's online set
     */
    void addUserToChannel(Long channelId, Long userId);

    /**
     * Remove user from channel's online set
     */
    void removeUserFromChannel(Long channelId, Long userId);

    /**
     * Get online users in a channel
     */
    Set<Long> getOnlineUsersInChannel(Long channelId);

    /**
     * Check if user is online
     */
    boolean isUserOnline(Long userId);

    /**
     * Refresh user's presence (heartbeat)
     */
    void refreshPresence(Long userId);

    // ==================== TYPING INDICATORS ====================

    /**
     * Set user typing in a channel (expires automatically after TYPING_TTL)
     */
    void setUserTyping(Long channelId, Long userId);

    /**
     * Clear user typing status
     */
    void clearUserTyping(Long channelId, Long userId);

    /**
     * Get users currently typing in a channel
     */
    Set<Long> getTypingUsers(Long channelId);

    // ==================== UNREAD COUNTS ====================

    /**
     * Cache unread count for user in channel
     */
    void cacheUnreadCount(Long userId, Long channelId, int count);

    /**
     * Increment unread count
     */
    void incrementUnreadCount(Long userId, Long channelId);

    /**
     * Reset unread count (mark as read)
     */
    void resetUnreadCount(Long userId, Long channelId);

    /**
     * Get cached unread count
     */
    Optional<Integer> getCachedUnreadCount(Long userId, Long channelId);

    /**
     * Get total unread count for user across all channels
     */
    long getTotalUnreadCount(Long userId);

    // ==================== SESSION MANAGEMENT ====================

    /**
     * Store WebSocket session info
     */
    void storeSession(String sessionId, Long userId, String instanceId);

    /**
     * Get session info
     */
    Optional<SessionInfo> getSession(String sessionId);

    /**
     * Get user's active session IDs
     */
    Set<String> getUserSessions(Long userId);

    /**
     * Remove session
     */
    void removeSession(String sessionId, Long userId);

    /**
     * Get count of active sessions for user
     */
    int getActiveSessionCount(Long userId);

    // ==================== CHANNEL MEMBERS CACHE ====================

    /**
     * Cache channel member IDs
     */
    void cacheChannelMembers(Long channelId, Set<Long> memberIds);

    /**
     * Get cached channel member IDs
     */
    Set<Long> getCachedChannelMembers(Long channelId);

    /**
     * Add member to channel members cache
     */
    void addMemberToChannelCache(Long channelId, Long userId);

    /**
     * Remove member from channel members cache
     */
    void removeMemberFromChannelCache(Long channelId, Long userId);

    /**
     * Check if user is member (from cache)
     */
    boolean isMemberCached(Long channelId, Long userId);

    // ==================== USER CHANNELS CACHE ====================

    /**
     * Cache user's channel IDs
     */
    void cacheUserChannels(Long userId, Set<Long> channelIds);

    /**
     * Get cached user's channel IDs
     */
    Set<Long> getCachedUserChannels(Long userId);

    /**
     * Add channel to user's channels cache
     */
    void addChannelToUserCache(Long userId, Long channelId);

    /**
     * Remove channel from user's channels cache
     */
    void removeChannelFromUserCache(Long userId, Long channelId);

    // ==================== ATTACHMENT URL CACHE ====================

    /**
     * Cache presigned URL for an attachment
     *
     * @param attachmentId The attachment ID
     * @param urlInfo      The cached URL information
     */
    void cacheAttachmentUrl(Long attachmentId, CachedAttachmentUrl urlInfo);

    /**
     * Get cached presigned URL for an attachment
     *
     * @param attachmentId The attachment ID
     * @return Optional containing cached URL info if present and not expired
     */
    Optional<CachedAttachmentUrl> getCachedAttachmentUrl(Long attachmentId);

    /**
     * Invalidate cached URL for an attachment
     *
     * @param attachmentId The attachment ID to invalidate
     */
    void invalidateAttachmentUrl(Long attachmentId);

    // ==================== VALUE OBJECTS ====================

    /**
     * Session info value object
     */
    record SessionInfo(String sessionId, Long userId, String instanceId, long createdAt) {}

    /**
     * Cached attachment URL value object
     */
    record CachedAttachmentUrl(String downloadUrl, String thumbnailUrl, long expiresAt) {}

    /**
     * Cached messages page value object
     */
    record CachedMessagesPage(long totalCount, List<MessageEntity> messages) {}
}
