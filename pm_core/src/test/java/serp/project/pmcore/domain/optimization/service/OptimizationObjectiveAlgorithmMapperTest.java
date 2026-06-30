/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizationObjectiveAlgorithmMapperTest {

    @Test
    void algorithmKeyForShouldMapEveryObjective() {
        assertThat(OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(OptimizationObjective.BALANCED_WORKLOAD))
                .isEqualTo(OptimizationAlgorithmKeys.GREEDY_BALANCED);
        assertThat(OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(OptimizationObjective.SKILL_FIRST))
                .isEqualTo(OptimizationAlgorithmKeys.GREEDY_SKILL_FIRST);
        assertThat(OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(OptimizationObjective.DEADLINE_FIRST))
                .isEqualTo(OptimizationAlgorithmKeys.GREEDY_DEADLINE_FIRST);
        assertThat(OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(OptimizationObjective.MINIMAL_REASSIGNMENT))
                .isEqualTo(OptimizationAlgorithmKeys.GREEDY_MINIMAL_REASSIGNMENT);
    }

    @Test
    void algorithmKeyForShouldDefaultNullObjectiveToBalanced() {
        assertThat(OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(null))
                .isEqualTo(OptimizationAlgorithmKeys.GREEDY_BALANCED);
    }
}
