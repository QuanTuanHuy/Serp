/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineItemProjection;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class WorkItemTimelineItemRowMapper extends BaseRowMapper implements RowMapper<WorkItemTimelineItemProjection> {

    @Override
    public WorkItemTimelineItemProjection mapRow(ResultSet rs, int rowNum) throws SQLException {
        return WorkItemTimelineItemProjection.builder()
                .id(rs.getLong("id"))
                .projectId(rs.getLong("project_id"))
                .parentId(getNullableLong(rs, "parent_id"))
                .key(rs.getString("key"))
                .summary(rs.getString("summary"))
                .assigneeId(getNullableLong(rs, "assignee_id"))
                .startDate(toEpochMilli(rs.getTimestamp("start_date")))
                .dueDate(toEpochMilli(rs.getTimestamp("due_date")))
                .unscheduled(rs.getBoolean("is_unscheduled"))
                .hasChildren(rs.getBoolean("has_children"))
                .rank(rs.getString("rank"))
                .issueTypeId(getNullableLong(rs, "issue_type_id"))
                .issueTypeName(rs.getString("issue_type_name"))
                .issueTypeIconUrl(rs.getString("issue_type_icon_url"))
                .issueTypeHierarchyLevel(getNullableInt(rs, "issue_type_hierarchy_level"))
                .statusId(getNullableLong(rs, "status_id"))
                .statusName(rs.getString("status_name"))
                .priorityId(getNullableLong(rs, "priority_id"))
                .priorityName(rs.getString("priority_name"))
                .priorityColor(rs.getString("priority_color"))
                .build();
    }
}
