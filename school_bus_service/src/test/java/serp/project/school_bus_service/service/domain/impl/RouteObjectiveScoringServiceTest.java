package serp.project.school_bus_service.service.domain.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.SchoolBusServiceApplication;
import serp.project.school_bus_service.dto.response.ObjectiveScoreResponse;
import serp.project.school_bus_service.entity.*;
import serp.project.school_bus_service.enums.*;
import serp.project.school_bus_service.service.IRouteObjectiveScoringService;
import serp.project.school_bus_service.repository.RoutePlanningSessionRepository;
import serp.project.school_bus_service.repository.RoutePlanningIssueRepository;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SchoolBusServiceApplication.class)
@Transactional
public class RouteObjectiveScoringServiceTest {

    static {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    private static final Long TENANT_ID = 1L;

    @Autowired
    private SchoolBusScenarioTestDataFactory dataFactory;

    @Autowired
    private IRouteObjectiveScoringService objectiveScoringService;

    @Autowired
    private RoutePlanningSessionRepository sessionRepository;

    @Autowired
    private RoutePlanningIssueRepository issueRepository;

    @Test
    public void testRouteObjectiveFormula() {
        // 1. Setup config weights
        dataFactory.createAppConfig("ROUTING_WEIGHT_DISTANCE", "1.0");
        dataFactory.createAppConfig("ROUTING_WEIGHT_DURATION", "1.0");
        dataFactory.createAppConfig("ROUTING_WEIGHT_WAIT_TIME", "0.5");
        dataFactory.createAppConfig("ROUTING_WEIGHT_WARNING_ISSUE", "50.0");
        dataFactory.createAppConfig("ROUTING_WEIGHT_BLOCKING_ISSUE", "10000.0");

        // 2. Setup entities
        SchoolEntity school = dataFactory.createSchool("S_O1", "Obj School One", 21.0285, 105.8542, TENANT_ID);
        SchoolScheduleEntity schedule = dataFactory.createSchoolSchedule(school, "Morning Shift", LocalTime.of(7, 30), LocalTime.of(12, 0), TENANT_ID);
        DepotEntity depot = dataFactory.createDepot("D_O1", "Obj Depot One", 21.0385, 105.8642, TENANT_ID);
        PickupPointEntity pickup = dataFactory.createPickupPoint("P_O1", "Obj Pickup One", "PICKUP_DROPOFF", 21.0185, 105.8442, TENANT_ID);

        RoutePlanningSessionEntity session = dataFactory.createRoutePlanningSession(school, schedule, RouteDirection.OUTBOUND, TENANT_ID);
        RoutePlanEntity route = dataFactory.createRoutePlan(school, session, RouteDirection.OUTBOUND, TENANT_ID);
        
        route.setPlannedDistanceKm(10.0);
        route.setPlannedDurationMin(20);
        route.setPlannedStudentCount(1);
        route.setAssignedBusCapacity(30);

        // Stops
        RouteStopEntity stopDepot = dataFactory.createRouteStop(route, depot, RouteLocationType.DEPOT, RouteStopPurpose.START_TERMINAL, 0, TENANT_ID);
        stopDepot.setPlannedDepartureTime(LocalTime.of(6, 45));

        RouteStopEntity stopPickup = dataFactory.createRouteStop(route, pickup, RouteLocationType.PICKUP_POINT, RouteStopPurpose.PICKUP, 1, TENANT_ID);
        stopPickup.setPlannedArrivalTime(LocalTime.of(6, 55));
        stopPickup.setPlannedDepartureTime(LocalTime.of(6, 56));
        stopPickup.setEstimatedStudentCount(1);

        RouteStopEntity stopSchool = dataFactory.createRouteStop(route, school, RouteLocationType.SCHOOL, RouteStopPurpose.END_TERMINAL, 2, TENANT_ID);
        stopSchool.setPlannedArrivalTime(LocalTime.of(7, 0));

        // Create 1 WARNING issue
        RoutePlanningIssueEntity issue = new RoutePlanningIssueEntity();
        issue.markCreated(TENANT_ID, "TEST");
        issue.setRoute(route);
        issue.setIssueType("BUS_NOT_ASSIGNED_CAPACITY_UNKNOWN");
        issue.setSeverity(PlanningIssueSeverity.WARNING);
        issue.setMessage("Warning issue");
        issue.setIsResolved(false);
        issueRepository.save(issue);

        // 3. Action
        ObjectiveScoreResponse score = objectiveScoringService.calculateRouteScore(route.getId(), TENANT_ID);

        // 4. Assert
        assertNotNull(score);
        // Objective Value = distance (10 * 1.0) + duration (20 * 1.0) + waitTime (4 * 0.5) + warning (1 * 50.0) = 82.0
        assertEquals(new java.math.BigDecimal("82.00"), score.getObjectiveValue());
        
        // Display Score = 100 / (1 + 82 / 500) = 85.91
        assertEquals(new java.math.BigDecimal("85.91"), score.getDisplayScore());
        assertTrue(score.getFeasible(), "Should be feasible when there are no blocking issues");
    }

    @Test
    public void testSessionUnassignedPenalty() {
        // 1. Setup config weights
        dataFactory.createAppConfig("ROUTING_WEIGHT_UNASSIGNED", "1000.0");

        // 2. Setup session with 10 unassigned students
        SchoolEntity school = dataFactory.createSchool("S_O2", "Obj School Two", 21.0285, 105.8542, TENANT_ID);
        SchoolScheduleEntity schedule = dataFactory.createSchoolSchedule(school, "Morning Shift", LocalTime.of(7, 30), LocalTime.of(12, 0), TENANT_ID);
        RoutePlanningSessionEntity session = dataFactory.createRoutePlanningSession(school, schedule, RouteDirection.OUTBOUND, TENANT_ID);
        
        session.setTotalUnassignedStudents(10);
        sessionRepository.save(session);

        // 3. Action
        ObjectiveScoreResponse score = objectiveScoringService.calculateSolutionScore(session.getId(), TENANT_ID);

        // 4. Assert
        assertNotNull(score);
        assertEquals(new java.math.BigDecimal("10000.00"), score.getUnassignedCost());
        assertFalse(score.getFeasible(), "Solution with unassigned students should not be feasible");
    }

    @Test
    public void testBlockingIssuePenalty() {
        // 1. Setup config weights
        dataFactory.createAppConfig("ROUTING_WEIGHT_BLOCKING_ISSUE", "10000.0");

        // 2. Setup route with 1 blocking issue
        SchoolEntity school = dataFactory.createSchool("S_O3", "Obj School Three", 21.0285, 105.8542, TENANT_ID);
        SchoolScheduleEntity schedule = dataFactory.createSchoolSchedule(school, "Morning Shift", LocalTime.of(7, 30), LocalTime.of(12, 0), TENANT_ID);
        RoutePlanningSessionEntity session = dataFactory.createRoutePlanningSession(school, schedule, RouteDirection.OUTBOUND, TENANT_ID);
        RoutePlanEntity route = dataFactory.createRoutePlan(school, session, RouteDirection.OUTBOUND, TENANT_ID);

        RoutePlanningIssueEntity issue = new RoutePlanningIssueEntity();
        issue.markCreated(TENANT_ID, "TEST");
        issue.setRoute(route);
        issue.setIssueType("MISSING_TIME_WINDOW");
        issue.setSeverity(PlanningIssueSeverity.BLOCKING);
        issue.setMessage("Blocking issue");
        issue.setIsResolved(false);
        issueRepository.save(issue);

        // 3. Action
        ObjectiveScoreResponse score = objectiveScoringService.calculateRouteScore(route.getId(), TENANT_ID);

        // 4. Assert
        assertNotNull(score);
        assertEquals(new java.math.BigDecimal("10000.00"), score.getBlockingIssueCost());
        assertFalse(score.getFeasible(), "Route with blocking issues should not be feasible");
    }
}
