/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import serp.project.pmcore.domain.optimization.model.OptimizationBuilderInput;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;

public interface IOptimizationProjectModelBuilder {
    OptimizationProjectModel build(OptimizationBuilderInput input);
}
