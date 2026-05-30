/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.optimization.enums.WorkItemPlanSource;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_item_plan_allocations")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class WorkItemPlanAllocationModel extends BaseModel {
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "work_item_plan_id", nullable = false)
    private Long workItemPlanId;

    @Column(name = "work_item_id", nullable = false)
    private Long workItemId;

    @Column(name = "assignee_id", nullable = false)
    private Long assigneeId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "effort_millis", nullable = false)
    private Long effortMillis;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private WorkItemPlanSource source;

    @Column(name = "source_run_id")
    private Long sourceRunId;

    @Column(name = "source_run_item_id")
    private Long sourceRunItemId;
}
