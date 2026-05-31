package serp.project.school_bus_service.service.algorithm;

/**
 * Pure greedy route planning algorithm.
 *
 * <p>Groups eligible subscriptions by pickup/dropoff point, sorts by student count
 * (descending), partitions into capacity-bounded batches, and returns the plan.
 * Does NOT access any repository or external service.
 */
public interface IGreedyRoutePlanningService {

    /**
     * Compute a greedy route plan from the given input.
     *
     * @param input eligible subscriptions, direction flag, and bus capacity
     * @return batches (one per route) plus unassigned subscriptions
     */
    GreedyPlanResult buildPlan(GreedyPlanInput input);
}
