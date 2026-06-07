package serp.project.school_bus_service.service.domain.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.SchoolBusServiceApplication;
import serp.project.school_bus_service.dto.request.GreedyGenerateRequest;
import serp.project.school_bus_service.dto.response.GreedyGenerateResponse;
import serp.project.school_bus_service.entity.*;
import serp.project.school_bus_service.enums.*;
import serp.project.school_bus_service.service.IGreedyRouteGenerationService;
import serp.project.school_bus_service.repository.RoutePlanningSessionRepository;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SchoolBusServiceApplication.class)
@Transactional
public class GreedyRouteGenerationScenarioTest {

    static {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    private static final Long TENANT_ID = 1L;
    private static final Long ACTOR_ID = 999L;

    @Autowired
    private SchoolBusScenarioTestDataFactory dataFactory;

    @Autowired
    private IGreedyRouteGenerationService greedyRouteGenerationService;

    @Autowired
    private RoutePlanningSessionRepository sessionRepository;

    @Test
    public void testGreedyRouteGenerationSuccess() {
        // 1. Setup entities
        SchoolEntity school = dataFactory.createSchool("S_G1", "Greedy School One", 21.0285, 105.8542, TENANT_ID);
        SchoolScheduleEntity schedule = dataFactory.createSchoolSchedule(school, "Morning Shift", LocalTime.of(7, 30), LocalTime.of(12, 0), TENANT_ID);
        DepotEntity depot = dataFactory.createDepot("D_G1", "Greedy Depot One", 21.0385, 105.8642, TENANT_ID);
        PickupPointEntity pickup = dataFactory.createPickupPoint("P_G1", "Greedy Pickup One", "PICKUP_DROPOFF", 21.0185, 105.8442, TENANT_ID);

        SchoolPickupPointEntity spp = dataFactory.createSchoolPickupPoint(school, pickup, TENANT_ID);
        dataFactory.createSchoolPickupPointWindow(spp, schedule, "PICKUP_TO_SCHOOL", LocalTime.of(6, 0), LocalTime.of(8, 0), TENANT_ID);

        RoutePlanningSessionEntity session = dataFactory.createRoutePlanningSession(school, schedule, RouteDirection.OUTBOUND, TENANT_ID);

        ParentProfileEntity parent = dataFactory.createParentProfile("Parent Greedy One", TENANT_ID);
        StudentEntity student = dataFactory.createStudent(school, parent, "Student Greedy One", TENANT_ID);
        dataFactory.createStudentSubscription(student, school, pickup, null, TripOption.MORNING, TENANT_ID);

        // Prepare request
        GreedyGenerateRequest request = new GreedyGenerateRequest();
        request.setDepotId(depot.getId());
        request.setDefaultBusCapacity(30);

        // 2. Action
        System.out.println("=== DEBUG GREEDY GENERATION SETUP ===");
        System.out.println("School: " + school.getId() + ", Schedule: " + schedule.getId());
        
        GreedyGenerateResponse response = greedyRouteGenerationService.generateRoutes(session.getId(), request, TENANT_ID, ACTOR_ID);

        System.out.println("Generated routes count: " + (response.getRoutes() != null ? response.getRoutes().size() : "null"));
        System.out.println("Unassigned students count: " + response.getTotalUnassignedStudents());

        // 3. Assert
        assertNotNull(response);
        assertNotNull(response.getRoutes());
        assertFalse(response.getRoutes().isEmpty(), "Greedy generation should produce at least one route");
        assertEquals(0, response.getTotalUnassignedStudents(), "All eligible students should be assigned");

        // Verify session stats updated in DB
        RoutePlanningSessionEntity updatedSession = sessionRepository.findById(session.getId()).orElseThrow();
        assertEquals(1, updatedSession.getTotalRoutes());
        assertEquals(1, updatedSession.getTotalPlannedStudents());
        assertEquals(0, updatedSession.getTotalUnassignedStudents());
    }

    @Test
    public void testGreedyRouteGenerationRejectionDueToLateWindow() {
        // 1. Setup entities - narrow window (06:00 - 07:00) making it late
        SchoolEntity school = dataFactory.createSchool("S_G3", "Greedy School Three", 21.0285, 105.8542, TENANT_ID);
        SchoolScheduleEntity schedule = dataFactory.createSchoolSchedule(school, "Morning Shift", LocalTime.of(7, 30), LocalTime.of(12, 0), TENANT_ID);
        DepotEntity depot = dataFactory.createDepot("D_G3", "Greedy Depot Three", 21.0385, 105.8642, TENANT_ID);
        PickupPointEntity pickup = dataFactory.createPickupPoint("P_G3", "Greedy Pickup Three", "PICKUP_DROPOFF", 21.0185, 105.8442, TENANT_ID);

        SchoolPickupPointEntity spp = dataFactory.createSchoolPickupPoint(school, pickup, TENANT_ID);
        dataFactory.createSchoolPickupPointWindow(spp, schedule, "PICKUP_TO_SCHOOL", LocalTime.of(6, 0), LocalTime.of(7, 0), TENANT_ID);

        RoutePlanningSessionEntity session = dataFactory.createRoutePlanningSession(school, schedule, RouteDirection.OUTBOUND, TENANT_ID);

        ParentProfileEntity parent = dataFactory.createParentProfile("Parent Greedy Three", TENANT_ID);
        StudentEntity student = dataFactory.createStudent(school, parent, "Student Greedy Three", TENANT_ID);
        dataFactory.createStudentSubscription(student, school, pickup, null, TripOption.MORNING, TENANT_ID);

        // Prepare request
        GreedyGenerateRequest request = new GreedyGenerateRequest();
        request.setDepotId(depot.getId());
        request.setDefaultBusCapacity(30);

        // 2. Action
        GreedyGenerateResponse response = greedyRouteGenerationService.generateRoutes(session.getId(), request, TENANT_ID, ACTOR_ID);

        // 3. Assert - Should reject because window end is too early (07:00) vs arrival deadline (07:30)
        assertNotNull(response);
        assertTrue(response.getRoutes().isEmpty(), "No route should be generated because arrival is late");
        assertEquals(1, response.getTotalUnassignedStudents(), "Student remains unassigned due to late window rejection");
    }

    @Test
    public void testGreedyRouteGenerationRejectionDueToMissingWindow() {
        // 1. Setup entities - WITHOUT window
        SchoolEntity school = dataFactory.createSchool("S_G2", "Greedy School Two", 21.0285, 105.8542, TENANT_ID);
        SchoolScheduleEntity schedule = dataFactory.createSchoolSchedule(school, "Morning Shift", LocalTime.of(7, 30), LocalTime.of(12, 0), TENANT_ID);
        DepotEntity depot = dataFactory.createDepot("D_G2", "Greedy Depot Two", 21.0385, 105.8642, TENANT_ID);
        PickupPointEntity pickup = dataFactory.createPickupPoint("P_G2", "Greedy Pickup Two", "PICKUP_DROPOFF", 21.0185, 105.8442, TENANT_ID);

        // Link but DO NOT create window
        dataFactory.createSchoolPickupPoint(school, pickup, TENANT_ID);

        RoutePlanningSessionEntity session = dataFactory.createRoutePlanningSession(school, schedule, RouteDirection.OUTBOUND, TENANT_ID);

        ParentProfileEntity parent = dataFactory.createParentProfile("Parent Greedy Two", TENANT_ID);
        StudentEntity student = dataFactory.createStudent(school, parent, "Student Greedy Two", TENANT_ID);
        dataFactory.createStudentSubscription(student, school, pickup, null, TripOption.MORNING, TENANT_ID);

        // Prepare request
        GreedyGenerateRequest request = new GreedyGenerateRequest();
        request.setDepotId(depot.getId());
        request.setDefaultBusCapacity(30);

        // 2. Action
        GreedyGenerateResponse response = greedyRouteGenerationService.generateRoutes(session.getId(), request, TENANT_ID, ACTOR_ID);

        // 3. Assert
        assertNotNull(response);
        assertEquals(0, response.getTotalUnassignedStudents(), "Student without window is not eligible and shouldn't count as unassigned");
        assertTrue(response.getRoutes().isEmpty(), "No route should be generated");
    }
}
