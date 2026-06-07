/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.enums.OptimizationCapability;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmDescriptor;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithm;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptimizationAlgorithmRegistryTest {

    @Test
    void resolveShouldReturnRegisteredAlgorithmByKey() {
        IOptimizationAlgorithm algorithm = stubAlgorithm(OptimizationAlgorithmKeys.GREEDY_BALANCED);
        OptimizationAlgorithmRegistry registry = new OptimizationAlgorithmRegistry(List.of(algorithm));

        IOptimizationAlgorithm resolved = registry.resolve(OptimizationAlgorithmKeys.GREEDY_BALANCED);

        assertThat(resolved).isSameAs(algorithm);
    }

    @Test
    void resolveShouldRejectUnknownAlgorithmKey() {
        OptimizationAlgorithmRegistry registry = new OptimizationAlgorithmRegistry(
                List.of(stubAlgorithm(OptimizationAlgorithmKeys.GREEDY_BALANCED))
        );

        assertThatThrownBy(() -> registry.resolve("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported optimization algorithm");
    }

    @Test
    void resolveShouldReturnSkillFirstAlgorithmByKey() {
        IOptimizationAlgorithm algorithm = stubAlgorithm(OptimizationAlgorithmKeys.GREEDY_SKILL_FIRST);
        OptimizationAlgorithmRegistry registry = new OptimizationAlgorithmRegistry(List.of(algorithm));

        IOptimizationAlgorithm resolved = registry.resolve(OptimizationAlgorithmKeys.GREEDY_SKILL_FIRST);

        assertThat(resolved).isSameAs(algorithm);
    }

    private IOptimizationAlgorithm stubAlgorithm(String key) {
        return new IOptimizationAlgorithm() {
            @Override
            public OptimizationAlgorithmDescriptor descriptor() {
                return new OptimizationAlgorithmDescriptor(
                        key,
                        OptimizationAlgorithmKeys.DEFAULT_VERSION,
                        Set.of(OptimizationCapability.ASSIGNMENT)
                );
            }

            @Override
            public OptimizationSolution solve(OptimizationProblem problem) {
                return null;
            }
        };
    }
}
