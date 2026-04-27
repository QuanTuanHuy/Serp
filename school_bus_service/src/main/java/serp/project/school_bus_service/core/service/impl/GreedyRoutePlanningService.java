package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.core.service.IRoutePlanningService;
import serp.project.school_bus_service.core.service.IStudentSubscriptionService;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStopType;
import serp.project.school_bus_service.infrastructure.store.model.PickupPointEntity;
import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.infrastructure.store.model.RouteStopEntity;
import serp.project.school_bus_service.infrastructure.store.model.StudentSubscriptionEntity;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GreedyRoutePlanningService implements IRoutePlanningService {

    private final IStudentSubscriptionService subscriptionService;

    @Override
    public List<RouteStopEntity> generateGreedyStops(RoutePlanEntity route, Long tenantId) {
        Map<Long, PickupPointAggregate> pickupPointMap = new LinkedHashMap<>();
        for (StudentSubscriptionEntity subscription : subscriptionService.findEligibleSubscriptions(
                route.getSchool().getId(), route.getRouteDirection(), route.getServiceDate(), tenantId)) {
            PickupPointEntity point = route.getRouteDirection() == RouteDirection.RETURN
                    ? subscription.getDropoffPoint()
                    : subscription.getPickupPoint();
            if (point == null) {
                continue;
            }

            pickupPointMap.compute(point.getId(), (key, existing) -> {
                if (existing == null) {
                    return new PickupPointAggregate(point, 1);
                }
                existing.increment();
                return existing;
            });
        }

        List<PickupPointAggregate> sortedPickupPoints = pickupPointMap.values().stream()
                .sorted(Comparator
                        .comparingInt(PickupPointAggregate::getStudentCount).reversed()
                        .thenComparing(aggregate -> aggregate.getPickupPoint().getId()))
                .toList();

        List<RouteStopEntity> stops = new ArrayList<>();
        LocalTime nextArrival = route.getShiftType().name().equals("MORNING")
                ? LocalTime.of(6, 30)
                : LocalTime.of(15, 0);
        int order = 1;
        for (PickupPointAggregate aggregate : sortedPickupPoints) {
            RouteStopEntity stop = new RouteStopEntity();
            stop.markCreated(tenantId, "SYSTEM");
            stop.setRoute(route);
            stop.setPickupPoint(aggregate.getPickupPoint());
            stop.setStopType(route.getRouteDirection() == RouteDirection.RETURN
                    ? RouteStopType.DROPOFF
                    : RouteStopType.PICKUP);
            stop.setStopOrder(order++);
            stop.setEstimatedStudentCount(aggregate.getStudentCount());
            stop.setPlannedArrivalTime(nextArrival);
            stop.setPlannedDepartureTime(nextArrival.plusMinutes(5));
            stops.add(stop);
            nextArrival = nextArrival.plusMinutes(10);
        }

        return stops;
    }

    private static final class PickupPointAggregate {
        private final PickupPointEntity pickupPoint;
        private int studentCount;

        private PickupPointAggregate(PickupPointEntity pickupPoint, int studentCount) {
            this.pickupPoint = pickupPoint;
            this.studentCount = studentCount;
        }

        private PickupPointEntity getPickupPoint() {
            return pickupPoint;
        }

        private int getStudentCount() {
            return studentCount;
        }

        private void increment() {
            this.studentCount++;
        }
    }
}
