/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.OutboxEventEntity;
import serp.project.account.core.domain.enums.OutboxEventStatus;
import serp.project.account.core.port.store.IOutboxEventPort;
import serp.project.account.infrastructure.store.mapper.OutboxEventRowMapper;

@Component
public class OutboxEventAdapter implements IOutboxEventPort {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final OutboxEventRowMapper rowMapper;

    public OutboxEventAdapter(NamedParameterJdbcTemplate jdbcTemplate, OutboxEventRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    @Override
    public OutboxEventEntity save(OutboxEventEntity event) {
        String sql = """
                INSERT INTO outbox_events (
                    tenant_id,
                    aggregate_type,
                    aggregate_id,
                    event_type,
                    topic,
                    partition_key,
                    payload,
                    status,
                    retry_count,
                    max_retries,
                    next_retry_at,
                    published_at,
                    error_message,
                    created_at,
                    updated_at
                )
                VALUES (
                    :tenantId,
                    :aggregateType,
                    :aggregateId,
                    :eventType,
                    :topic,
                    :partitionKey,
                    CAST(:payload AS jsonb),
                    :status,
                    :retryCount,
                    :maxRetries,
                    :nextRetryAt,
                    :publishedAt,
                    :errorMessage,
                    :createdAt,
                    :updatedAt
                )
                RETURNING id
                """;
        var params = new MapSqlParameterSource()
                .addValue("tenantId", event.getTenantId())
                .addValue("aggregateType", event.getAggregateType())
                .addValue("aggregateId", event.getAggregateId())
                .addValue("eventType", event.getEventType())
                .addValue("payload", event.getPayload())
                .addValue("topic", event.getTopic())
                .addValue("partitionKey", event.getPartitionKey())
                .addValue("status", event.getStatus().name())
                .addValue("retryCount", event.getRetryCount())
                .addValue("maxRetries", event.getMaxRetries())
                .addValue("nextRetryAt", longToLocalDateTime(event.getNextRetryAt()))
                .addValue("publishedAt", longToLocalDateTime(event.getPublishedAt()))
                .addValue("errorMessage", event.getErrorMessage())
                .addValue("createdAt", longToLocalDateTime(event.getCreatedAt()))
                .addValue("updatedAt", longToLocalDateTime(event.getUpdatedAt()));
        Long generatedId = jdbcTemplate.queryForObject(sql, params, Long.class);
        event.setId(generatedId);
        return event;
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

        var batchParams = events.stream()
                .map(event -> new MapSqlParameterSource()
                        .addValue("id", event.getId())
                        .addValue("status", event.getStatus().name())
                        .addValue("retryCount", event.getRetryCount())
                        .addValue("nextRetryAt", longToLocalDateTime(event.getNextRetryAt()))
                        .addValue("publishedAt", longToLocalDateTime(event.getPublishedAt()))
                        .addValue("errorMessage", event.getErrorMessage())
                        .addValue("updatedAt", longToLocalDateTime(event.getUpdatedAt())))
                .toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(sql, batchParams);
    }

    @Override
    public List<OutboxEventEntity> getEventsByStatuses(List<OutboxEventStatus> statuses, int limit) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        String sql = """
                SELECT * FROM outbox_events
                WHERE status IN (:statuses)
                AND (next_retry_at IS NULL OR next_retry_at <= NOW())
                ORDER BY created_at ASC
                LIMIT :limit
                FOR UPDATE SKIP LOCKED
                """;
        var params = new MapSqlParameterSource()
                .addValue("statuses", statuses.stream().map(Enum::name).toList())
                .addValue("limit", limit);
        return jdbcTemplate.query(sql, params, rowMapper);

    }

    @Override
    public int deletePublishedEventsBefore(long timestamp) {
        if (timestamp <= 0) {
            return 0;
        }
        String sql = """
                DELETE FROM outbox_events
                WHERE status = 'PUBLISHED'
                AND published_at < :before
                """;
        var params = new MapSqlParameterSource()
                .addValue("before", longToLocalDateTime(timestamp));
        return jdbcTemplate.update(sql, params);
    }

    private LocalDateTime longToLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
}
