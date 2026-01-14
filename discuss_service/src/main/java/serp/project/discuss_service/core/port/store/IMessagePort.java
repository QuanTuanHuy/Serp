/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message port interface
 */

package serp.project.discuss_service.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.discuss_service.core.domain.entity.MessageEntity;

import java.util.List;
import java.util.Optional;

public interface IMessagePort {

    /**
     * Save a message (create or update)
     */
    MessageEntity save(MessageEntity message);

    /**
     * Find message by ID
     */
    Optional<MessageEntity> findById(Long id);

    /**
     * Find messages in a channel with pagination (newest first)
     */
    Pair<Long, List<MessageEntity>> findByChannelId(Long channelId, int page, int size);

    /**
     * Find messages before a specific ID (for infinite scroll)
     */
    List<MessageEntity> findBeforeId(Long channelId, Long beforeId, int limit);

    /**
     * Find thread replies for a message
     */
    List<MessageEntity> findReplies(Long parentId);

    /**
     * Find messages by sender
     */
    List<MessageEntity> findBySenderId(Long senderId, int page, int size);

    /**
     * Find messages mentioning a user
     */
    List<MessageEntity> findByMentioningUser(Long userId, int page, int size);

    /**
     * Search messages in a channel using full-text search
     */
    List<MessageEntity> searchMessages(Long channelId, String query, int page, int size);

    /**
     * Count unread messages in channel after a specific message
     */
    long countUnreadMessages(Long channelId, Long afterMessageId);

    /**
     * Soft delete all messages in a channel
     */
    int softDeleteByChannelId(Long channelId, Long deletedAt);

    /**
     * Count messages in a channel
     */
    long countByChannelId(Long channelId);
}
