package serp.project.school_bus_service.service.algorithm.impl;

import org.springframework.stereotype.Service;
import serp.project.school_bus_service.entity.PickupPointEntity;

import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.service.algorithm.GreedyPlanInput;
import serp.project.school_bus_service.service.algorithm.GreedyPlanResult;
import serp.project.school_bus_service.service.algorithm.GreedyRouteBatch;
import serp.project.school_bus_service.service.algorithm.GreedyStopAssignment;
import serp.project.school_bus_service.service.algorithm.IGreedyRoutePlanningService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure greedy route planning algorithm.
 *
 * <p>Groups subscriptions by pickup/dropoff point (based on direction), sorts by
 * student count descending, then partitions into capacity-bounded batches.
 * Does NOT access any repository or external service.
 */
@Service("greedyAlgorithmService")
public class GreedyRoutePlanningService implements IGreedyRoutePlanningService {

    public GreedyRoutePlanningService() {
    }

    @Override
    public GreedyPlanResult buildPlan(GreedyPlanInput input) {
        List<StudentSubscriptionEntity> unassigned = new ArrayList<>();
        Map<Long, PointAggregate> pointMap = new LinkedHashMap<>();

        for (StudentSubscriptionEntity sub : input.getEligibleSubscriptions()) {
            PickupPointEntity point = input.isOutbound()
                    ? sub.getPickupPoint()
                    : sub.getDropoffPoint();
            if (point == null) {
                unassigned.add(sub);
                continue;
            }
            pointMap.compute(point.getId(), (id, agg) -> {
                if (agg == null) return new PointAggregate(point);
                return agg;
            }).addStudent(sub);
        }

        List<PointAggregate> sorted = pointMap.values().stream()
                .sorted(Comparator
                        .comparingInt(PointAggregate::studentCount).reversed()
                        .thenComparingLong(a -> a.point.getId()))
                .toList();

        // Partition stops into capacity-bounded batches
        List<GreedyRouteBatch> batches = new ArrayList<>();
        int batchIndex = 0;
        List<GreedyStopAssignment> currentBatch = new ArrayList<>();
        int currentCapacity = 0;
        int stopOrder = 1;

        for (PointAggregate agg : sorted) {
            int needed = agg.studentCount();
            if (!currentBatch.isEmpty() && currentCapacity + needed > input.getBusCapacity()) {
                // Close current batch
                batches.add(new GreedyRouteBatch(batchIndex++, List.copyOf(currentBatch)));
                currentBatch = new ArrayList<>();
                currentCapacity = 0;
                stopOrder = 1;
            }
            currentBatch.add(new GreedyStopAssignment(agg.point, List.copyOf(agg.students), stopOrder++));
            currentCapacity += needed;
        }
        if (!currentBatch.isEmpty()) {
            batches.add(new GreedyRouteBatch(batchIndex, List.copyOf(currentBatch)));
        }

        return new GreedyPlanResult(batches, unassigned);
    }

    // ── Private aggregate ─────────────────────────────────────────────────────

    private static final class PointAggregate {
        private final PickupPointEntity point;
        private final List<StudentSubscriptionEntity> students = new ArrayList<>();

        private PointAggregate(PickupPointEntity point) {
            this.point = point;
        }

        private void addStudent(StudentSubscriptionEntity sub) {
            students.add(sub);
        }

        private int studentCount() {
            return students.size();
        }
    }
}

