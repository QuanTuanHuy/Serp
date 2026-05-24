/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workitem.dto.WorkItemLinkProjection;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class WorkItemLinkRowMapper extends BaseRowMapper implements RowMapper<WorkItemLinkProjection> {

    @Override
    public WorkItemLinkProjection mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new WorkItemLinkProjection(
                rs.getLong("id"),
                getNullableLong(rs, "source_id"),
                getNullableLong(rs, "target_id"),
                getNullableLong(rs, "link_type_id"),
                rs.getString("link_type_name"),
                rs.getString("outward_desc"),
                rs.getString("inward_desc"),
                getNullableLong(rs, "related_work_item_id"),
                getNullableLong(rs, "related_project_id"),
                rs.getString("related_work_item_key"),
                rs.getString("related_work_item_summary"),
                getNullableLong(rs, "related_status_id"),
                rs.getString("related_status_key"),
                rs.getString("related_status_name"),
                getNullableLong(rs, "related_priority_id"),
                rs.getString("related_priority_name"),
                rs.getString("related_priority_color")
        );
    }
}
