/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket session registry for tracking connected users
 */

package serp.project.discuss_service.kernel.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for managing WebSocket sessions and subscriptions.
 * Thread-safe implementation for tracking user connections and channel subscriptions.
 */
@Component
@Slf4j
public class WebSocketSessionRegistry {

    // userId -> set of sessionIds
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();
    
    // sessionId -> userId
    private final Map<String, Long> sessionToUser = new ConcurrentHashMap<>();
    
    // channelId -> set of userIds (subscribed to that channel)
    private final Map<Long, Set<Long>> channelSubscribers = new ConcurrentHashMap<>();
    
    // userId -> set of channelIds (channels user is subscribed to)
    private final Map<Long, Set<Long>> userSubscriptions = new ConcurrentHashMap<>();

    /**
     * Register a user session
     */
    public void registerSession(Long userId, String sessionId) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionToUser.put(sessionId, userId);
        log.debug("Registered session {} for user {}", sessionId, userId);
    }

    /**
     * Unregister a user session
     */
    public void unregisterSession(String sessionId) {
        Long userId = sessionToUser.remove(sessionId);
        if (userId != null) {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                    // Clean up all subscriptions for this user
                    removeAllUserSubscriptions(userId);
                }
            }
            log.debug("Unregistered session {} for user {}", sessionId, userId);
        }
    }

    /**
     * Subscribe user to a channel
     */
    public void subscribeToChannel(Long userId, Long channelId) {
        channelSubscribers.computeIfAbsent(channelId, k -> ConcurrentHashMap.newKeySet()).add(userId);
        userSubscriptions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(channelId);
        log.debug("User {} subscribed to channel {}", userId, channelId);
    }

    /**
     * Unsubscribe user from a channel
     */
    public void unsubscribeFromChannel(Long userId, Long channelId) {
        Set<Long> subscribers = channelSubscribers.get(channelId);
        if (subscribers != null) {
            subscribers.remove(userId);
            if (subscribers.isEmpty()) {
                channelSubscribers.remove(channelId);
            }
        }
        
        Set<Long> subscriptions = userSubscriptions.get(userId);
        if (subscriptions != null) {
            subscriptions.remove(channelId);
            if (subscriptions.isEmpty()) {
                userSubscriptions.remove(userId);
            }
        }
        log.debug("User {} unsubscribed from channel {}", userId, channelId);
    }

    /**
     * Get all subscribers for a channel
     */
    public Set<Long> getChannelSubscribers(Long channelId) {
        Set<Long> subscribers = channelSubscribers.get(channelId);
        return subscribers != null ? Set.copyOf(subscribers) : Set.of();
    }

    /**
     * Get all channels a user is subscribed to
     */
    public Set<Long> getUserSubscribedChannels(Long userId) {
        Set<Long> subscriptions = userSubscriptions.get(userId);
        return subscriptions != null ? Set.copyOf(subscriptions) : Set.of();
    }

    /**
     * Check if user is connected (has at least one active session)
     */
    public boolean isUserConnected(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * Get all connected users
     */
    public Set<Long> getConnectedUsers() {
        return Set.copyOf(userSessions.keySet());
    }

    /**
     * Get session IDs for a user
     */
    public Set<String> getUserSessionIds(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null ? Set.copyOf(sessions) : Set.of();
    }

    /**
     * Get online subscribers for a channel (users that are both subscribed and connected)
     */
    public Set<Long> getOnlineChannelSubscribers(Long channelId) {
        Set<Long> subscribers = channelSubscribers.get(channelId);
        if (subscribers == null || subscribers.isEmpty()) {
            return Set.of();
        }
        
        return subscribers.stream()
                .filter(this::isUserConnected)
                .collect(Collectors.toSet());
    }

    /**
     * Get user ID from session ID
     */
    public Long getUserIdBySessionId(String sessionId) {
        return sessionToUser.get(sessionId);
    }

    /**
     * Get total number of connected users
     */
    public int getConnectedUserCount() {
        return userSessions.size();
    }

    /**
     * Get total number of active sessions
     */
    public int getActiveSessionCount() {
        return sessionToUser.size();
    }

    /**
     * Remove all subscriptions for a user
     */
    private void removeAllUserSubscriptions(Long userId) {
        Set<Long> channels = userSubscriptions.remove(userId);
        if (channels != null) {
            for (Long channelId : channels) {
                Set<Long> subscribers = channelSubscribers.get(channelId);
                if (subscribers != null) {
                    subscribers.remove(userId);
                    if (subscribers.isEmpty()) {
                        channelSubscribers.remove(channelId);
                    }
                }
            }
        }
        log.debug("Removed all subscriptions for user {}", userId);
    }
}
