/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.WorkItemEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class WorkItemRowMapper extends BaseRowMapper implements RowMapper<WorkItemEntity> {

    @Override
    public WorkItemEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        var builder = WorkItemEntity.builder()
                .id(rs.getLong("id"))
                .tenantId(rs.getLong("tenant_id"))
                .projectId(rs.getLong("project_id"))
                .issueTypeId(rs.getLong("issue_type_id"))
                .issueNo(rs.getLong("issue_no"))
                .key(rs.getString("key"))
                .summary(rs.getString("summary"))
                .description(rs.getString("description"))
                .statusId(rs.getLong("status_id"))
                .priorityId(rs.getLong("priority_id"))
                .assigneeId(getNullableLong(rs, "assignee_id"))
                .reporterId(rs.getLong("reporter_id"))
                .parentId(getNullableLong(rs, "parent_id"))
                .dueDate(toEpochMilli(rs.getTimestamp("due_date")))
                .rank(rs.getString("rank"))
                .createdAt(toEpochMilli(rs.getTimestamp("created_at")))
                .updatedAt(toEpochMilli(rs.getTimestamp("updated_at")))
                .createdBy(getNullableLong(rs, "created_by"))
                .updatedBy(getNullableLong(rs, "updated_by"));

        // Enriched columns (only present when LEFT JOINs are active)
        if (hasColumn(rs, "issue_type_name")) {
            builder.issueTypeName(rs.getString("issue_type_name"))
                    .issueTypeIconUrl(rs.getString("issue_type_icon_url"))
                    .issueTypeHierarchyLevel(getNullableInt(rs, "issue_type_hierarchy_level"));
        }
        if (hasColumn(rs, "priority_name")) {
            builder.priorityName(rs.getString("priority_name"))
                    .priorityIconUrl(rs.getString("priority_icon_url"))
                    .priorityColor(rs.getString("priority_color"))
                    .prioritySequence(getNullableInt(rs, "priority_sequence"));
        }

        return builder.build();
    }

}
