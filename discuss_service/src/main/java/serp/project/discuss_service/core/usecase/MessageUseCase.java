/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message use case
 */

package serp.project.discuss_service.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import serp.project.discuss_service.core.domain.dto.response.MessageResponse;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.domain.event.MessageDeletedInternalEvent;
import serp.project.discuss_service.core.domain.event.MessageSentInternalEvent;
import serp.project.discuss_service.core.domain.event.MessageUpdatedInternalEvent;
import serp.project.discuss_service.core.domain.event.ReactionAddedInternalEvent;
import serp.project.discuss_service.core.domain.event.ReactionRemovedInternalEvent;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.service.IAttachmentService;
import serp.project.discuss_service.core.service.IAttachmentUrlService;
import serp.project.discuss_service.core.service.IChannelMemberService;
import serp.project.discuss_service.core.service.IChannelService;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;
import serp.project.discuss_service.core.service.IMessageService;
import serp.project.discuss_service.core.service.IUserInfoService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

/**
 * Use case for message operations.
 * Orchestrates message-related business logic across multiple services.
 * 
 * Post-commit operations (Kafka events, cache invalidation) are handled via
 * Spring's @TransactionalEventListener to ensure they only execute after
 * successful DB transaction commit.
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
    private final IAttachmentService attachmentService;
    private final IAttachmentUrlService attachmentUrlService;
    private final IUserInfoService userInfoService;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Send a new message to a channel
     */
    @Transactional
    public MessageEntity sendMessage(Long channelId, Long senderId, Long tenantId,
                                     String content, List<Long> mentions) {
        memberService.getMemberWithSendPermission(channelId, senderId);

        ChannelEntity channel = channelService.getChannelByIdOrThrow(channelId);
        if (!channel.isActive()) {
            throw new AppException(ErrorCode.CHANNEL_ARCHIVED);
        }

        MessageEntity message = MessageEntity.createText(channelId, senderId, tenantId, content, mentions);
        MessageEntity saved = messageService.sendMessage(message);

        channelService.recordMessage(channel);

        memberService.incrementUnreadForChannel(channelId, senderId);

        applicationEventPublisher.publishEvent(new MessageSentInternalEvent(this, saved));

        log.info("User {} sent message {} in channel {}", senderId, saved.getId(), channelId);
        return saved;
    }

    /**
     * Send a new message with file attachments to a channel
     */
    @Transactional
    public MessageEntity sendMessageWithAttachments(Long channelId, Long senderId, Long tenantId,
                                                     String content, List<Long> mentions,
                                                     List<MultipartFile> files) {
        memberService.getMemberWithSendPermission(channelId, senderId);

        ChannelEntity channel = channelService.getChannelByIdOrThrow(channelId);
        if (!channel.isActive()) {
            throw new AppException(ErrorCode.CHANNEL_ARCHIVED);
        }

        boolean hasContent = content != null && !content.trim().isEmpty();
        boolean hasFiles = files != null && !files.isEmpty();
        if (!hasContent && !hasFiles) {
            throw new AppException(ErrorCode.MESSAGE_CONTENT_REQUIRED);
        }

        MessageEntity message = MessageEntity.createText(channelId, senderId, tenantId, 
                hasContent ? content : "[Attachment]", mentions);
        MessageEntity saved = messageService.sendMessage(message);

        if (hasFiles) {
            List<AttachmentEntity> attachments = attachmentService.uploadAttachments(
                    files, saved.getId(), channelId, tenantId);
            saved.setAttachments(attachments);
        }

        channelService.recordMessage(channel);

        memberService.incrementUnreadForChannel(channelId, senderId);

        applicationEventPublisher.publishEvent(new MessageSentInternalEvent(this, saved));

        log.info("User {} sent message {} with {} attachments in channel {}", 
                senderId, saved.getId(), hasFiles ? files.size() : 0, channelId);
        return saved;
    }

    /**
     * Send a reply to a message (thread)
     */
    @Transactional
    public MessageEntity sendReply(Long channelId, Long parentId, Long senderId, Long tenantId,
                                   String content, List<Long> mentions) {
        memberService.getMemberWithSendPermission(channelId, senderId);

        ChannelEntity channel = channelService.getChannelByIdOrThrow(channelId);
        if (!channel.isActive()) {
            throw new AppException(ErrorCode.CHANNEL_ARCHIVED);
        }

        MessageEntity parent = messageService.getMessageByIdOrThrow(parentId);
        if (!parent.getChannelId().equals(channelId)) {
            throw new AppException(ErrorCode.PARENT_MESSAGE_NOT_IN_CHANNEL);
        }

        MessageEntity message = MessageEntity.createReply(channelId, senderId, tenantId, content, parentId, mentions);
        MessageEntity saved = messageService.sendReply(parentId, message);

        channelService.recordMessage(channel);

        memberService.incrementUnreadForChannel(channelId, senderId);

        applicationEventPublisher.publishEvent(new MessageSentInternalEvent(this, saved));
        
        return saved;
    }

    /**
     * Get messages in a channel with pagination.
     */
    @Transactional(readOnly = true)
    public Pair<Long, List<MessageEntity>> getChannelMessages(Long channelId, Long userId, 
                                                               int page, int size) {
        if (!memberService.isMember(channelId, userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        Optional<IDiscussCacheService.CachedMessagesPage> cached = 
                cacheService.getCachedChannelMessagesPage(channelId, page, size);
        
        if (cached.isPresent()) {
            List<MessageEntity> messages = cached.get().messages();
            enrichMessagesWithAttachments(messages);
            return Pair.of(cached.get().totalCount(), messages);
        }

        Pair<Long, List<MessageEntity>> result = messageService.getMessagesByChannel(channelId, page, size);
        
        enrichMessagesWithAttachments(result.getSecond());
        
        cacheService.cacheChannelMessagesPage(channelId, page, size, 
                result.getSecond(), result.getFirst());
        
        return result;
    }

    /**
     * Get messages before a specific message (for infinite scroll).
     */
    @Transactional(readOnly = true)
    public List<MessageEntity> getMessagesBefore(Long channelId, Long userId, 
                                                  Long beforeId, int limit) {
        if (!memberService.isMember(channelId, userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        List<MessageEntity> messages = messageService.getMessagesBefore(channelId, beforeId, limit);
        
        enrichMessagesWithAttachments(messages);
        
        return messages;
    }

    /**
     * Get thread replies.
     */
    @Transactional(readOnly = true)
    public List<MessageEntity> getThreadReplies(Long channelId, Long parentId, Long userId) {
        if (!memberService.isMember(channelId, userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        List<MessageEntity> messages = messageService.getThreadReplies(parentId);
        
        enrichMessagesWithAttachments(messages);
        
        return messages;
    }

    /**
     * Search messages in a channel.
     */
    @Transactional(readOnly = true)
    public List<MessageEntity> searchMessages(Long channelId, Long userId, 
                                               String query, int page, int size) {
        if (!memberService.isMember(channelId, userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        List<MessageEntity> messages = messageService.searchMessages(channelId, query, page, size);
        
        enrichMessagesWithAttachments(messages);
        
        return messages;
    }

    /**
     * Get detailed message by ID
     */
    @Transactional(readOnly = true)
    public Optional<MessageResponse> getMessageDetail(Long messageId) {
        Optional<MessageEntity> messageOpt = messageService.getMessageById(messageId);
        return messageOpt.map(m -> {
            MessageResponse response = attachmentUrlService.enrichMessageWithUrls(m);
            response = userInfoService.enrichMessageWithUserInfo(response);
            return response;
        });
    }

    /**
     * Edit a message
     */
    @Transactional
    public MessageEntity editMessage(Long messageId, Long userId, String newContent) {
        MessageEntity message = messageService.getMessageByIdOrThrow(messageId);
        
        if (!memberService.isMember(message.getChannelId(), userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        MessageEntity edited = messageService.editMessage(messageId, newContent, userId);
        
        applicationEventPublisher.publishEvent(new MessageUpdatedInternalEvent(this, edited));
        
        return edited;
    }

    /**
     * Delete a message
     */
    @Transactional
    public MessageEntity deleteMessage(Long messageId, Long userId) {
        MessageEntity message = messageService.getMessageByIdOrThrow(messageId);
        
        boolean isAdmin = memberService.canManageChannel(message.getChannelId(), userId);
        
        MessageEntity deleted = messageService.deleteMessage(messageId, userId, isAdmin);
        
        applicationEventPublisher.publishEvent(new MessageDeletedInternalEvent(this, deleted));
        
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
        
        applicationEventPublisher.publishEvent(
                new ReactionAddedInternalEvent(this, messageId, message.getChannelId(), userId, emoji));
        
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
        
        applicationEventPublisher.publishEvent(
                new ReactionRemovedInternalEvent(this, messageId, message.getChannelId(), userId, emoji));
        
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
            return;
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

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Batch load and attach attachments to messages
     *
     * @param messages List of messages to enrich with attachments
     */
    private void enrichMessagesWithAttachments(List<MessageEntity> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        List<Long> messageIds = messages.stream()
                .map(MessageEntity::getId)
                .toList();

        Map<Long, List<AttachmentEntity>> attachmentMap = 
                attachmentService.getAttachmentsByMessageIds(messageIds);

        messages.forEach(msg -> 
                msg.setAttachments(attachmentMap.getOrDefault(msg.getId(), List.of()))
        );
    }
}
