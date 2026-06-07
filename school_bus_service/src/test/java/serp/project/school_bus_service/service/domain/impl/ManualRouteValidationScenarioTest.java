package serp.project.school_bus_service.service.domain.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.SchoolBusServiceApplication;
import serp.project.school_bus_service.dto.response.PlanningIssueResponse;
import serp.project.school_bus_service.dto.response.RouteManualValidationResponse;
import serp.project.school_bus_service.entity.*;
import serp.project.school_bus_service.enums.*;
import serp.project.school_bus_service.service.IRouteManualValidationService;
import serp.project.school_bus_service.service.IRoutePlanningIssueService;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SchoolBusServiceApplication.class)
@Transactional
public class ManualRouteValidationScenarioTest {

    static {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    private static final Long TENANT_ID = 1L;

    @Autowired
    private SchoolBusScenarioTestDataFactory dataFactory;

    @Autowired
    private IRouteManualValidationService validationService;

    @Autowired
    private IRoutePlanningIssueService issueService;

    @Test
    public void testValidOutboundRoute() {
        // 1. Setup entities
        SchoolEntity school = dataFactory.createSchool("S1", "School One", 21.0285, 105.8542, TENANT_ID);
        SchoolScheduleEntity schedule = dataFactory.createSchoolSchedule(school, "Morning Shift", LocalTime.of(7, 30), LocalTime.of(12, 0), TENANT_ID);
        DepotEntity depot = dataFactory.createDepot("D1", "Depot One", 21.0385, 105.8642, TENANT_ID);
        PickupPointEntity pickup = dataFactory.createPickupPoint("P1", "Pickup One", "PICKUP_DROPOFF", 21.0185, 105.8442, TENANT_ID);

        SchoolPickupPointEntity spp = dataFactory.createSchoolPickupPoint(school, pickup, TENANT_ID);
        dataFactory.createSchoolPickupPointWindow(spp, schedule, "PICKUP_TO_SCHOOL", LocalTime.of(6, 0), LocalTime.of(8, 0), TENANT_ID);

        RoutePlanningSessionEntity session = dataFactory.createRoutePlanningSession(school, schedule, RouteDirection.OUTBOUND, TENANT_ID);
        RoutePlanEntity route = dataFactory.createRoutePlan(school, session, RouteDirection.OUTBOUND, TENANT_ID);
        route.setStartDepot(depot);
        route.setEndSchool(school);

        // Stops
        RouteStopEntity stopDepot = dataFactory.createRouteStop(route, depot, RouteLocationType.DEPOT, RouteStopPurpose.START_TERMINAL, 0, TENANT_ID);
        RouteStopEntity stopPickup = dataFactory.createRouteStop(route, pickup, RouteLocationType.PICKUP_POINT, RouteStopPurpose.PICKUP, 1, TENANT_ID);
        RouteStopEntity stopSchool = dataFactory.createRouteStop(route, school, RouteLocationType.SCHOOL, RouteStopPurpose.END_TERMINAL, 2, TENANT_ID);

        ParentProfileEntity parent = dataFactory.createParentProfile("Parent One", TENANT_ID);
        StudentEntity student = dataFactory.createStudent(school, parent, "Student One", TENANT_ID);
        StudentSubscriptionEntity sub = dataFactory.createStudentSubscription(student, school, pickup, null, TripOption.MORNING, TENANT_ID);

        // Assign student to the stop
        dataFactory.createRoutePlanStudent(route, stopPickup, student, sub, TENANT_ID);

        // 2. Action
        RouteManualValidationResponse response = validationService.validateRoute(route.getId(), TENANT_ID);

        // Debug print
        System.out.println("=== DEBUG VALID OUTBOUND ROUTE ISSUES ===");
        if (response.getIssues() != null) {
            response.getIssues().forEach(issue -> System.out.println("ISSUE: " + issue.getIssueType() + " - " + issue.getMessage()));
        }

        // 3. Assert
        assertTrue(response.isValid(), "Route should be valid without blocking issues");
        assertEquals(0, response.getBlockingIssueCount());
        
        // Assert timelines have been calculated
        assertNotNull(stopDepot.getPlannedDepartureTime(), "Planned departure time should be set");
        assertNotNull(stopPickup.getPlannedArrivalTime(), "Planned arrival time should be set");
        assertNotNull(stopSchool.getPlannedArrivalTime(), "Planned school arrival time should be set");
    }

    @Test
    public void testMissingPickupWindow() {
        // 1. Setup entities - WITHOUT window
        SchoolEntity school = dataFactory.createSchool("S2", "School Two", 21.0285, 105.8542, TENANT_ID);
        SchoolScheduleEntity schedule = dataFactory.createSchoolSchedule(school, "Morning Shift", LocalTime.of(7, 30), LocalTime.of(12, 0), TENANT_ID);
        DepotEntity depot = dataFactory.createDepot("D2", "Depot Two", 21.0385, 105.8642, TENANT_ID);
        PickupPointEntity pickup = dataFactory.createPickupPoint("P2", "Pickup Two", "PICKUP_DROPOFF", 21.0185, 105.8442, TENANT_ID);

        // Link but DO NOT create window
        dataFactory.createSchoolPickupPoint(school, pickup, TENANT_ID);

        RoutePlanningSessionEntity session = dataFactory.createRoutePlanningSession(school, schedule, RouteDirection.OUTBOUND, TENANT_ID);
        RoutePlanEntity route = dataFactory.createRoutePlan(school, session, RouteDirection.OUTBOUND, TENANT_ID);
        route.setStartDepot(depot);
        route.setEndSchool(school);

        // Stops
        dataFactory.createRouteStop(route, depot, RouteLocationType.DEPOT, RouteStopPurpose.START_TERMINAL, 0, TENANT_ID);
        dataFactory.createRouteStop(route, pickup, RouteLocationType.PICKUP_POINT, RouteStopPurpose.PICKUP, 1, TENANT_ID);
        dataFactory.createRouteStop(route, school, RouteLocationType.SCHOOL, RouteStopPurpose.END_TERMINAL, 2, TENANT_ID);

        // 2. Action
        RouteManualValidationResponse response = validationService.validateRoute(route.getId(), TENANT_ID);

        // 3. Assert
        assertFalse(response.isValid(), "Route should be invalid due to missing window");
        assertTrue(response.getBlockingIssueCount() > 0);
        
        boolean hasMissingWindowIssue = response.getIssues().stream()
                .anyMatch(i -> "MISSING_TIME_WINDOW".equals(i.getIssueType()));
        assertTrue(hasMissingWindowIssue, "Should report MISSING_TIME_WINDOW issue");
    }

    @Test
    public void testInvalidTerminal() {
        // 1. Setup entities - Route starts at Pickup Point instead of Depot (Outbound)
        SchoolEntity school = dataFactory.createSchool("S3", "School Three", 21.0285, 105.8542, TENANT_ID);
        SchoolScheduleEntity schedule = dataFactory.createSchoolSchedule(school, "Morning Shift", LocalTime.of(7, 30), LocalTime.of(12, 0), TENANT_ID);
        PickupPointEntity pickup = dataFactory.createPickupPoint("P3", "Pickup Three", "PICKUP_DROPOFF", 21.0185, 105.8442, TENANT_ID);

        RoutePlanningSessionEntity session = dataFactory.createRoutePlanningSession(school, schedule, RouteDirection.OUTBOUND, TENANT_ID);
        RoutePlanEntity route = dataFactory.createRoutePlan(school, session, RouteDirection.OUTBOUND, TENANT_ID);
        // Start is SCHOOL, end is SCHOOL (Invalid outbound structure - should start with depot)
        route.setStartLocationType(RouteLocationType.SCHOOL);
        route.setStartSchool(school);
        route.setStartDepot(null);
        route.setEndLocationType(RouteLocationType.SCHOOL);
        route.setEndSchool(school);
        route.setEndDepot(null);

        // Stops: Start terminal is school instead of depot
        dataFactory.createRouteStop(route, school, RouteLocationType.SCHOOL, RouteStopPurpose.START_TERMINAL, 0, TENANT_ID);
        dataFactory.createRouteStop(route, pickup, RouteLocationType.PICKUP_POINT, RouteStopPurpose.PICKUP, 1, TENANT_ID);
        dataFactory.createRouteStop(route, school, RouteLocationType.SCHOOL, RouteStopPurpose.END_TERMINAL, 2, TENANT_ID);

        // 2. Action
        RouteManualValidationResponse response = validationService.validateRoute(route.getId(), TENANT_ID);

        // 3. Assert
        assertFalse(response.isValid(), "Route terminal should be invalid");
        assertTrue(response.getBlockingIssueCount() > 0);
        
        boolean hasInvalidTerminalIssue = response.getIssues().stream()
                .anyMatch(i -> "INVALID_ROUTE_TERMINAL".equals(i.getIssueType()));
        assertTrue(hasInvalidTerminalIssue, "Should report INVALID_ROUTE_TERMINAL issue");
    }

    @Test
    public void testDuplicateStudent() {
        // 1. Setup entities
        SchoolEntity school = dataFactory.createSchool("S4", "School Four", 21.0285, 105.8542, TENANT_ID);
        SchoolScheduleEntity schedule = dataFactory.createSchoolSchedule(school, "Morning Shift", LocalTime.of(7, 30), LocalTime.of(12, 0), TENANT_ID);
        DepotEntity depot = dataFactory.createDepot("D4", "Depot Four", 21.0385, 105.8642, TENANT_ID);
        PickupPointEntity pickup = dataFactory.createPickupPoint("P4", "Pickup Four", "PICKUP_DROPOFF", 21.0185, 105.8442, TENANT_ID);

        SchoolPickupPointEntity spp = dataFactory.createSchoolPickupPoint(school, pickup, TENANT_ID);
        dataFactory.createSchoolPickupPointWindow(spp, schedule, "PICKUP_TO_SCHOOL", LocalTime.of(6, 0), LocalTime.of(7, 0), TENANT_ID);

        RoutePlanningSessionEntity session = dataFactory.createRoutePlanningSession(school, schedule, RouteDirection.OUTBOUND, TENANT_ID);
        
        // Route 1
        RoutePlanEntity route1 = dataFactory.createRoutePlan(school, session, RouteDirection.OUTBOUND, TENANT_ID);
        route1.setStartDepot(depot);
        route1.setEndSchool(school);
        RouteStopEntity r1Depot = dataFactory.createRouteStop(route1, depot, RouteLocationType.DEPOT, RouteStopPurpose.START_TERMINAL, 0, TENANT_ID);
        RouteStopEntity r1Pickup = dataFactory.createRouteStop(route1, pickup, RouteLocationType.PICKUP_POINT, RouteStopPurpose.PICKUP, 1, TENANT_ID);
        RouteStopEntity r1School = dataFactory.createRouteStop(route1, school, RouteLocationType.SCHOOL, RouteStopPurpose.END_TERMINAL, 2, TENANT_ID);

        // Route 2
        RoutePlanEntity route2 = dataFactory.createRoutePlan(school, session, RouteDirection.OUTBOUND, TENANT_ID);
        route2.setStartDepot(depot);
        route2.setEndSchool(school);
        RouteStopEntity r2Depot = dataFactory.createRouteStop(route2, depot, RouteLocationType.DEPOT, RouteStopPurpose.START_TERMINAL, 0, TENANT_ID);
        RouteStopEntity r2Pickup = dataFactory.createRouteStop(route2, pickup, RouteLocationType.PICKUP_POINT, RouteStopPurpose.PICKUP, 1, TENANT_ID);
        RouteStopEntity r2School = dataFactory.createRouteStop(route2, school, RouteLocationType.SCHOOL, RouteStopPurpose.END_TERMINAL, 2, TENANT_ID);

        // Create student & subscription
        ParentProfileEntity parent = dataFactory.createParentProfile("Parent Four", TENANT_ID);
        StudentEntity student = dataFactory.createStudent(school, parent, "Student Four", TENANT_ID);
        StudentSubscriptionEntity sub = dataFactory.createStudentSubscription(student, school, pickup, null, TripOption.MORNING, TENANT_ID);

        // Assign student to Route 1 AND Route 2 (Duplicate assignment in session)
        dataFactory.createRoutePlanStudent(route1, r1Pickup, student, sub, TENANT_ID);
        dataFactory.createRoutePlanStudent(route2, r2Pickup, student, sub, TENANT_ID);

        // 2. Action - validate Route 2 (should detect student is already in Route 1)
        RouteManualValidationResponse response = validationService.validateRoute(route2.getId(), TENANT_ID);

        // 3. Assert
        assertFalse(response.isValid(), "Route should be invalid due to duplicate student");
        assertTrue(response.getBlockingIssueCount() > 0);
        
        boolean hasDuplicateIssue = response.getIssues().stream()
                .anyMatch(i -> "STUDENT_ALREADY_ASSIGNED_TO_ROUTE".equals(i.getIssueType()));
        assertTrue(hasDuplicateIssue, "Should report STUDENT_ALREADY_ASSIGNED_TO_ROUTE issue");
    }
}
