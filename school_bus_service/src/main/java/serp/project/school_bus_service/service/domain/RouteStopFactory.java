package serp.project.school_bus_service.service.domain;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.entity.DepotEntity;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.enums.RouteStopPurpose;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralised factory for creating {@link RouteStopEntity} instances.
 *
 * <p>All stop-order assignment goes through here so that the constraint
 * {@code uk_school_bus_route_stop_order (route_id, stop_order)} is never violated.
 * No other class should set {@code stopOrder} directly.
 *
 * <p>Naming conventions:
 * <ul>
 *   <li>OUTBOUND: DEPOT START_TERMINAL → PICKUP middle stops → SCHOOL END_TERMINAL</li>
 *   <li>RETURN:   SCHOOL START_TERMINAL → DROPOFF middle stops → DEPOT END_TERMINAL</li>
 * </ul>
 */
@Component
public class RouteStopFactory {

    // ── Terminal builders ────────────────────────────────────────────────────

    /**
     * Builds a START_TERMINAL stop for the route.
     * OUTBOUND → DEPOT terminal.  RETURN → SCHOOL terminal.
     * Order is set by the caller via {@link #buildFullStopList}.
     */
    public RouteStopEntity buildStartTerminal(RoutePlanEntity route, Long tenantId, String actor) {
        boolean isOutbound = route.getRouteDirection() == RouteDirection.OUTBOUND;
        RouteStopEntity stop = newStop(route, tenantId, actor);
        stop.setStopPurpose(RouteStopPurpose.START_TERMINAL);
        if (isOutbound) {
            stop.setLocationType(RouteLocationType.DEPOT);
            stop.setDepot(route.getStartDepot());
        } else {
            stop.setLocationType(RouteLocationType.SCHOOL);
            stop.setSchool(route.getStartSchool());
        }
        return stop;
    }

    /**
     * Builds an END_TERMINAL stop for the route.
     * OUTBOUND → SCHOOL terminal.  RETURN → DEPOT terminal.
     */
    public RouteStopEntity buildEndTerminal(RoutePlanEntity route, Long tenantId, String actor) {
        boolean isOutbound = route.getRouteDirection() == RouteDirection.OUTBOUND;
        RouteStopEntity stop = newStop(route, tenantId, actor);
        stop.setStopPurpose(RouteStopPurpose.END_TERMINAL);
        if (isOutbound) {
            stop.setLocationType(RouteLocationType.SCHOOL);
            stop.setSchool(route.getEndSchool());
        } else {
            stop.setLocationType(RouteLocationType.DEPOT);
            stop.setDepot(route.getEndDepot());
        }
        return stop;
    }

    /**
     * Builds a middle (PICKUP or DROPOFF) stop at a pickup point.
     */
    public RouteStopEntity buildMiddleStop(RoutePlanEntity route, PickupPointEntity pickupPoint,
                                            Long tenantId, String actor) {
        boolean isOutbound = route.getRouteDirection() == RouteDirection.OUTBOUND;
        RouteStopEntity stop = newStop(route, tenantId, actor);
        stop.setPickupPoint(pickupPoint);
        stop.setLocationType(RouteLocationType.PICKUP_POINT);
        stop.setStopPurpose(isOutbound ? RouteStopPurpose.PICKUP : RouteStopPurpose.DROPOFF);
        return stop;
    }

    // ── Full list builder ────────────────────────────────────────────────────

    /**
     * Combines terminals + middle stops into the canonical ordered list and assigns final
     * {@code stopOrder} values in a single pass — no subsequent shifting is ever needed.
     *
     * <pre>
     *   order 0        → START_TERMINAL (built fresh)
     *   order 1..N     → middle stops in supplied order
     *   order N+1      → END_TERMINAL (built fresh)
     * </pre>
     *
     * @param route       parent route (must already have start/end location set)
     * @param middleStops ordered list of PICKUP / DROPOFF stops (may be empty)
     * @param tenantId    tenant context for new stops
     * @param actor       audit actor string
     * @return full ordered stop list with stopOrder already set
     */
    public List<RouteStopEntity> buildFullStopList(RoutePlanEntity route,
                                                    List<RouteStopEntity> middleStops,
                                                    Long tenantId, String actor) {
        RouteStopEntity start = buildStartTerminal(route, tenantId, actor);
        RouteStopEntity end   = buildEndTerminal(route, tenantId, actor);

        List<RouteStopEntity> all = new ArrayList<>();
        all.add(start);
        all.addAll(middleStops);
        all.add(end);

        normalizeStopOrders(all);
        return all;
    }

    // ── Order normalisation ──────────────────────────────────────────────────

    /**
     * Assigns {@code stopOrder = 0, 1, 2, ...} to each stop in list order.
     * Call this whenever the stop list changes (insert, remove, reorder).
     *
     * @param stops mutable, ordered list — modified in-place
     */
    public void normalizeStopOrders(List<RouteStopEntity> stops) {
        for (int i = 0; i < stops.size(); i++) {
            stops.get(i).setStopOrder(i);
        }
    }

    // ── Validation ───────────────────────────────────────────────────────────

    /**
     * Asserts that terminal coordinate data is present for route geometry computation.
     * Throws {@link IllegalStateException} with a clear message if anything is missing.
     */
    public void validateTerminalCoordinates(RoutePlanEntity route) {
        boolean isOutbound = route.getRouteDirection() == RouteDirection.OUTBOUND;
        if (isOutbound) {
            requireCoords("start depot", route.getStartDepot() != null ? route.getStartDepot().getLatitude() : null,
                    route.getStartDepot() != null ? route.getStartDepot().getLongitude() : null);
            requireCoords("end school", route.getEndSchool() != null ? route.getEndSchool().getLatitude() : null,
                    route.getEndSchool() != null ? route.getEndSchool().getLongitude() : null);
        } else {
            requireCoords("start school", route.getStartSchool() != null ? route.getStartSchool().getLatitude() : null,
                    route.getStartSchool() != null ? route.getStartSchool().getLongitude() : null);
            requireCoords("end depot", route.getEndDepot() != null ? route.getEndDepot().getLatitude() : null,
                    route.getEndDepot() != null ? route.getEndDepot().getLongitude() : null);
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private RouteStopEntity newStop(RoutePlanEntity route, Long tenantId, String actor) {
        RouteStopEntity s = new RouteStopEntity();
        s.markCreated(tenantId, actor);
        s.setRoute(route);
        s.setEstimatedStudentCount(0);
        s.setPlannedBoardingCount(0);
        s.setPlannedDropoffCount(0);
        return s;
    }

    private void requireCoords(String name, Double lat, Double lon) {
        if (lat == null || lon == null) {
            throw new IllegalStateException("Missing coordinates for " + name
                    + " — cannot compute route geometry. Please configure coordinates first.");
        }
    }
}
