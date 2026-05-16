/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

public record OptimizationCandidateAssignee(
        Long workItemId,
        Long candidateId,
        double baseCost,
        boolean currentAssignee,
        boolean componentLead,
        boolean projectLead,
        boolean reporter
) {
}
