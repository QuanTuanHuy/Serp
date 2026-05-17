/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private String capacitySourceMode;
    private String calendarCoverageStatus;
    private String workloadCoverageStatus;
    private List<Long> fallbackUserIds;
    private Long calendarFetchedAt;
    private Long workloadFetchedAt;
    private Long deductedWorkloadMillis;
    private Long sameProjectOutsideScopeDeductedMillis;
    private Long crossProjectDeductedMillis;
    private List<CapacityWorkloadBucket> workloadBuckets;
    private Integer itemsWithSkillRequirements;
    private Integer itemsMissingSkillRequirements;
    private Integer candidatesWithSkillProfiles;
    private Integer candidatesMissingSkillProfiles;
    private Integer requiredSkillMismatchCount;
    private String skillRankingConfidence;
    private List<OptimizationCandidateSkillFit> selectedCandidateSkillFits;
}
