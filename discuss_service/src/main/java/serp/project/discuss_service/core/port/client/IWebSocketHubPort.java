/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket hub port for real-time messaging
 */

package serp.project.discuss_service.core.port.client;

import serp.project.discuss_service.core.domain.entity.MessageEntity;

import java.util.Set;

/**
 * Port for WebSocket hub operations (real-time message delivery)
 */
public interface IWebSocketHubPort {

    /**
     * Send message to all users in a channel
     */
    void broadcastToChannel(Long channelId, Object payload);

    /**
     * Send message to a specific user
     */
    void sendToUser(Long userId, Object payload);

    /**
     * Send error message to a specific user
     */
    void sendErrorToUser(Long userId, Object payload);

    /**
     * Send message to multiple users
     */
    void sendToUsers(Set<Long> userIds, Object payload);

    /**
     * Subscribe user to a channel
     */
    void subscribeUserToChannel(Long userId, Long channelId);

    /**
     * Unsubscribe user from a channel
     */
    void unsubscribeUserFromChannel(Long userId, Long channelId);

    /**
     * Get subscribers of a channel (online users)
     */
    Set<Long> getChannelSubscribers(Long channelId);

    /**
     * Notify channel about typing status
     */
    void notifyTyping(Long channelId, Long userId, boolean isTyping);

    /**
     * Notify channel about new message
     */
    void notifyNewMessage(Long channelId, MessageEntity message);

    /**
     * Notify channel about message update
     */
    void notifyMessageUpdated(Long channelId, MessageEntity message);

    /**
     * Notify channel about message deletion
     */
    void notifyMessageDeleted(Long channelId, Long messageId);

    /**
     * Notify channel about reaction
     */
    void notifyReaction(Long channelId, Long messageId, Long userId, String emoji, boolean added);

    /**
     * Notify about presence change
     */
    void notifyPresenceChange(Long channelId, Long userId, boolean online);
}
