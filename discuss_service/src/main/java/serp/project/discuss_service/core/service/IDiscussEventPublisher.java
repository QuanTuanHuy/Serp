/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Discuss-specific Kafka event publisher service
 */

package serp.project.discuss_service.core.service;

import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.entity.MessageEntity;

/**
 * Service interface for publishing discuss-specific events to Kafka.
 * Uses IKafkaProducerPort for low-level Kafka operations.
 */
public interface IDiscussEventPublisher {

    // ==================== KAFKA TOPICS ====================

    String TOPIC_MESSAGE_EVENTS = "discuss.message.events";
    String TOPIC_CHANNEL_EVENTS = "discuss.channel.events";
    String TOPIC_MEMBER_EVENTS = "discuss.member.events";
    String TOPIC_REACTION_EVENTS = "discuss.reaction.events";
    String TOPIC_PRESENCE_EVENTS = "discuss.presence.events";

    // ==================== MESSAGE EVENTS ====================

    /**
     * Publish message sent event
     */
    void publishMessageSent(MessageEntity message);

    /**
     * Publish message updated event (edited)
     */
    void publishMessageUpdated(MessageEntity message);

    /**
     * Publish message deleted event
     */
    void publishMessageDeleted(MessageEntity message);

    // ==================== CHANNEL EVENTS ====================

    /**
     * Publish channel created event
     */
    void publishChannelCreated(ChannelEntity channel);

    /**
     * Publish channel updated event
     */
    void publishChannelUpdated(ChannelEntity channel);

    /**
     * Publish channel archived event
     */
    void publishChannelArchived(ChannelEntity channel);

    // ==================== MEMBER EVENTS ====================

    /**
     * Publish member joined event
     */
    void publishMemberJoined(ChannelMemberEntity member);

    /**
     * Publish member left event
     */
    void publishMemberLeft(ChannelMemberEntity member);

    /**
     * Publish member removed event
     */
    void publishMemberRemoved(ChannelMemberEntity member);

    /**
     * Publish member role changed event
     */
    void publishMemberRoleChanged(ChannelMemberEntity member);

    // ==================== REACTION EVENTS ====================

    /**
     * Publish reaction added event
     */
    void publishReactionAdded(Long messageId, Long channelId, Long userId, String emoji);

    /**
     * Publish reaction removed event
     */
    void publishReactionRemoved(Long messageId, Long channelId, Long userId, String emoji);

    // ==================== PRESENCE EVENTS ====================

    /**
     * Publish typing indicator
     */
    void publishTypingIndicator(Long channelId, Long userId, boolean isTyping);

    /**
     * Publish user online status
     */
    void publishUserOnline(Long userId);

    /**
     * Publish user offline status
     */
    void publishUserOffline(Long userId);
}
