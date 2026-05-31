package serp.project.school_bus_service.enums;

/**
 * Describes the functional role of a route stop.
 *
 * <ul>
 *   <li>{@link #START_TERMINAL} – First stop on the route (Depot for OUTBOUND, School for RETURN).</li>
 *   <li>{@link #PICKUP}         – A mid-route stop where students are picked up (OUTBOUND).</li>
 *   <li>{@link #DROPOFF}        – A mid-route stop where students are dropped off (RETURN).</li>
 *   <li>{@link #END_TERMINAL}   – Last stop on the route (School for OUTBOUND, Depot for RETURN).</li>
 * </ul>
 */
public enum RouteStopPurpose {
    START_TERMINAL,
    PICKUP,
    DROPOFF,
    END_TERMINAL;

    public boolean isTerminal() {
        return this == START_TERMINAL || this == END_TERMINAL;
    }
}
