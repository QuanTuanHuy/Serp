/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.optimization.enums.OptimizationApplyStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;
import serp.project.pmcore.domain.shared.entity.BaseEntity;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OptimizationRunItemEntity extends BaseEntity {
    private Long tenantId;
    private Long runId;
    private Long projectId;
    private Long workItemId;
    private Long workItemUpdatedAtSnapshot;
    private Long planUpdatedAtSnapshot;
    private Long currentAssigneeId;
    private Long suggestedAssigneeId;
    private Long overrideAssigneeId;
    private Long currentPlannedStart;
    private Long currentPlannedEnd;
    private Long suggestedPlannedStart;
    private Long suggestedPlannedEnd;
    private Long overridePlannedStart;
    private Long overridePlannedEnd;
    private Long currentDueDate;
    private OptimizationDecision assignmentDecision;
    private OptimizationDecision scheduleDecision;
    private OptimizationApplyStatus assignmentApplyStatus;
    private OptimizationApplyStatus scheduleApplyStatus;
    private BigDecimal score;
    private BigDecimal cost;
    private String confidence;
    private String assignmentReasonsJson;
    private String scheduleReasonsJson;
    private String violationsJson;
    private String allocationChunksJson;
    private String overrideAllocationChunksJson;
    private Long appliedAt;
    private String assignmentSkippedReason;
    private String scheduleSkippedReason;
    private Long deletedAt;
}
