/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationRunSummary {
    private Integer scopeSize;
    private Integer assigneeCount;
    private Integer dependencyCount;
    private Long planningStart;
    private Long planningEnd;
    private Integer assignmentSuggestionCount;
    private Integer scheduledItemCount;
    private Integer lateItemsBefore;
    private Integer lateItemsAfter;
    private Integer overloadedAssigneeCountBefore;
    private Integer overloadedAssigneeCountAfter;
    private Integer warningsCount;
    private String confidenceLevel;
}
