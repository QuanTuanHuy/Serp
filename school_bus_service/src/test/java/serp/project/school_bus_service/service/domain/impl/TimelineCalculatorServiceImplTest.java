package serp.project.school_bus_service.service.domain.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import serp.project.school_bus_service.dto.response.RoutingRuntimeConfig;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStopPurpose;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.service.ISchoolPickupPointWindowService;
import serp.project.school_bus_service.service.IRoutePlanningIssueService;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.service.domain.IRoutingConfigResolver;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Disabled("TimelineCalculatorServiceImpl / IRoutingConfigResolver removed; test needs full rewrite after Phase 2")
class TimelineCalculatorServiceImplTest {

    private IRoutingConfigResolver routingConfigResolver;
    private ISchoolPickupPointWindowService windowService;
    private IRoutePlanningIssueService issueService;
    private IRoutePlanStudentService studentService;
    private TimelineCalculatorServiceImpl timelineCalculator;

    @BeforeEach
    void setUp() {
        routingConfigResolver = Mockito.mock(IRoutingConfigResolver.class);
        windowService = Mockito.mock(ISchoolPickupPointWindowService.class);
        issueService = Mockito.mock(IRoutePlanningIssueService.class);
        studentService = Mockito.mock(IRoutePlanStudentService.class);
        timelineCalculator = new TimelineCalculatorServiceImpl(routingConfigResolver, windowService, issueService, studentService);

        RoutingRuntimeConfig config = RoutingRuntimeConfig.builder()
                .averageSpeedKmph(25.0)
                .dwellTimeMinutes(2)
                .roadFactor(1.3)
                .osrmEnabled(true)
                .build();
        when(routingConfigResolver.resolve()).thenReturn(config);
    }

    @Test
    void testCalculateTimelineOutbound() {
        SchoolScheduleEntity schedule = new SchoolScheduleEntity();
        schedule.setArrivalDeadline(LocalTime.of(8, 0));

        RoutePlanningSessionEntity session = new RoutePlanningSessionEntity();
        session.setSchoolSchedule(schedule);

        RoutePlanEntity route = new RoutePlanEntity();
        route.setTenantId(1L);
        route.setRouteDirection(RouteDirection.OUTBOUND);
        route.setPlanningSession(session);

        RouteStopEntity depot = new RouteStopEntity();
        depot.setStopOrder(0);
        depot.setStopPurpose(RouteStopPurpose.START_TERMINAL);
        depot.setLocationType(RouteLocationType.DEPOT);

        RouteStopEntity pickup = new RouteStopEntity();
        pickup.setStopOrder(1);
        pickup.setStopPurpose(RouteStopPurpose.PICKUP);
        pickup.setLocationType(RouteLocationType.PICKUP_POINT);
        pickup.setEstimatedTravelTimeFromPrevious(10);

        RouteStopEntity school = new RouteStopEntity();
        school.setStopOrder(2);
        school.setStopPurpose(RouteStopPurpose.END_TERMINAL);
        school.setLocationType(RouteLocationType.SCHOOL);
        school.setEstimatedTravelTimeFromPrevious(15);

        List<RouteStopEntity> stops = Arrays.asList(depot, pickup, school);

        timelineCalculator.calculateTimeline(route, stops);

        assertEquals(LocalTime.of(8, 0), school.getPlannedArrivalTime());
        assertEquals(LocalTime.of(8, 0), school.getPlannedDepartureTime());

        assertEquals(LocalTime.of(7, 43), pickup.getPlannedArrivalTime());
        assertEquals(LocalTime.of(7, 45), pickup.getPlannedDepartureTime());

        assertEquals(LocalTime.of(7, 33), depot.getPlannedArrivalTime());
        assertEquals(LocalTime.of(7, 33), depot.getPlannedDepartureTime());

        assertEquals(LocalTime.of(7, 33), route.getPlannedStartTime());
        assertEquals(LocalTime.of(8, 0), route.getPlannedEndTime());
    }

    @Test
    void testCalculateTimelineReturn() {
        SchoolScheduleEntity schedule = new SchoolScheduleEntity();
        schedule.setDepartureTime(LocalTime.of(16, 0));

        RoutePlanningSessionEntity session = new RoutePlanningSessionEntity();
        session.setSchoolSchedule(schedule);

        RoutePlanEntity route = new RoutePlanEntity();
        route.setTenantId(1L);
        route.setRouteDirection(RouteDirection.RETURN);
        route.setPlanningSession(session);

        RouteStopEntity school = new RouteStopEntity();
        school.setStopOrder(0);
        school.setStopPurpose(RouteStopPurpose.START_TERMINAL);
        school.setLocationType(RouteLocationType.SCHOOL);

        RouteStopEntity dropoff = new RouteStopEntity();
        dropoff.setStopOrder(1);
        dropoff.setStopPurpose(RouteStopPurpose.DROPOFF);
        dropoff.setLocationType(RouteLocationType.PICKUP_POINT);
        dropoff.setEstimatedTravelTimeFromPrevious(12);

        RouteStopEntity depot = new RouteStopEntity();
        depot.setStopOrder(2);
        depot.setStopPurpose(RouteStopPurpose.END_TERMINAL);
        depot.setLocationType(RouteLocationType.DEPOT);
        depot.setEstimatedTravelTimeFromPrevious(8);

        List<RouteStopEntity> stops = Arrays.asList(school, dropoff, depot);

        timelineCalculator.calculateTimeline(route, stops);

        assertEquals(LocalTime.of(16, 0), school.getPlannedArrivalTime());
        assertEquals(LocalTime.of(16, 0), school.getPlannedDepartureTime());

        assertEquals(LocalTime.of(16, 12), dropoff.getPlannedArrivalTime());
        assertEquals(LocalTime.of(16, 14), dropoff.getPlannedDepartureTime());

        assertEquals(LocalTime.of(16, 22), depot.getPlannedArrivalTime());
        assertEquals(LocalTime.of(16, 22), depot.getPlannedDepartureTime());

        assertEquals(LocalTime.of(16, 0), route.getPlannedStartTime());
        assertEquals(LocalTime.of(16, 22), route.getPlannedEndTime());
    }
}
