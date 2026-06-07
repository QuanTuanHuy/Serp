/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithm;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithmRegistry;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OptimizationAlgorithmRegistry implements IOptimizationAlgorithmRegistry {
    private final Map<String, IOptimizationAlgorithm> algorithmsByKey;

    public OptimizationAlgorithmRegistry(List<IOptimizationAlgorithm> algorithms) {
        this.algorithmsByKey = algorithms.stream()
                .collect(Collectors.toUnmodifiableMap(
                        algorithm -> algorithm.descriptor().key(),
                        Function.identity()
                ));
    }

    @Override
    public IOptimizationAlgorithm resolve(String algorithmKey) {
        String resolvedKey = algorithmKey == null || algorithmKey.isBlank()
                ? OptimizationAlgorithmKeys.GREEDY_BALANCED
                : algorithmKey;
        IOptimizationAlgorithm algorithm = algorithmsByKey.get(resolvedKey);
        if (algorithm == null) {
            throw new IllegalArgumentException("Unsupported optimization algorithm: " + resolvedKey);
        }
        return algorithm;
    }
}
