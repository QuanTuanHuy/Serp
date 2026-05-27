package serp.project.school_bus_service.enums;

/**
 * RoutePlan lifecycle statuses.
 *
 * <p>This enum describes the planning-phase state of a route plan only.
 * Operational execution state (running, done) belongs to {@code TripStatus}
 * on the {@code TripExecution} aggregate.
 *
 * <ul>
 *   <li>DRAFT — just created, not yet planned
 *   <li>GENERATED — algorithm output, under review
 *   <li>REVIEWING — planner is reviewing the plan
 *   <li>PUBLISHED — approved, ready to dispatch
 *   <li>ASSIGNED — bus + driver assigned, ready to create trip
 *   <li>TRIP_CREATED — a TripExecution has been snapshot-locked from this plan
 *   <li>CANCELLED — plan was discarded
 * </ul>
 */
public enum RouteStatus {
    DRAFT,
    GENERATED,
    REVIEWING,
    PUBLISHED,
    ASSIGNED,
    TRIP_CREATED,
    CANCELLED
}
