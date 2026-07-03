/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import serp.project.pmcore.domain.optimization.model.OptimizationBuilderInput;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;

/**
 * Service interface for compiling and building the complete project data model required by the optimization engine.
 * The builder aggregates project metadata, work items, issue links (dependencies), resource capacities,
 * and skill requirements into a unified model representation.
 */
public interface IOptimizationProjectModelBuilder {

    /**
     * Compiles raw project data and configurations into a structured model suitable for running the optimization solver.
     *
     * @param input the input parameters containing project ID, selected work items, planning window, and run intent
     * @return the compiled {@link OptimizationProjectModel}
     * @throws IllegalArgumentException if the project is not found or invalid inputs are provided
     */
    OptimizationProjectModel build(OptimizationBuilderInput input);
}
