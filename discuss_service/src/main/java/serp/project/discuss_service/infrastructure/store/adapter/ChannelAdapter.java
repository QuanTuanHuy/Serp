/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel adapter implementation
 */

package serp.project.discuss_service.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.enums.ChannelType;
import serp.project.discuss_service.core.port.store.IChannelPort;
import serp.project.discuss_service.infrastructure.store.mapper.ChannelMapper;
import serp.project.discuss_service.infrastructure.store.model.ChannelModel;
import serp.project.discuss_service.infrastructure.store.repository.IChannelRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChannelAdapter implements IChannelPort {

    private final IChannelRepository channelRepository;
    private final ChannelMapper channelMapper;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public ChannelEntity save(ChannelEntity channel) {
        ChannelModel model = channelMapper.toModel(channel);
        ChannelModel saved = channelRepository.save(model);
        return channelMapper.toEntity(saved);
    }

    @Override
    public Optional<ChannelEntity> findById(Long id) {
        return channelRepository.findById(id)
                .map(channelMapper::toEntity);
    }

    @Override
    public List<ChannelEntity> findByIds(List<Long> ids) {
        return channelMapper.toEntityList(channelRepository.findByIdIn(ids));
    }

    @Override
    public List<ChannelEntity> findByTenantId(Long tenantId) {
        return channelMapper.toEntityList(
                channelRepository.findByTenantIdAndIsArchivedFalse(tenantId));
    }

    @Override
    public Pair<Long, List<ChannelEntity>> findByTenantIdPaginated(Long tenantId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageAt"));
        var pageResult = channelRepository.findByTenantIdAndIsArchivedFalse(tenantId, pageable);
        return Pair.of(
                pageResult.getTotalElements(),
                channelMapper.toEntityList(pageResult.getContent()));
    }

    @Override
    public List<ChannelEntity> findByTenantIdAndType(Long tenantId, ChannelType type) {
        return channelMapper.toEntityList(
                channelRepository.findByTenantIdAndTypeAndIsArchivedFalse(tenantId, type));
    }

    @Override
    public Optional<ChannelEntity> findDirectChannel(Long tenantId, Long userId1, Long userId2) {
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);

        return channelRepository.findDirectChannel(tenantId, smallerId, largerId)
                .map(channelMapper::toEntity);
    }

    @Override
    public Optional<ChannelEntity> findByEntity(Long tenantId, String entityType, Long entityId) {
        return channelRepository.findByTenantIdAndEntityTypeAndEntityId(tenantId, entityType, entityId)
                .map(channelMapper::toEntity);
    }

    @Override
    public long countByTenantId(Long tenantId) {
        return channelRepository.countByTenantIdAndIsArchivedFalse(tenantId);
    }

    @Override
    public void deleteById(Long id) {
        channelRepository.deleteById(id);
    }

    @Override
    public Pair<Long, List<ChannelEntity>> findUserChannelsPaginated(Long userId, Long tenantId, int page, int size,
            ChannelType type, Boolean isArchived, String entityType, Long entityId, String searchQuery) {
        String fromAndWhere = buildFromAndWhereClause(type, isArchived, entityType, entityId, searchQuery);
        String dataQuery = "SELECT c.id " + fromAndWhere
                + "ORDER BY c.last_message_at DESC NULLS LAST, c.id DESC "
                + "LIMIT :limit OFFSET :offset";
        String countQuery = "SELECT COUNT(DISTINCT c.id) " + fromAndWhere;

        String normalizedSearchQuery = normalizeSearchQuery(searchQuery);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("userId", userId)
                .addValue("type", type != null ? type.name() : null)
                .addValue("isArchived", isArchived)
                .addValue("entityType", entityType)
                .addValue("entityId", entityId)
                .addValue("limit", size)
                .addValue("offset", page * size);

        if (normalizedSearchQuery != null) {
            params.addValue("searchLike", "%" + normalizedSearchQuery.toLowerCase() + "%");
        }

        Long total = jdbcTemplate.queryForObject(countQuery, params, Long.class);

        List<Long> channelIds = jdbcTemplate.query(dataQuery, params, (rs, rowNum) -> rs.getLong("id"));
        if (channelIds.isEmpty()) {
            return Pair.of(total != null ? total : 0L, List.of());
        }

        List<ChannelEntity> channels = channelMapper.toEntityList(channelRepository.findByIdIn(channelIds));
        Map<Long, ChannelEntity> channelsById = new HashMap<>();
        for (ChannelEntity channel : channels) {
            channelsById.putIfAbsent(channel.getId(), channel);
        }

        List<ChannelEntity> orderedChannels = channelIds.stream()
                .map(channelsById::get)
                .filter(Objects::nonNull)
                .toList();

        return Pair.of(total != null ? total : 0L, orderedChannels);
    }

    private String normalizeSearchQuery(String searchQuery) {
        if (searchQuery == null) {
            return null;
        }

        String trimmed = searchQuery.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildFromAndWhereClause(ChannelType type, Boolean isArchived, String entityType,
            Long entityId, String searchQuery) {
        StringBuilder sql = new StringBuilder();
        sql.append("FROM channels c ")
                .append("JOIN channel_members m ON c.id = m.channel_id ")
                .append("WHERE c.tenant_id = :tenantId AND m.user_id = :userId AND m.status = 'ACTIVE' ");

        if (type != null) {
            sql.append("AND c.type = :type ");
        }
        if (isArchived != null) {
            sql.append("AND c.is_archived = :isArchived ");
        }
        if (entityType != null) {
            sql.append("AND c.entity_type = :entityType ");
        }
        if (entityId != null) {
            sql.append("AND c.entity_id = :entityId ");
        }
        if (normalizeSearchQuery(searchQuery) != null) {
            sql.append("AND (LOWER(c.name) LIKE :searchLike OR LOWER(COALESCE(c.description, '')) LIKE :searchLike) ");
        }

        return sql.toString();
    }
}
