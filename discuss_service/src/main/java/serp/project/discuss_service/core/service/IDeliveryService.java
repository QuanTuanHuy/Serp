/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Delivery service contract
 */


package serp.project.discuss_service.core.service;

import java.util.Set;

public interface IDeliveryService {

    void sendToUser(Long userId, Object payload);

    void sendToUsers(Set<Long> userIds, Object payload);

    void fanOutToChannelMembers(Long channelId, Object payload);

    void fanOutToChannelMembersExcept(Long channelId, Long excludeUserId, Object payload);

    void notifyTyping(Long channelId, Long userId, boolean isTyping);

    void notifyNewMessage(Long channelId, Long messageId);

    void notifyMessageUpdated(Long channelId, Long messageId);

    void notifyMessageDeleted(Long channelId, Long messageId);

    void notifyReaction(Long channelId, Long messageId, Long userId, String emoji, boolean added);

    void notifyPresenceChange(Long userId);
}