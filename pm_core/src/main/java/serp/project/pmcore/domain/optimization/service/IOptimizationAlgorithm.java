/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmDescriptor;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;

public interface IOptimizationAlgorithm {
    OptimizationAlgorithmDescriptor descriptor();

    OptimizationSolution solve(OptimizationProblem problem);
}
