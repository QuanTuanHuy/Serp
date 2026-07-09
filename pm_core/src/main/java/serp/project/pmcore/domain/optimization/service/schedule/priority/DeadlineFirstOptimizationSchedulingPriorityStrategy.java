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
 * Deadline-first scheduling priority strategy.
 *
 * This strategy prioritizes tasks with closer deadlines to mitigate late risk. It uses
 * a multi-tier comparator chain:
 * 1. Due Date (earlier due dates first, nulls last).
 * 2. Critical Path (tasks on the critical path are scheduled first).
 * 3. Priority Score (higher score first).
 * 4. Duration (shorter tasks first - Shortest Job First).
 * 5. Rank (alphabetical rank first, nulls last).
 * 6. Work Item ID (deterministic tie-breaker).
 */
@Service
public class DeadlineFirstOptimizationSchedulingPriorityStrategy implements OptimizationSchedulingPriorityStrategy {
    
    /**
     * {@inheritDoc}
     * Returns a multi-tier comparator chain to prioritize tasks by earlier due dates, followed by
     * critical path status, high priority score, shorter durations, ranks, and IDs.
     */
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
