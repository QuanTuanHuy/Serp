/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import serp.project.pmcore.core.domain.enums.ConsumerInboxAcquireResult;
import serp.project.pmcore.core.domain.enums.ConsumerInboxStatus;
import serp.project.pmcore.core.port.store.IConsumerInboxPort;

@Component
public class ConsumerInboxAdapter implements IConsumerInboxPort {

    private static final int MAX_ERROR_LENGTH = 2_000;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ConsumerInboxAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ConsumerInboxAcquireResult acquireForProcessing(
            String consumerGroup,
            String eventId,
            String eventType,
            String topic,
            Integer partitionNo,
            Long offsetNo,
            Long tenantId,
            String payloadHash,
            String rawPayload) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("consumerGroup", consumerGroup)
                .addValue("eventId", eventId)
                .addValue("eventType", eventType)
                .addValue("topic", topic)
                .addValue("partitionNo", partitionNo)
                .addValue("offsetNo", offsetNo)
                .addValue("tenantId", tenantId)
                .addValue("payloadHash", payloadHash)
                .addValue("rawPayload", rawPayload);

        String insertSql = """
                INSERT INTO consumer_inbox_events (
                    consumer_group,
                    event_id,
                    event_type,
                    topic,
                    partition_no,
                    offset_no,
                    tenant_id,
                    payload_hash,
                    raw_payload,
                    status,
                    attempts,
                    created_at,
                    updated_at
                ) VALUES (
                    :consumerGroup,
                    :eventId,
                    :eventType,
                    :topic,
                    :partitionNo,
                    :offsetNo,
                    :tenantId,
                    :payloadHash,
                    CAST(:rawPayload AS jsonb),
                    'PROCESSING',
                    1,
                    NOW(),
                    NOW()
                )
                ON CONFLICT (consumer_group, event_id) DO NOTHING
                """;

        int inserted = jdbcTemplate.update(insertSql, params);
        if (inserted > 0) {
            return ConsumerInboxAcquireResult.ACQUIRED;
        }

        ConsumerInboxStatus currentStatus = getCurrentStatus(consumerGroup, eventId);
        if (currentStatus == null) {
            int retriedInsert = jdbcTemplate.update(insertSql, params);
            if (retriedInsert > 0) {
                return ConsumerInboxAcquireResult.ACQUIRED;
            }
            throw new IllegalStateException(
                    "Unable to acquire consumer inbox row for eventId=" + eventId);
        }
        if (currentStatus == ConsumerInboxStatus.PROCESSED) {
            return ConsumerInboxAcquireResult.ALREADY_PROCESSED;
        }
        if (currentStatus == ConsumerInboxStatus.DEAD) {
            return ConsumerInboxAcquireResult.ALREADY_DEAD;
        }

        String reacquireSql = """
                UPDATE consumer_inbox_events
                SET status = 'PROCESSING',
                    attempts = attempts + 1,
                    event_type = :eventType,
                    topic = :topic,
                    partition_no = :partitionNo,
                    offset_no = :offsetNo,
                    tenant_id = :tenantId,
                    payload_hash = :payloadHash,
                    raw_payload = CAST(:rawPayload AS jsonb),
                    last_error = NULL,
                    updated_at = NOW()
                WHERE consumer_group = :consumerGroup
                AND event_id = :eventId
                """;
        jdbcTemplate.update(reacquireSql, params);

        return ConsumerInboxAcquireResult.ACQUIRED;
    }

    @Override
    public void markProcessed(String consumerGroup, String eventId, Long offsetNo) {
        String sql = """
                UPDATE consumer_inbox_events
                SET status = 'PROCESSED',
                    offset_no = :offsetNo,
                    processed_at = NOW(),
                    last_error = NULL,
                    updated_at = NOW()
                WHERE consumer_group = :consumerGroup
                AND event_id = :eventId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("consumerGroup", consumerGroup)
                .addValue("eventId", eventId)
                .addValue("offsetNo", offsetNo);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void markFailed(String consumerGroup, String eventId, String errorMessage) {
        String sql = """
                UPDATE consumer_inbox_events
                SET status = 'FAILED',
                    last_error = :lastError,
                    updated_at = NOW()
                WHERE consumer_group = :consumerGroup
                AND event_id = :eventId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("consumerGroup", consumerGroup)
                .addValue("eventId", eventId)
                .addValue("lastError", normalizeError(errorMessage));
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void markDead(String consumerGroup, String eventId, String errorMessage) {
        String sql = """
                UPDATE consumer_inbox_events
                SET status = 'DEAD',
                    last_error = :lastError,
                    updated_at = NOW()
                WHERE consumer_group = :consumerGroup
                AND event_id = :eventId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("consumerGroup", consumerGroup)
                .addValue("eventId", eventId)
                .addValue("lastError", normalizeError(errorMessage));
        jdbcTemplate.update(sql, params);
    }

    @Override
    public int deleteTerminalEventsBefore(long timestamp) {
        LocalDateTime before = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        String sql = """
                DELETE FROM consumer_inbox_events
                WHERE status IN ('PROCESSED', 'DEAD')
                AND updated_at < :before
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("before", before);
        return jdbcTemplate.update(sql, params);
    }

    private ConsumerInboxStatus getCurrentStatus(String consumerGroup, String eventId) {
        String sql = """
                SELECT status
                FROM consumer_inbox_events
                WHERE consumer_group = :consumerGroup
                AND event_id = :eventId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("consumerGroup", consumerGroup)
                .addValue("eventId", eventId);

        try {
            String status = jdbcTemplate.queryForObject(sql, params, String.class);
            return ConsumerInboxStatus.valueOf(status);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private String normalizeError(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        String normalized = errorMessage.trim();
        if (normalized.length() <= MAX_ERROR_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_ERROR_LENGTH);
    }
}
