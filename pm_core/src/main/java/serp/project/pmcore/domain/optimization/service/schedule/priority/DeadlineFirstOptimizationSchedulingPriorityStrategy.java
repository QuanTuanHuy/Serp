/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.schedule.priority;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;

import java.util.Comparator;
import java.util.Map;

@Service
public class DeadlineFirstOptimizationSchedulingPriorityStrategy implements OptimizationSchedulingPriorityStrategy {
    @Override
    public Comparator<Long> readyWorkItemComparator(Map<Long, OptimizationWorkItem> itemById) {
        return Comparator.<Long, Long>comparing(
                        id -> itemById.get(id).workItem().getDueDate(),
                        Comparator.nullsLast(Long::compareTo)
                )
                .thenComparing(id -> itemById.get(id).criticalPath(), Comparator.reverseOrder())
                .thenComparing(id -> itemById.get(id).priorityScore().score(), Comparator.reverseOrder())
                .thenComparing(id -> itemById.get(id).duration().durationMillis())
                .thenComparing(id -> itemById.get(id).workItem().getRank(), Comparator.nullsLast(String::compareTo))
                .thenComparing(id -> id);
    }
}
