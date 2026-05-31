/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.assignment;

import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;

import java.util.List;
import java.util.Map;

public interface OptimizationAssignmentPolicy {
    Map<Long, OptimizationAssignmentSuggestion> generateAssignments(
            OptimizationProjectModel projectModel,
            OptimizationAlgorithmOptions options,
            List<OptimizationConstraintViolation> warnings
    );
}
