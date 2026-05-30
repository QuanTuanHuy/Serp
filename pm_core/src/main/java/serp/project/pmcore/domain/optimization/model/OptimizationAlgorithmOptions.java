/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.OptimizationMode;

public record OptimizationAlgorithmOptions(
        String algorithmKey,
        OptimizationMode mode,
        Boolean allowReassignment,
        Boolean allowScheduleChanges
) {
}
