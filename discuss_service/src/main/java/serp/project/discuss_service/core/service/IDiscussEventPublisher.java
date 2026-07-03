/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Discuss-specific Kafka event publisher service
 */

package serp.project.discuss_service.core.service;

import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.entity.MessageEntity;

import java.util.List;

public interface IDiscussEventPublisher {

    // ==================== KAFKA TOPICS ====================

    String TOPIC_MESSAGE_EVENTS = "discuss.message.events";
    String TOPIC_CHANNEL_EVENTS = "discuss.channel.events";
    String TOPIC_MEMBER_EVENTS = "discuss.member.events";
    String TOPIC_REACTION_EVENTS = "discuss.reaction.events";
    String TOPIC_PRESENCE_EVENTS = "discuss.presence.events";

    void publishRealtimeEvent(String key, WsEvent<?> event, String topic);

    // ==================== MESSAGE EVENTS ====================

    void publishMessageSent(MessageEntity message);

    void publishMessageUpdated(MessageEntity message);

    void publishMessageDeleted(MessageEntity message);

    void publishMessageRead(Long channelId, Long messageId, Long userId, List<Long> readBy, Integer readCount);

    // ==================== CHANNEL EVENTS ====================

    void publishChannelCreated(ChannelEntity channel);

    void publishChannelUpdated(ChannelEntity channel);

    void publishChannelArchived(ChannelEntity channel);

    // ==================== MEMBER EVENTS ====================

    void publishMemberJoined(ChannelMemberEntity member);

    void publishMemberLeft(ChannelMemberEntity member);

    void publishMemberRemoved(ChannelMemberEntity member);

    void publishMemberRoleChanged(ChannelMemberEntity member);

    // ==================== REACTION EVENTS ====================

    void publishReactionAdded(Long messageId, Long channelId, Long userId, String emoji);

    void publishReactionRemoved(Long messageId, Long channelId, Long userId, String emoji);

    // ==================== PRESENCE EVENTS ====================

    void publishUserOnline(Long userId);

    void publishUserOffline(Long userId);
}
