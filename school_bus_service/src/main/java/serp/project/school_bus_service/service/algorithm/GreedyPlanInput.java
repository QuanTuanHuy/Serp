package serp.project.school_bus_service.service.algorithm;

import serp.project.school_bus_service.entity.StudentSubscriptionEntity;

import java.util.List;

/** Input to the greedy route planning algorithm. */
public final class GreedyPlanInput {

    private final List<StudentSubscriptionEntity> eligibleSubscriptions;
    private final boolean outbound;
    private final int busCapacity;

    public GreedyPlanInput(List<StudentSubscriptionEntity> eligibleSubscriptions,
                           boolean outbound,
                           int busCapacity) {
        this.eligibleSubscriptions = eligibleSubscriptions;
        this.outbound = outbound;
        this.busCapacity = busCapacity;
    }

    public List<StudentSubscriptionEntity> getEligibleSubscriptions() {
        return eligibleSubscriptions;
    }

    public boolean isOutbound() {
        return outbound;
    }

    public int getBusCapacity() {
        return busCapacity;
    }
}
