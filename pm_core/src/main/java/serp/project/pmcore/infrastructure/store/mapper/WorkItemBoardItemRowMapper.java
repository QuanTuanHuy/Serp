/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardItemProjection;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class WorkItemBoardItemRowMapper extends BaseRowMapper implements RowMapper<WorkItemBoardItemProjection> {

    @Override
    public WorkItemBoardItemProjection mapRow(ResultSet rs, int rowNum) throws SQLException {
        return WorkItemBoardItemProjection.builder()
                .id(rs.getLong("id"))
                .projectId(rs.getLong("project_id"))
                .parentId(getNullableLong(rs, "parent_id"))
                .key(rs.getString("key"))
                .summary(rs.getString("summary"))
                .description(rs.getString("description"))
                .assigneeId(getNullableLong(rs, "assignee_id"))
                .assigneeName(rs.getString("assignee_name"))
                .assigneeAvatarUrl(rs.getString("assignee_avatar_url"))
                .reporterId(getNullableLong(rs, "reporter_id"))
                .startDate(toEpochMilli(rs.getTimestamp("start_date")))
                .dueDate(toEpochMilli(rs.getTimestamp("due_date")))
                .rank(rs.getString("rank"))
                .issueTypeId(getNullableLong(rs, "issue_type_id"))
                .issueTypeName(rs.getString("issue_type_name"))
                .issueTypeIconUrl(rs.getString("issue_type_icon_url"))
                .issueTypeHierarchyLevel(getNullableInt(rs, "issue_type_hierarchy_level"))
                .statusId(getNullableLong(rs, "status_id"))
                .statusKey(rs.getString("status_key"))
                .statusName(rs.getString("status_name"))
                .priorityId(getNullableLong(rs, "priority_id"))
                .priorityName(rs.getString("priority_name"))
                .priorityIconUrl(rs.getString("priority_icon_url"))
                .priorityColor(rs.getString("priority_color"))
                .build();
    }
}
