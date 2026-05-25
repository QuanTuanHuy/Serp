/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.query.get;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationCandidateSkillFitView {
    private Long suggestedAssigneeId;
    private Double requiredCoveragePercent;
    private Double preferredCoveragePercent;
    private List<Long> matchedRequiredSkills;
    private List<Long> missingRequiredSkills;
    private List<Long> matchedPreferredSkills;
    private List<Long> missingPreferredSkills;
    private String proficiencySummary;
    private String confidence;
}
