/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message service interface
 */

package serp.project.discuss_service.core.service;

import org.springframework.data.util.Pair;
import serp.project.discuss_service.core.domain.entity.MessageEntity;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for message business operations.
 * Handles message CRUD and business logic.
 */
public interface IMessageService {

    /**
     * Send a new message
     */
    MessageEntity sendMessage(MessageEntity message);

    /**
     * Send a reply to a message
     */
    MessageEntity sendReply(Long parentId, MessageEntity message);

    /**
     * Get message by ID
     */
    Optional<MessageEntity> getMessageById(Long id);

    /**
     * Get message by ID, throw exception if not found
     */
    MessageEntity getMessageByIdOrThrow(Long id);

    /**
     * Get messages in channel with pagination
     */
    Pair<Long, List<MessageEntity>> getMessagesByChannel(Long channelId, int page, int size);

    /**
     * Get messages before a specific message ID (for infinite scroll)
     */
    List<MessageEntity> getMessagesBefore(Long channelId, Long beforeId, int limit);

    /**
     * Get thread replies
     */
    List<MessageEntity> getThreadReplies(Long parentId);

    /**
     * Search messages in channel
     */
    List<MessageEntity> searchMessages(Long channelId, String query, int page, int size);

    /**
     * Edit message content
     */
    MessageEntity editMessage(Long messageId, String newContent, Long editorId);

    /**
     * Delete message (soft delete)
     */
    MessageEntity deleteMessage(Long messageId, Long deleterId, boolean isAdmin);

    /**
     * Add reaction to message
     */
    MessageEntity addReaction(Long messageId, Long userId, String emoji);

    /**
     * Remove reaction from message
     */
    MessageEntity removeReaction(Long messageId, Long userId, String emoji);

    /**
     * Mark message as read by user
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * Count unread messages after specific message
     */
    long countUnreadMessages(Long channelId, Long afterMessageId);
}
