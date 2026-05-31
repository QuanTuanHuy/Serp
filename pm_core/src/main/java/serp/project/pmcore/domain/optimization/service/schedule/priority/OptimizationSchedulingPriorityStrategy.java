/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.schedule.priority;

import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;

import java.util.Comparator;
import java.util.Map;

public interface OptimizationSchedulingPriorityStrategy {
    Comparator<Long> readyWorkItemComparator(Map<Long, OptimizationWorkItem> itemById);
}
