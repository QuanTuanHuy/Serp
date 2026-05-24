/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workitem.dto.WorkItemChildProjection;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class WorkItemChildRowMapper extends BaseRowMapper implements RowMapper<WorkItemChildProjection> {

    @Override
    public WorkItemChildProjection mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new WorkItemChildProjection(
                rs.getLong("id"),
                rs.getLong("project_id"),
                getNullableLong(rs, "parent_id"),
                rs.getString("key"),
                rs.getString("summary"),
                getNullableLong(rs, "assignee_id"),
                getNullableLong(rs, "issue_type_id"),
                rs.getString("issue_type_name"),
                rs.getString("issue_type_icon_url"),
                getNullableInt(rs, "issue_type_hierarchy_level"),
                getNullableLong(rs, "status_id"),
                rs.getString("status_key"),
                rs.getString("status_name"),
                getNullableLong(rs, "priority_id"),
                rs.getString("priority_name"),
                rs.getString("priority_color"),
                rs.getString("rank")
        );
    }
}
