/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;

import java.util.List;

public record OptimizationCandidateSkillFit(
        Long workItemId,
        Long candidateId,
        int matchedRequiredSkillCount,
        int totalRequiredSkillCount,
        int matchedPreferredSkillCount,
        int totalPreferredSkillCount,
        double requiredCoveragePercent,
        double preferredCoveragePercent,
        double proficiencyScore,
        List<Long> missingRequiredSkillIds,
        List<Long> missingPreferredSkillIds,
        List<Long> matchedSkillIds,
        OptimizationConfidence confidence
) {
    public static OptimizationCandidateSkillFit neutral(Long workItemId, Long candidateId) {
        return new OptimizationCandidateSkillFit(workItemId, candidateId, 0, 0, 0, 0, 100D, 100D,
                0D, List.of(), List.of(), List.of(), OptimizationConfidence.LOW);
    }
}
