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
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "optimization_runs")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OptimizationRunModel extends BaseModel {
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    @Column(name = "scope", nullable = false, length = 50)
    private String scope;
    @Column(name = "mode", nullable = false, length = 50)
    private String mode;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OptimizationRunStatus status;
    @Column(name = "planning_start", nullable = false)
    private LocalDateTime planningStart;
    @Column(name = "planning_end", nullable = false)
    private LocalDateTime planningEnd;
    @Column(name = "allow_reassignment", nullable = false)
    private Boolean allowReassignment;
    @Column(name = "allow_schedule_changes", nullable = false)
    private Boolean allowScheduleChanges;
    @Column(name = "selected_work_item_count", nullable = false)
    private Integer selectedWorkItemCount;
    @Column(name = "summary_json", columnDefinition = "TEXT")
    private String summaryJson;
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;
    @Column(name = "applied_by")
    private Long appliedBy;
    @Column(name = "discarded_at")
    private LocalDateTime discardedAt;
}
