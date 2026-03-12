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

    MessageEntity save(MessageEntity message);

    Optional<MessageEntity> findById(Long id);

    Pair<Long, List<MessageEntity>> findByChannelId(Long channelId, int page, int size);

    List<MessageEntity> findBeforeId(Long channelId, Long beforeId, int limit);

    List<MessageEntity> findReplies(Long parentId);

    List<MessageEntity> findBySenderId(Long senderId, int page, int size);

    List<MessageEntity> findByMentioningUser(Long userId, int page, int size);

    Pair<Long, List<MessageEntity>> searchMessages(Long channelId, String query, int page, int size);

    long countUnreadMessages(Long channelId, Long afterMessageId);

    int softDeleteByChannelId(Long channelId, Long deletedAt);

    long countByChannelId(Long channelId);
}
