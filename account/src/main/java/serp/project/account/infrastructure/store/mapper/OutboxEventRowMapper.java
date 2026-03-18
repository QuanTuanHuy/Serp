/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.OutboxEventEntity;
import serp.project.account.core.domain.enums.OutboxEventStatus;

@Component
public class OutboxEventRowMapper implements RowMapper<OutboxEventEntity> {

    @Override
    public OutboxEventEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setId(rs.getLong("id"));
        event.setTenantId(rs.getLong("tenant_id"));
        event.setAggregateType(rs.getString("aggregate_type"));
        event.setAggregateId(rs.getLong("aggregate_id"));
        event.setEventType(rs.getString("event_type"));
        event.setTopic(rs.getString("topic"));
        event.setPartitionKey(rs.getString("partition_key"));
        event.setPayload(rs.getString("payload"));
        event.setStatus(OutboxEventStatus.valueOf(rs.getString("status")));
        event.setRetryCount(rs.getInt("retry_count"));
        event.setMaxRetries(rs.getInt("max_retries"));
        event.setNextRetryAt(toEpochMilli(rs.getTimestamp("next_retry_at")));
        event.setPublishedAt(toEpochMilli(rs.getTimestamp("published_at")));
        event.setErrorMessage(rs.getString("error_message"));
        event.setCreatedAt(toEpochMilli(rs.getTimestamp("created_at")));
        event.setUpdatedAt(toEpochMilli(rs.getTimestamp("updated_at")));
        return event;
    }

    private Long toEpochMilli(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.getTime();
    }

}
