/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workitem.dto.WorkItemScheduleAllocationCalendarProjection;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class WorkItemScheduleAllocationCalendarRowMapper extends BaseRowMapper implements RowMapper<WorkItemScheduleAllocationCalendarProjection> {

    @Override
    public WorkItemScheduleAllocationCalendarProjection mapRow(ResultSet rs, int rowNum) throws SQLException {
        return WorkItemScheduleAllocationCalendarProjection.builder()
                .allocationId(rs.getLong("allocation_id"))
                .workItemPlanId(rs.getLong("work_item_plan_id"))
                .workItemId(rs.getLong("work_item_id"))
                .projectId(rs.getLong("project_id"))
                .key(rs.getString("key"))
                .summary(rs.getString("summary"))
                .assigneeId(getNullableLong(rs, "assignee_id"))
                .assigneeName(rs.getString("assignee_name"))
                .assigneeAvatarUrl(rs.getString("assignee_avatar_url"))
                .start(toEpochMilli(rs.getTimestamp("start_time")))
                .end(toEpochMilli(rs.getTimestamp("end_time")))
                .effortMillis(getNullableLong(rs, "effort_millis"))
                .plannedStart(toEpochMilli(rs.getTimestamp("planned_start")))
                .plannedEnd(toEpochMilli(rs.getTimestamp("planned_end")))
                .source(rs.getString("source"))
                .sourceRunId(getNullableLong(rs, "source_run_id"))
                .sourceRunItemId(getNullableLong(rs, "source_run_item_id"))
                .locked(rs.getBoolean("locked"))
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
