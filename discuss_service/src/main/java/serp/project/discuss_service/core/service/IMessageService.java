/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message service interface
 */

package serp.project.discuss_service.core.service;

import org.springframework.data.util.Pair;
import serp.project.discuss_service.core.domain.entity.MessageEntity;

import java.util.List;
import java.util.Optional;

public interface IMessageService {
    MessageEntity sendMessage(MessageEntity message);

    MessageEntity sendReply(Long parentId, MessageEntity message);

    Optional<MessageEntity> getMessageById(Long id);

    MessageEntity getMessageByIdOrThrow(Long id);

    Pair<Long, List<MessageEntity>> getMessagesByChannel(Long channelId, int page, int size);

    List<MessageEntity> getMessagesBefore(Long channelId, Long beforeId, int limit);

    List<MessageEntity> getThreadReplies(Long parentId);

    List<MessageEntity> searchMessages(Long channelId, String query, int page, int size);

    MessageEntity editMessage(Long messageId, String newContent, Long editorId);

    MessageEntity deleteMessage(Long messageId, Long deleterId, boolean isAdmin);

    MessageEntity addReaction(Long messageId, Long userId, String emoji);

    MessageEntity removeReaction(Long messageId, Long userId, String emoji);

    void markAsRead(Long messageId, Long userId);

    long countUnreadMessages(Long channelId, Long afterMessageId);
}
