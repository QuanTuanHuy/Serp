/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Discuss cache service implementation
 */

package serp.project.discuss_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.port.client.ICachePort;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.kernel.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of discuss-specific cache service.
 * Uses ICachePort for low-level Redis operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscussCacheService implements IDiscussCacheService {

    private final ICachePort cachePort;
    private final JsonUtils jsonUtils;

    // ==================== CHANNEL CACHE ====================

    @Override
    public void cacheChannel(ChannelEntity channel) {
        if (channel == null || channel.getId() == null) {
            return;
        }
        String key = CHANNEL_PREFIX + channel.getId();
        cachePort.setToCache(key, channel, CHANNEL_TTL);
        log.debug("Cached channel: {}", channel.getId());
    }

    @Override
    public Optional<ChannelEntity> getCachedChannel(Long channelId) {
        if (channelId == null) {
            return Optional.empty();
        }
        String key = CHANNEL_PREFIX + channelId;
        ChannelEntity channel = cachePort.getFromCache(key, ChannelEntity.class);
        return Optional.ofNullable(channel);
    }

    @Override
    public void invalidateChannel(Long channelId) {
        if (channelId == null) {
            return;
        }
        String key = CHANNEL_PREFIX + channelId;
        cachePort.deleteFromCache(key);
        log.debug("Invalidated channel cache: {}", channelId);
    }

    // ==================== MESSAGE CACHE ====================

    @Override
    public void cacheMessage(MessageEntity message) {
        if (message == null || message.getId() == null) {
            return;
        }
        String key = MESSAGE_PREFIX + message.getId();
        cachePort.setToCache(key, message, MESSAGE_TTL);
        log.debug("Cached message: {}", message.getId());
    }

    @Override
    public Optional<MessageEntity> getCachedMessage(Long messageId) {
        if (messageId == null) {
            return Optional.empty();
        }
        String key = MESSAGE_PREFIX + messageId;
        MessageEntity message = cachePort.getFromCache(key, MessageEntity.class);
        return Optional.ofNullable(message);
    }

    @Override
    public void cacheRecentMessages(Long channelId, List<MessageEntity> messages) {
        if (channelId == null || messages == null) {
            return;
        }
        String key = RECENT_MESSAGES_PREFIX + channelId;
        cachePort.setToCache(key, messages, RECENT_MESSAGES_TTL);
        log.debug("Cached {} recent messages for channel: {}", messages.size(), channelId);
    }

    @Override
    public List<MessageEntity> getCachedRecentMessages(Long channelId) {
        if (channelId == null) {
            return Collections.emptyList();
        }
        String key = RECENT_MESSAGES_PREFIX + channelId;
        String json = cachePort.getFromCache(key);
        if (json == null) {
            return Collections.emptyList();
        }
        try {
            return jsonUtils.fromJson(json, 
                    new ParameterizedTypeReference<List<MessageEntity>>() {});
        } catch (Exception e) {
            log.error("Failed to parse recent messages from cache", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void addToRecentMessages(Long channelId, MessageEntity message) {
        if (channelId == null || message == null) {
            return;
        }
        String key = RECENT_MESSAGES_PREFIX + channelId;
        String messageJson = jsonUtils.toJson(message);
        cachePort.leftPush(key, messageJson);
        cachePort.trimList(key, 0, 49);
        cachePort.expire(key, RECENT_MESSAGES_TTL);
        log.debug("Added message to recent messages cache for channel: {}", channelId);
    }

    @Override
    public void invalidateMessage(Long messageId) {
        if (messageId == null) {
            return;
        }
        String key = MESSAGE_PREFIX + messageId;
        cachePort.deleteFromCache(key);
        log.debug("Invalidated message cache: {}", messageId);
    }

    @Override
    public void invalidateChannelMessages(Long channelId) {
        if (channelId == null) {
            return;
        }
        String recentKey = RECENT_MESSAGES_PREFIX + channelId;
        cachePort.deleteFromCache(recentKey);
        
        invalidateChannelMessagesPage(channelId);
        
        log.debug("Invalidated channel messages cache: {}", channelId);
    }

    // ==================== PRESENCE / ONLINE STATUS ====================

    @Override
    public void setUserOnline(Long userId) {
        if (userId == null) {
            return;
        }
        String key = PRESENCE_PREFIX + userId;
        cachePort.setToCache(key, System.currentTimeMillis(), PRESENCE_TTL);
        log.debug("Set user online: {}", userId);
    }

    @Override
    public void setUserOffline(Long userId) {
        if (userId == null) {
            return;
        }
        String key = PRESENCE_PREFIX + userId;
        cachePort.deleteFromCache(key);
        log.debug("Set user offline: {}", userId);
    }

    @Override
    public void addUserToChannel(Long channelId, Long userId) {
        if (channelId == null || userId == null) {
            return;
        }
        String key = CHANNEL_ONLINE_PREFIX + channelId;
        cachePort.addToSet(key, userId.toString());
        log.debug("Added user {} to channel {} online set", userId, channelId);
    }

    @Override
    public void removeUserFromChannel(Long channelId, Long userId) {
        if (channelId == null || userId == null) {
            return;
        }
        String key = CHANNEL_ONLINE_PREFIX + channelId;
        cachePort.removeFromSet(key, userId.toString());
        log.debug("Removed user {} from channel {} online set", userId, channelId);
    }

    @Override
    public Set<Long> getOnlineUsersInChannel(Long channelId) {
        if (channelId == null) {
            return Collections.emptySet();
        }
        String key = CHANNEL_ONLINE_PREFIX + channelId;
        Set<String> members = cachePort.getSetMembers(key);
        return members.stream()
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isUserOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        String key = PRESENCE_PREFIX + userId;
        return cachePort.exists(key);
    }

    @Override
    public void refreshPresence(Long userId) {
        if (userId == null) {
            return;
        }
        String key = PRESENCE_PREFIX + userId;
        cachePort.expire(key, PRESENCE_TTL);
        log.debug("Refreshed presence for user: {}", userId);
    }

    // ==================== TYPING INDICATORS ====================

    @Override
    public void setUserTyping(Long channelId, Long userId) {
        if (channelId == null || userId == null) {
            return;
        }
        String key = TYPING_PREFIX + channelId + ":" + userId;
        cachePort.setToCache(key, System.currentTimeMillis(), TYPING_TTL);
        log.debug("Set user {} typing in channel {}", userId, channelId);
    }

    @Override
    public void clearUserTyping(Long channelId, Long userId) {
        if (channelId == null || userId == null) {
            return;
        }
        String key = TYPING_PREFIX + channelId + ":" + userId;
        cachePort.deleteFromCache(key);
        log.debug("Cleared typing for user {} in channel {}", userId, channelId);
    }

    @Override
    public Set<Long> getTypingUsers(Long channelId) {
        if (channelId == null) {
            return Collections.emptySet();
        }
        String pattern = TYPING_PREFIX + channelId + ":*";
        try {
            Set<String> typingKeys = cachePort.scanKeys(pattern);
            return typingKeys.stream()
                    .map(key -> {
                        String[] parts = key.split(":");
                        return Long.valueOf(parts[parts.length - 1]);
                    })
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get typing users for channel: {}", channelId, e);
            return Collections.emptySet();
        }
    }

    // ==================== UNREAD COUNTS ====================

    @Override
    public void cacheUnreadCount(Long userId, Long channelId, int count) {
        if (userId == null || channelId == null) {
            return;
        }
        String key = UNREAD_PREFIX + userId;
        cachePort.hashSet(key, channelId.toString(), String.valueOf(count));
        log.debug("Cached unread count {} for user {} in channel {}", count, userId, channelId);
    }

    @Override
    public void incrementUnreadCount(Long userId, Long channelId) {
        if (userId == null || channelId == null) {
            return;
        }
        String key = UNREAD_PREFIX + userId;
        cachePort.hashIncrement(key, channelId.toString(), 1);
        log.debug("Incremented unread count for user {} in channel {}", userId, channelId);
    }

    @Override
    public void incrementUnreadCountBatch(Set<Long> userIds, Long channelId) {
        if (userIds == null || userIds.isEmpty() || channelId == null) {
            return;
        }
        
        Map<String, Map<String, Long>> operations = new HashMap<>();
        String channelIdStr = channelId.toString();
        
        for (Long userId : userIds) {
            String key = UNREAD_PREFIX + userId;
            operations.put(key, Map.of(channelIdStr, 1L));
        }
        
        cachePort.batchHashIncrement(operations);
        log.debug("Batch incremented unread count for {} users in channel {}", userIds.size(), channelId);
    }

    @Override
    public void resetUnreadCount(Long userId, Long channelId) {
        if (userId == null || channelId == null) {
            return;
        }
        String key = UNREAD_PREFIX + userId;
        cachePort.hashSet(key, channelId.toString(), "0");
        log.debug("Reset unread count for user {} in channel {}", userId, channelId);
    }

    @Override
    public Optional<Integer> getCachedUnreadCount(Long userId, Long channelId) {
        if (userId == null || channelId == null) {
            return Optional.empty();
        }
        String key = UNREAD_PREFIX + userId;
        String value = cachePort.hashGet(key, channelId.toString());
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public long getTotalUnreadCount(Long userId) {
        if (userId == null) {
            return 0L;
        }
        String key = UNREAD_PREFIX + userId;
        Map<String, String> entries = cachePort.hashGetAll(key);
        return entries.values().stream()
                .mapToLong(v -> {
                    try {
                        return Long.parseLong(v);
                    } catch (NumberFormatException e) {
                        return 0L;
                    }
                })
                .sum();
    }

    // ==================== SESSION MANAGEMENT ====================

    @Override
    public void storeSession(String sessionId, Long userId, String instanceId) {
        if (sessionId == null || userId == null) {
            return;
        }
        String sessionKey = SESSION_PREFIX + sessionId;
        SessionInfo sessionInfo = new SessionInfo(sessionId, userId, instanceId, System.currentTimeMillis());
        cachePort.setToCache(sessionKey, sessionInfo, SESSION_TTL);

        // Add session to user's session set
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        cachePort.addToSet(userSessionsKey, sessionId);
        cachePort.expire(userSessionsKey, SESSION_TTL);

        log.debug("Stored session {} for user {}", sessionId, userId);
    }

    @Override
    public Optional<SessionInfo> getSession(String sessionId) {
        if (sessionId == null) {
            return Optional.empty();
        }
        String key = SESSION_PREFIX + sessionId;
        SessionInfo sessionInfo = cachePort.getFromCache(key, SessionInfo.class);
        return Optional.ofNullable(sessionInfo);
    }

    @Override
    public Set<String> getUserSessions(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        String key = USER_SESSIONS_PREFIX + userId;
        return cachePort.getSetMembers(key);
    }

    @Override
    public void removeSession(String sessionId, Long userId) {
        if (sessionId == null) {
            return;
        }
        // Remove session data
        String sessionKey = SESSION_PREFIX + sessionId;
        cachePort.deleteFromCache(sessionKey);

        // Remove from user's session set
        if (userId != null) {
            String userSessionsKey = USER_SESSIONS_PREFIX + userId;
            cachePort.removeFromSet(userSessionsKey, sessionId);
        }

        log.debug("Removed session {} for user {}", sessionId, userId);
    }

    @Override
    public int getActiveSessionCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        String key = USER_SESSIONS_PREFIX + userId;
        return (int) cachePort.getSetSize(key);
    }

    // ==================== CHANNEL MEMBERS CACHE ====================

    @Override
    public void cacheChannelMembers(Long channelId, Set<Long> memberIds) {
        if (channelId == null || memberIds == null) {
            return;
        }
        String key = CHANNEL_MEMBERS_PREFIX + channelId;
        String[] members = memberIds.stream()
                .map(String::valueOf)
                .toArray(String[]::new);
        // Clear existing and add new
        cachePort.deleteFromCache(key);
        if (members.length > 0) {
            cachePort.addToSet(key, members);
        }
        cachePort.expire(key, CHANNEL_TTL);
        log.debug("Cached {} members for channel {}", memberIds.size(), channelId);
    }

    @Override
    public Set<Long> getCachedChannelMembers(Long channelId) {
        if (channelId == null) {
            return Collections.emptySet();
        }
        String key = CHANNEL_MEMBERS_PREFIX + channelId;
        Set<String> members = cachePort.getSetMembers(key);
        return members.stream()
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    @Override
    public void addMemberToChannelCache(Long channelId, Long userId) {
        if (channelId == null || userId == null) {
            return;
        }
        String key = CHANNEL_MEMBERS_PREFIX + channelId;
        cachePort.addToSet(key, userId.toString());
        log.debug("Added member {} to channel {} cache", userId, channelId);
    }

    @Override
    public void removeMemberFromChannelCache(Long channelId, Long userId) {
        if (channelId == null || userId == null) {
            return;
        }
        String key = CHANNEL_MEMBERS_PREFIX + channelId;
        cachePort.removeFromSet(key, userId.toString());
        log.debug("Removed member {} from channel {} cache", userId, channelId);
    }

    @Override
    public boolean isMemberCached(Long channelId, Long userId) {
        if (channelId == null || userId == null) {
            return false;
        }
        String key = CHANNEL_MEMBERS_PREFIX + channelId;
        return cachePort.isSetMember(key, userId.toString());
    }

    // ==================== USER CHANNELS CACHE ====================

    @Override
    public void cacheUserChannels(Long userId, Set<Long> channelIds) {
        if (userId == null || channelIds == null) {
            return;
        }
        String key = USER_CHANNELS_PREFIX + userId;
        String[] channels = channelIds.stream()
                .map(String::valueOf)
                .toArray(String[]::new);
        // Clear existing and add new
        cachePort.deleteFromCache(key);
        if (channels.length > 0) {
            cachePort.addToSet(key, channels);
        }
        cachePort.expire(key, CHANNEL_TTL);
        log.debug("Cached {} channels for user {}", channelIds.size(), userId);
    }

    @Override
    public Set<Long> getCachedUserChannels(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        String key = USER_CHANNELS_PREFIX + userId;
        Set<String> channels = cachePort.getSetMembers(key);
        return channels.stream()
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    @Override
    public void addChannelToUserCache(Long userId, Long channelId) {
        if (userId == null || channelId == null) {
            return;
        }
        String key = USER_CHANNELS_PREFIX + userId;
        cachePort.addToSet(key, channelId.toString());
        log.debug("Added channel {} to user {} cache", channelId, userId);
    }

    @Override
    public void removeChannelFromUserCache(Long userId, Long channelId) {
        if (userId == null || channelId == null) {
            return;
        }
        String key = USER_CHANNELS_PREFIX + userId;
        cachePort.removeFromSet(key, channelId.toString());
        log.debug("Removed channel {} from user {} cache", channelId, userId);
    }

    // ==================== ATTACHMENT URL CACHE ====================

    @Override
    public void cacheAttachmentUrl(Long attachmentId, CachedAttachmentUrl urlInfo) {
        if (attachmentId == null || urlInfo == null) {
            return;
        }
        String key = ATTACHMENT_URL_PREFIX + attachmentId;
        cachePort.setToCache(key, urlInfo, ATTACHMENT_URL_TTL);
        log.debug("Cached attachment URL for: {}", attachmentId);
    }

    @Override
    public Optional<CachedAttachmentUrl> getCachedAttachmentUrl(Long attachmentId) {
        if (attachmentId == null) {
            return Optional.empty();
        }
        String key = ATTACHMENT_URL_PREFIX + attachmentId;
        CachedAttachmentUrl urlInfo = cachePort.getFromCache(key, CachedAttachmentUrl.class);
        return Optional.ofNullable(urlInfo);
    }

    @Override
    public void invalidateAttachmentUrl(Long attachmentId) {
        if (attachmentId == null) {
            return;
        }
        String key = ATTACHMENT_URL_PREFIX + attachmentId;
        cachePort.deleteFromCache(key);
        log.debug("Invalidated attachment URL cache: {}", attachmentId);
    }

    // ==================== CHANNEL MESSAGES PAGE CACHE ====================

    @Override
    public void cacheChannelMessagesPage(Long channelId, int page, int size,
                                          List<MessageEntity> messages, long totalCount) {
        if (channelId == null || page != 0 || messages == null) {
            return;
        }
        String key = CHANNEL_MESSAGES_PREFIX + channelId + ":p" + page + ":s" + size;
        CachedMessagesPage cached = new CachedMessagesPage(totalCount, messages);
        cachePort.setToCache(key, cached, CHANNEL_MESSAGES_TTL);
        log.debug("Cached {} messages for channel {} page {} size {}", messages.size(), channelId, page, size);
    }

    @Override
    public Optional<CachedMessagesPage> getCachedChannelMessagesPage(Long channelId, int page, int size) {
        if (channelId == null || page != 0) {
            return Optional.empty();
        }
        String key = CHANNEL_MESSAGES_PREFIX + channelId + ":p" + page + ":s" + size;
        CachedMessagesPage cached = cachePort.getFromCache(key, CachedMessagesPage.class);
        if (cached != null) {
            log.debug("Cache hit for channel {} messages page {} size {}", channelId, page, size);
        }
        return Optional.ofNullable(cached);
    }

    @Override
    public void invalidateChannelMessagesPage(Long channelId) {
        if (channelId == null) {
            return;
        }
        String pattern = CHANNEL_MESSAGES_PREFIX + channelId + ":*";
        cachePort.deleteAllByPattern(pattern);
        log.debug("Invalidated channel messages page cache: {}", channelId);
    }

    @Override
    public void invalidateChannelMessagesPageAsync(Long channelId) {
        if (channelId == null) {
            return;
        }
        String pattern = CHANNEL_MESSAGES_PREFIX + channelId + ":*";
        cachePort.scanAndDelete(pattern, 100);
        log.debug("Async invalidated channel messages page cache: {}", channelId);
    }

    @Override
    public boolean prependMessageToFirstPage(Long channelId, MessageEntity message, int pageSize) {
        if (channelId == null || message == null || pageSize <= 0) {
            return false;
        }

        try {
            String key = CHANNEL_MESSAGES_PREFIX + channelId + ":p0:s" + pageSize;
            CachedMessagesPage cached = cachePort.getFromCache(key, CachedMessagesPage.class);

            if (cached == null) {
                log.debug("No cache to prepend for channel {}, skipping smart update", channelId);
                return false;
            }

            List<MessageEntity> updatedMessages = new ArrayList<>();
            updatedMessages.add(message);
            
            List<MessageEntity> existingMessages = cached.messages();
            if (existingMessages != null) {
                int limit = Math.min(existingMessages.size(), pageSize - 1);
                for (int i = 0; i < limit; i++) {
                    updatedMessages.add(existingMessages.get(i));
                }
            }

            CachedMessagesPage updated = new CachedMessagesPage(
                    cached.totalCount() + 1,
                    updatedMessages
            );

            cachePort.setToCache(key, updated, CHANNEL_MESSAGES_TTL);
            log.debug("Smart cache update: prepended message {} to channel {} first page", 
                    message.getId(), channelId);
            return true;

        } catch (Exception e) {
            log.warn("Failed to prepend message to cache for channel {}, falling back to invalidation: {}", 
                    channelId, e.getMessage());
            invalidateChannelMessagesPageAsync(channelId);
            return false;
        }
    }

    @Override
    public boolean removeMessageFromFirstPage(Long channelId, Long messageId) {
        if (channelId == null || messageId == null) {
            return false;
        }

        try {
            int[] commonPageSizes = {20, 25, 50};
            
            for (int pageSize : commonPageSizes) {
                String key = CHANNEL_MESSAGES_PREFIX + channelId + ":p0:s" + pageSize;
                CachedMessagesPage cached = cachePort.getFromCache(key, CachedMessagesPage.class);

                if (cached == null || cached.messages() == null || cached.messages().isEmpty()) {
                    continue;
                }

                List<MessageEntity> existingMessages = cached.messages();
                List<MessageEntity> updatedMessages = existingMessages.stream()
                        .filter(m -> !messageId.equals(m.getId()))
                        .toList();

                if (updatedMessages.size() < existingMessages.size()) {
                    CachedMessagesPage updated = new CachedMessagesPage(
                            Math.max(0, cached.totalCount() - 1),
                            updatedMessages
                    );
                    cachePort.setToCache(key, updated, CHANNEL_MESSAGES_TTL);
                    log.debug("Smart cache update: removed message {} from channel {} first page (size {})", 
                            messageId, channelId, pageSize);
                    return true;
                }
            }

            log.debug("Message {} not in cached first pages for channel {}, invalidating", messageId, channelId);
            invalidateChannelMessagesPageAsync(channelId);
            return false;

        } catch (Exception e) {
            log.warn("Failed to remove message from cache for channel {}, falling back to invalidation: {}", 
                    channelId, e.getMessage());
            invalidateChannelMessagesPageAsync(channelId);
            return false;
        }
    }
}
