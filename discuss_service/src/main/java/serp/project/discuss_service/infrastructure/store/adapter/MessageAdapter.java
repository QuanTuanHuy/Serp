/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message adapter implementation
 */

package serp.project.discuss_service.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.port.store.IMessagePort;
import serp.project.discuss_service.infrastructure.store.mapper.MessageMapper;
import serp.project.discuss_service.infrastructure.store.model.MessageModel;
import serp.project.discuss_service.infrastructure.store.repository.IMessageRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MessageAdapter implements IMessagePort {

    private final IMessageRepository messageRepository;
    private final MessageMapper messageMapper;

    @Override
    public MessageEntity save(MessageEntity message) {
        MessageModel model = messageMapper.toModel(message);
        MessageModel saved = messageRepository.save(model);
        return messageMapper.toEntity(saved);
    }

    @Override
    public Optional<MessageEntity> findById(Long id) {
        return messageRepository.findById(id)
                .map(messageMapper::toEntity);
    }

    @Override
    public Pair<Long, List<MessageEntity>> findByChannelId(Long channelId, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var pageResult = messageRepository.findByChannelIdAndIsDeletedFalseOrderByCreatedAtDesc(channelId, pageable);
        return Pair.of(
                pageResult.getTotalElements(),
                messageMapper.toEntityList(pageResult.getContent())
        );
    }

    @Override
    public List<MessageEntity> findBeforeId(Long channelId, Long beforeId, int limit) {
        var pageable = PageRequest.of(0, limit);
        return messageMapper.toEntityList(
                messageRepository.findMessagesBeforeId(channelId, beforeId, pageable));
    }

    @Override
    public List<MessageEntity> findReplies(Long parentId) {
        return messageMapper.toEntityList(
                messageRepository.findByParentIdAndIsDeletedFalseOrderByCreatedAtAsc(parentId));
    }

    @Override
    public List<MessageEntity> findBySenderId(Long senderId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return messageMapper.toEntityList(
                messageRepository.findBySenderIdAndIsDeletedFalse(senderId, pageable));
    }

    @Override
    public List<MessageEntity> findByMentioningUser(Long userId, int page, int size) {
        var pageable = PageRequest.of(page, size);
        return messageMapper.toEntityList(
                messageRepository.findByMentioningUser(userId, pageable));
    }

    @Override
    public List<MessageEntity> searchMessages(Long channelId, String query, int page, int size) {
        var pageable = PageRequest.of(page, size);
        return messageMapper.toEntityList(
                messageRepository.searchMessages(channelId, query, pageable));
    }

    @Override
    public long countUnreadMessages(Long channelId, Long afterMessageId) {
        return messageRepository.countUnreadMessages(channelId, afterMessageId);
    }

    @Override
    public int softDeleteByChannelId(Long channelId, Long deletedAt) {
        return messageRepository.softDeleteByChannelId(channelId, deletedAt);
    }

    @Override
    public long countByChannelId(Long channelId) {
        return messageRepository.countByChannelIdAndIsDeletedFalse(channelId);
    }
}
