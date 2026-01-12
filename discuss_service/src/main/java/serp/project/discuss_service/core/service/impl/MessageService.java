/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message service implementation
 */

package serp.project.discuss_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.port.store.IMessagePort;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.core.service.IMessageService;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of message service.
 * Handles message business operations with caching.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService implements IMessageService {

    private final IMessagePort messagePort;
    private final IDiscussCacheService cacheService;

    @Override
    public MessageEntity sendMessage(MessageEntity message) {
        message.validateForCreation();
        MessageEntity saved = messagePort.save(message);
        
        // Update caches
        cacheService.cacheMessage(saved);
        cacheService.addToRecentMessages(saved.getChannelId(), saved);
        
        log.info("Sent message: {} in channel: {}", saved.getId(), saved.getChannelId());
        return saved;
    }

    @Override
    public MessageEntity sendReply(Long parentId, MessageEntity message) {
        // Get parent message and increment thread count
        MessageEntity parent = getMessageByIdOrThrow(parentId);
        parent.incrementThreadCount();
        messagePort.save(parent);
        
        // Set parent ID and save reply
        message.setParentId(parentId);
        message.validateForCreation();
        MessageEntity saved = messagePort.save(message);
        
        cacheService.cacheMessage(saved);
        cacheService.invalidateMessage(parentId); // Invalidate parent cache due to thread count change
        
        log.info("Sent reply: {} to message: {}", saved.getId(), parentId);
        return saved;
    }

    @Override
    public Optional<MessageEntity> getMessageById(Long id) {
        // Try cache first
        Optional<MessageEntity> cached = cacheService.getCachedMessage(id);
        if (cached.isPresent()) {
            log.debug("Cache hit for message: {}", id);
            return cached;
        }

        // Fallback to database
        Optional<MessageEntity> message = messagePort.findById(id);
        message.ifPresent(cacheService::cacheMessage);
        return message;
    }

    @Override
    public MessageEntity getMessageByIdOrThrow(Long id) {
        return getMessageById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
    }

    @Override
    public Pair<Long, List<MessageEntity>> getMessagesByChannel(Long channelId, int page, int size) {
        return messagePort.findByChannelId(channelId, page, size);
    }

    @Override
    public List<MessageEntity> getMessagesBefore(Long channelId, Long beforeId, int limit) {
        return messagePort.findBeforeId(channelId, beforeId, limit);
    }

    @Override
    public List<MessageEntity> getThreadReplies(Long parentId) {
        return messagePort.findReplies(parentId);
    }

    @Override
    public List<MessageEntity> searchMessages(Long channelId, String query, int page, int size) {
        return messagePort.searchMessages(channelId, query, page, size);
    }

    @Override
    public MessageEntity editMessage(Long messageId, String newContent, Long editorId) {
        MessageEntity message = getMessageByIdOrThrow(messageId);
        message.edit(newContent, editorId);
        MessageEntity saved = messagePort.save(message);
        
        cacheService.cacheMessage(saved);
        cacheService.invalidateChannelMessages(saved.getChannelId());
        
        log.info("Edited message: {}", messageId);
        return saved;
    }

    @Override
    public MessageEntity deleteMessage(Long messageId, Long deleterId, boolean isAdmin) {
        MessageEntity message = getMessageByIdOrThrow(messageId);
        message.delete(deleterId, isAdmin);
        MessageEntity saved = messagePort.save(message);
        
        // Update parent thread count if this was a reply
        if (message.isReply()) {
            getMessageById(message.getParentId()).ifPresent(parent -> {
                parent.decrementThreadCount();
                messagePort.save(parent);
                cacheService.invalidateMessage(parent.getId());
            });
        }
        
        cacheService.invalidateMessage(messageId);
        cacheService.invalidateChannelMessages(saved.getChannelId());
        
        log.info("Deleted message: {}", messageId);
        return saved;
    }

    @Override
    public MessageEntity addReaction(Long messageId, Long userId, String emoji) {
        MessageEntity message = getMessageByIdOrThrow(messageId);
        message.addReaction(emoji, userId);
        MessageEntity saved = messagePort.save(message);
        
        cacheService.cacheMessage(saved);
        log.debug("Added reaction {} to message {} by user {}", emoji, messageId, userId);
        return saved;
    }

    @Override
    public MessageEntity removeReaction(Long messageId, Long userId, String emoji) {
        MessageEntity message = getMessageByIdOrThrow(messageId);
        message.removeReaction(emoji, userId);
        MessageEntity saved = messagePort.save(message);
        
        cacheService.cacheMessage(saved);
        log.debug("Removed reaction {} from message {} by user {}", emoji, messageId, userId);
        return saved;
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        MessageEntity message = getMessageByIdOrThrow(messageId);
        message.markReadBy(userId);
        messagePort.save(message);
        log.debug("Message {} marked as read by user {}", messageId, userId);
    }

    @Override
    public long countUnreadMessages(Long channelId, Long afterMessageId) {
        return messagePort.countUnreadMessages(channelId, afterMessageId);
    }
}
