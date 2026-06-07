/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workitem.dto.WorkItemActivityProjection;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class WorkItemActivityRowMapper extends BaseRowMapper implements RowMapper<WorkItemActivityProjection> {

    @Override
    public WorkItemActivityProjection mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new WorkItemActivityProjection(
                rs.getString("activity_id"),
                rs.getString("activity_type"),
                getNullableLong(rs, "actor_id"),
                rs.getString("body"),
                rs.getString("field_key"),
                rs.getString("field_name"),
                rs.getString("from_value"),
                rs.getString("to_value"),
                toEpochMilli(rs.getTimestamp("created_at"))
        );
    }
}
