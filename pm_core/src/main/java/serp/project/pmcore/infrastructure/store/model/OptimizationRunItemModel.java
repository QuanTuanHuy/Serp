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
import org.hibernate.annotations.SQLRestriction;
import serp.project.pmcore.domain.optimization.enums.OptimizationApplyStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "optimization_run_items")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OptimizationRunItemModel extends BaseModel {
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "run_id", nullable = false)
    private Long runId;
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    @Column(name = "work_item_id", nullable = false)
    private Long workItemId;
    @Column(name = "work_item_updated_at_snapshot")
    private LocalDateTime workItemUpdatedAtSnapshot;
    @Column(name = "plan_updated_at_snapshot")
    private LocalDateTime planUpdatedAtSnapshot;
    @Column(name = "current_assignee_id")
    private Long currentAssigneeId;
    @Column(name = "suggested_assignee_id")
    private Long suggestedAssigneeId;
    @Column(name = "override_assignee_id")
    private Long overrideAssigneeId;
    @Column(name = "current_planned_start")
    private LocalDateTime currentPlannedStart;
    @Column(name = "current_planned_end")
    private LocalDateTime currentPlannedEnd;
    @Column(name = "suggested_planned_start")
    private LocalDateTime suggestedPlannedStart;
    @Column(name = "suggested_planned_end")
    private LocalDateTime suggestedPlannedEnd;
    @Column(name = "override_planned_start")
    private LocalDateTime overridePlannedStart;
    @Column(name = "override_planned_end")
    private LocalDateTime overridePlannedEnd;
    @Column(name = "current_due_date")
    private LocalDateTime currentDueDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_decision", nullable = false, length = 50)
    private OptimizationDecision assignmentDecision;
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_decision", nullable = false, length = 50)
    private OptimizationDecision scheduleDecision;
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_apply_status", nullable = false, length = 50)
    private OptimizationApplyStatus assignmentApplyStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_apply_status", nullable = false, length = 50)
    private OptimizationApplyStatus scheduleApplyStatus;
    @Column(name = "score")
    private BigDecimal score;
    @Column(name = "cost")
    private BigDecimal cost;
    @Column(name = "confidence", length = 50)
    private String confidence;
    @Column(name = "assignment_reasons_json", columnDefinition = "TEXT")
    private String assignmentReasonsJson;
    @Column(name = "schedule_reasons_json", columnDefinition = "TEXT")
    private String scheduleReasonsJson;
    @Column(name = "violations_json", columnDefinition = "TEXT")
    private String violationsJson;
    @Column(name = "allocation_chunks_json", columnDefinition = "TEXT")
    private String allocationChunksJson;
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;
    @Column(name = "assignment_skipped_reason")
    private String assignmentSkippedReason;
    @Column(name = "schedule_skipped_reason")
    private String scheduleSkippedReason;
}
