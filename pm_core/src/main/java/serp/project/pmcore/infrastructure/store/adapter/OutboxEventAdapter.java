/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.core.domain.entity.OutboxEventEntity;
import serp.project.pmcore.core.domain.enums.OutboxEventStatus;
import serp.project.pmcore.core.port.store.IOutboxEventPort;
import serp.project.pmcore.infrastructure.store.mapper.OutboxEventMapper;
import serp.project.pmcore.infrastructure.store.mapper.OutboxEventRowMapper;
import serp.project.pmcore.infrastructure.store.repository.IOutboxEventRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventAdapter implements IOutboxEventPort {

    private final OutboxEventMapper mapper;
    private final OutboxEventRowMapper rowMapper;
    private final IOutboxEventRepository repository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public OutboxEventEntity save(OutboxEventEntity event) {
        return mapper.toEntity(
                repository.save(mapper.toModel(event)));
    }

    @Override
    public void batchUpdateStatus(List<OutboxEventEntity> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        String sql = """
                UPDATE outbox_events
                SET status = :status,
                    retry_count = :retryCount,
                    next_retry_at = :nextRetryAt,
                    published_at = :publishedAt,
                    error_message = :errorMessage,
                    updated_at = :updatedAt
                WHERE id = :id
                """;

        SqlParameterSource[] batchParams = events.stream()
                .map(event -> new MapSqlParameterSource()
                        .addValue("id", event.getId())
                        .addValue("status", event.getStatus().name())
                        .addValue("retryCount", event.getRetryCount())
                        .addValue("nextRetryAt", longToLocalDateTime(event.getNextRetryAt()))
                        .addValue("publishedAt", longToLocalDateTime(event.getPublishedAt()))
                        .addValue("errorMessage", event.getErrorMessage())
                        .addValue("updatedAt", longToLocalDateTime(event.getUpdatedAt())))
                .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(sql, batchParams);
    }

    @Override
    public List<OutboxEventEntity> getEventsByStatuses(List<OutboxEventStatus> statuses, int limit) {
        String sql = """
                SELECT * FROM outbox_events
                WHERE status IN (:statuses)
                AND (next_retry_at IS NULL OR next_retry_at <= NOW())
                ORDER BY created_at ASC
                LIMIT :limit
                FOR UPDATE SKIP LOCKED
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("statuses", statuses.stream().map(Enum::name).toList())
                .addValue("limit", limit);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public int deletePublishedEventsBefore(long timestamp) {
        LocalDateTime before = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        String sql = """
                DELETE FROM outbox_events
                WHERE status = 'PUBLISHED'
                AND published_at < :before
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("before", before);
        return jdbcTemplate.update(sql, params);
    }

    private LocalDateTime longToLocalDateTime(Long timestamp) {
        if (timestamp == null) { return null; }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

}
