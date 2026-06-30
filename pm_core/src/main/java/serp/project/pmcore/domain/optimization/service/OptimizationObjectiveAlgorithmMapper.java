/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import lombok.experimental.UtilityClass;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;

@UtilityClass
public class OptimizationObjectiveAlgorithmMapper {

    public static String algorithmKeyFor(OptimizationObjective objective) {
        if (objective == null) {
            return OptimizationAlgorithmKeys.GREEDY_BALANCED;
        }
        return switch (objective) {
            case BALANCED_WORKLOAD -> OptimizationAlgorithmKeys.GREEDY_BALANCED;
            case SKILL_FIRST -> OptimizationAlgorithmKeys.GREEDY_SKILL_FIRST;
            case DEADLINE_FIRST -> OptimizationAlgorithmKeys.GREEDY_DEADLINE_FIRST;
            case MINIMAL_REASSIGNMENT -> OptimizationAlgorithmKeys.GREEDY_MINIMAL_REASSIGNMENT;
        };
    }
}
