/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message adapter implementation
 */

package serp.project.discuss_service.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.port.store.IMessagePort;
import serp.project.discuss_service.infrastructure.store.mapper.MessageMapper;
import serp.project.discuss_service.infrastructure.store.model.MessageModel;
import serp.project.discuss_service.infrastructure.store.repository.IMessageRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MessageAdapter implements IMessagePort {

    private static final String SEARCH_FROM_AND_WHERE = """
            FROM messages m
            WHERE m.channel_id = :channelId
              AND m.is_deleted = false
              AND m.search_vector @@ websearch_to_tsquery('english', :query)
            """;

    private static final String SEARCH_COUNT_QUERY = "SELECT COUNT(*) " + SEARCH_FROM_AND_WHERE;

    private static final String SEARCH_DATA_QUERY = """
            SELECT m.id
            """ + SEARCH_FROM_AND_WHERE + """
            ORDER BY ts_rank_cd(m.search_vector, websearch_to_tsquery('english', :query)) DESC,
                     m.created_at DESC,
                     m.id DESC
            LIMIT :limit OFFSET :offset
            """;

    private final IMessageRepository messageRepository;
    private final MessageMapper messageMapper;

    private final NamedParameterJdbcTemplate jdbcTemplate;

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
    public Pair<Long, List<MessageEntity>> searchMessages(Long channelId, String query, int page, int size) {
        long offset = (long) page * size;
        var params = new MapSqlParameterSource()
                .addValue("channelId", channelId)
                .addValue("query", query)
                .addValue("limit", size)
                .addValue("offset", offset);

        Long total = jdbcTemplate.queryForObject(SEARCH_COUNT_QUERY, params, Long.class);
        if (total == null || total == 0L) {
            return Pair.of(0L, List.of());
        }

        List<Long> messageIds = jdbcTemplate.query(SEARCH_DATA_QUERY, params,
                (rs, rowNum) -> rs.getLong("id"));

        if (messageIds.isEmpty()) {
            return Pair.of(total, List.of());
        }

        List<MessageEntity> messages = messageMapper.toEntityList(messageRepository.findByIdIn(messageIds));
        Map<Long, MessageEntity> messagesById = new HashMap<>();
        for (MessageEntity message : messages) {
            messagesById.putIfAbsent(message.getId(), message);
        }

        List<MessageEntity> orderedMessages = messageIds.stream()
                .map(messagesById::get)
                .filter(Objects::nonNull)
                .toList();

        return Pair.of(total, orderedMessages);
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
