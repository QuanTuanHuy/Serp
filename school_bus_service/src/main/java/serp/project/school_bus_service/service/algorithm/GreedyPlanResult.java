package serp.project.school_bus_service.service.algorithm;

import serp.project.school_bus_service.entity.StudentSubscriptionEntity;

import java.util.List;

/** Full output of the greedy route planning algorithm. */
public final class GreedyPlanResult {

    private final List<GreedyRouteBatch> batches;
    /** Subscriptions that could not be assigned because they have no pickup/dropoff point. */
    private final List<StudentSubscriptionEntity> unassignedStudents;

    public GreedyPlanResult(List<GreedyRouteBatch> batches,
                            List<StudentSubscriptionEntity> unassignedStudents) {
        this.batches = batches;
        this.unassignedStudents = unassignedStudents;
    }

    public List<GreedyRouteBatch> getBatches() {
        return batches;
    }

    public List<StudentSubscriptionEntity> getUnassignedStudents() {
        return unassignedStudents;
    }
}
