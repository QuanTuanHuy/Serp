/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import serp.project.pmcore.domain.optimization.model.OptimizationBuilderInput;
import serp.project.pmcore.domain.optimization.model.OptimizationGenerationResult;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;

public interface IOptimizationRunGenerator {
    OptimizationGenerationResult generate(OptimizationProjectModel projectModel, OptimizationBuilderInput input);
}
