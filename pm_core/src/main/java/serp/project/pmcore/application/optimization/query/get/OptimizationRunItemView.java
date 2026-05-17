/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.query.get;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.optimization.enums.OptimizationApplyStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationRunItemView {
    private Long id;
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
    private OptimizationCandidateSkillFitView candidateSkillFit;
    private List<String> assignmentReasons;
    private List<String> scheduleReasons;
    private List<String> violations;
    private Long appliedAt;
    private String assignmentSkippedReason;
    private String scheduleSkippedReason;
}
