/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.OptimizationCapability;

import java.util.Set;

public record OptimizationAlgorithmDescriptor(
        String key,
        String version,
        Set<OptimizationCapability> capabilities
) {
}
