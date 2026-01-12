/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message use case
 */

package serp.project.discuss_service.core.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.service.IChannelMemberService;
import serp.project.discuss_service.core.service.IChannelService;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;
import serp.project.discuss_service.core.service.IMessageService;

import java.util.List;
import java.util.Set;

/**
 * Use case for message operations.
 * Orchestrates message-related business logic across multiple services.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageUseCase {

    private final IMessageService messageService;
    private final IChannelService channelService;
    private final IChannelMemberService memberService;
    private final IDiscussEventPublisher eventPublisher;
    private final IDiscussCacheService cacheService;

    /**
     * Send a new message to a channel
     */
    @Transactional
    public MessageEntity sendMessage(Long channelId, Long senderId, Long tenantId,
                                     String content, List<Long> mentions) {
        // Validate sender is member and can send messages
        if (!memberService.canSendMessages(channelId, senderId)) {
            throw new AppException(ErrorCode.CANNOT_SEND_MESSAGES);
        }

        // Validate channel is active
        ChannelEntity channel = channelService.getChannelByIdOrThrow(channelId);
        if (!channel.isActive()) {
            throw new AppException(ErrorCode.CHANNEL_ARCHIVED);
        }

        // Create and send message
        MessageEntity message = MessageEntity.createText(channelId, senderId, tenantId, content, mentions);
        MessageEntity saved = messageService.sendMessage(message);

        // Update channel stats
        channelService.recordMessage(channelId);

        // Increment unread counts for other members
        memberService.incrementUnreadForChannel(channelId, senderId);

        // Publish event for real-time delivery
        eventPublisher.publishMessageSent(saved);

        log.info("User {} sent message {} in channel {}", senderId, saved.getId(), channelId);
        return saved;
    }

    /**
     * Send a reply to a message (thread)
     */
    @Transactional
    public MessageEntity sendReply(Long channelId, Long parentId, Long senderId, Long tenantId,
                                   String content, List<Long> mentions) {
        if (!memberService.canSendMessages(channelId, senderId)) {
            throw new AppException(ErrorCode.CANNOT_SEND_MESSAGES);
        }

        // Validate parent message exists and is in same channel
        MessageEntity parent = messageService.getMessageByIdOrThrow(parentId);
        if (!parent.getChannelId().equals(channelId)) {
            throw new AppException(ErrorCode.PARENT_MESSAGE_NOT_IN_CHANNEL);
        }

        // Create reply
        MessageEntity message = MessageEntity.createReply(channelId, senderId, tenantId, content, parentId, mentions);
        MessageEntity saved = messageService.sendReply(parentId, message);

        // Update channel stats
        channelService.recordMessage(channelId);

        // Increment unread for thread participants
        memberService.incrementUnreadForChannel(channelId, senderId);

        eventPublisher.publishMessageSent(saved);
        return saved;
    }

    /**
     * Get messages in a channel with pagination
     */
    public Pair<Long, List<MessageEntity>> getChannelMessages(Long channelId, Long userId, 
                                                               int page, int size) {
        // Validate user is member
        if (!memberService.isMember(channelId, userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        return messageService.getMessagesByChannel(channelId, page, size);
    }

    /**
     * Get messages before a specific message (for infinite scroll)
     */
    public List<MessageEntity> getMessagesBefore(Long channelId, Long userId, 
                                                  Long beforeId, int limit) {
        if (!memberService.isMember(channelId, userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        return messageService.getMessagesBefore(channelId, beforeId, limit);
    }

    /**
     * Get thread replies
     */
    public List<MessageEntity> getThreadReplies(Long channelId, Long parentId, Long userId) {
        if (!memberService.isMember(channelId, userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        return messageService.getThreadReplies(parentId);
    }

    /**
     * Search messages in a channel
     */
    public List<MessageEntity> searchMessages(Long channelId, Long userId, 
                                               String query, int page, int size) {
        if (!memberService.isMember(channelId, userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        return messageService.searchMessages(channelId, query, page, size);
    }

    /**
     * Edit a message
     */
    @Transactional
    public MessageEntity editMessage(Long messageId, Long userId, String newContent) {
        MessageEntity message = messageService.getMessageByIdOrThrow(messageId);
        
        // Validate channel membership
        if (!memberService.isMember(message.getChannelId(), userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        MessageEntity edited = messageService.editMessage(messageId, newContent, userId);
        eventPublisher.publishMessageUpdated(edited);
        return edited;
    }

    /**
     * Delete a message
     */
    @Transactional
    public MessageEntity deleteMessage(Long messageId, Long userId) {
        MessageEntity message = messageService.getMessageByIdOrThrow(messageId);
        
        // Check if user is admin or owner
        boolean isAdmin = memberService.canManageChannel(message.getChannelId(), userId);
        
        MessageEntity deleted = messageService.deleteMessage(messageId, userId, isAdmin);
        eventPublisher.publishMessageDeleted(deleted);
        return deleted;
    }

    /**
     * Add reaction to a message
     */
    @Transactional
    public MessageEntity addReaction(Long messageId, Long userId, String emoji) {
        MessageEntity message = messageService.getMessageByIdOrThrow(messageId);
        
        if (!memberService.isMember(message.getChannelId(), userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        MessageEntity updated = messageService.addReaction(messageId, userId, emoji);
        eventPublisher.publishReactionAdded(messageId, message.getChannelId(), userId, emoji);
        return updated;
    }

    /**
     * Remove reaction from a message
     */
    @Transactional
    public MessageEntity removeReaction(Long messageId, Long userId, String emoji) {
        MessageEntity message = messageService.getMessageByIdOrThrow(messageId);
        
        if (!memberService.isMember(message.getChannelId(), userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        MessageEntity updated = messageService.removeReaction(messageId, userId, emoji);
        eventPublisher.publishReactionRemoved(messageId, message.getChannelId(), userId, emoji);
        return updated;
    }

    /**
     * Mark messages as read in a channel
     */
    @Transactional
    public void markAsRead(Long channelId, Long userId, Long messageId) {
        if (!memberService.isMember(channelId, userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        memberService.markAsRead(channelId, userId, messageId);
        messageService.markAsRead(messageId, userId);
        
        log.debug("User {} marked messages as read in channel {} up to {}", userId, channelId, messageId);
    }

    /**
     * Send typing indicator
     */
    public void sendTypingIndicator(Long channelId, Long userId, boolean isTyping) {
        if (!memberService.isMember(channelId, userId)) {
            return; // Silently ignore if not a member
        }

        if (isTyping) {
            cacheService.setUserTyping(channelId, userId);
        } else {
            cacheService.clearUserTyping(channelId, userId);
        }

        eventPublisher.publishTypingIndicator(channelId, userId, isTyping);
    }

    /**
     * Get users currently typing in a channel
     */
    public Set<Long> getTypingUsers(Long channelId, Long userId) {
        if (!memberService.isMember(channelId, userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        return cacheService.getTypingUsers(channelId);
    }

    /**
     * Get unread message count for user in channel
     */
    public long getUnreadCount(Long channelId, Long userId) {
        ChannelMemberEntity member = memberService.getMemberOrThrow(channelId, userId);
        
        if (member.getLastReadMsgId() == null) {
            return messageService.countUnreadMessages(channelId, 0L);
        }
        
        return messageService.countUnreadMessages(channelId, member.getLastReadMsgId());
    }
}
