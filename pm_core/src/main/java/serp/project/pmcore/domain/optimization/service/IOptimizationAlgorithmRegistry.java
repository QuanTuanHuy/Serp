/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

public interface IOptimizationAlgorithmRegistry {
    IOptimizationAlgorithm resolve(String algorithmKey);
}
