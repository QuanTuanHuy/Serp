/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.schedule.priority;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;

import java.util.Comparator;
import java.util.Map;

/**
 * Balanced scheduling priority strategy.
 *
 * This strategy balances multiple project scheduling metrics. It prioritizes work items using
 * a multi-tier comparator chain:
 * 1. Critical Path (tasks on the critical path are scheduled first).
 * 2. Priority Score (higher score first).
 * 3. Due Date (earlier due dates first, nulls last).
 * 4. Duration (shorter tasks first - Shortest Job First).
 * 5. Rank (alphabetical rank first, nulls last).
 * 6. Work Item ID (deterministic tie-breaker).
 */
@Service
public class BalancedOptimizationSchedulingPriorityStrategy implements OptimizationSchedulingPriorityStrategy {
    
    /**
     * {@inheritDoc}
     * Returns a multi-tier comparator chain to prioritize tasks on the critical path, followed
     * by high priority score, earlier due dates, shorter durations, ranks, and IDs.
     */
    @Override
    public Comparator<Long> readyWorkItemComparator(Map<Long, OptimizationWorkItem> itemById) {
        return Comparator.<Long, Boolean>comparing(id -> itemById.get(id).criticalPath()).reversed()
                .thenComparing(id -> itemById.get(id).priorityScore().score(), Comparator.reverseOrder())
                .thenComparing(id -> itemById.get(id).workItem().getDueDate(), Comparator.nullsLast(Long::compareTo))
                .thenComparing(id -> itemById.get(id).duration().durationMillis())
                .thenComparing(id -> itemById.get(id).workItem().getRank(), Comparator.nullsLast(String::compareTo))
                .thenComparing(id -> id);
    }
}
