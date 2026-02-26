/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import serp.project.pmcore.core.domain.entity.OutboxEventEntity;
import serp.project.pmcore.core.domain.enums.OutboxEventStatus;

@Component
public class OutboxEventRowMapper extends BaseRowMapper implements RowMapper<OutboxEventEntity> {

    @Override
    public OutboxEventEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return OutboxEventEntity.builder()
                .id(rs.getLong("id"))
                .tenantId(rs.getLong("tenant_id"))
                .aggregateType(rs.getString("aggregate_type"))
                .aggregateId(rs.getLong("aggregate_id"))
                .eventType(rs.getString("event_type"))
                .topic(rs.getString("topic"))
                .partitionKey(rs.getString("partition_key"))
                .payload(rs.getString("payload"))
                .status(OutboxEventStatus.valueOf(rs.getString("status")))
                .retryCount(rs.getInt("retry_count"))
                .maxRetries(rs.getInt("max_retries"))
                .nextRetryAt(toEpochMilli(rs.getTimestamp("next_retry_at")))
                .publishedAt(toEpochMilli(rs.getTimestamp("published_at")))
                .errorMessage(rs.getString("error_message"))
                .createdAt(toEpochMilli(rs.getTimestamp("created_at")))
                .updatedAt(toEpochMilli(rs.getTimestamp("updated_at")))
                .build();
    }
}
