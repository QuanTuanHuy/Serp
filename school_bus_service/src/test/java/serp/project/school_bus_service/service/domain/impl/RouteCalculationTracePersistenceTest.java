package serp.project.school_bus_service.service.domain.impl;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.SchoolBusServiceApplication;
import serp.project.school_bus_service.dto.request.RouteCalculationTraceCreateCommand;
import serp.project.school_bus_service.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.entity.DepotEntity;
import serp.project.school_bus_service.entity.RouteCalculationTraceEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;
import serp.project.school_bus_service.enums.PlanningMethod;
import serp.project.school_bus_service.enums.PlanningSessionStatus;
import serp.project.school_bus_service.enums.RouteCalculationStatus;
import serp.project.school_bus_service.enums.RouteCalculationType;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.ShiftType;
import serp.project.school_bus_service.repository.DepotRepository;
import serp.project.school_bus_service.repository.RouteCalculationTraceRepository;
import serp.project.school_bus_service.repository.RoutePlanRepository;
import serp.project.school_bus_service.repository.RoutePlanningSessionRepository;
import serp.project.school_bus_service.repository.RouteStopRepository;
import serp.project.school_bus_service.repository.SchoolRepository;
import serp.project.school_bus_service.repository.SchoolScheduleRepository;
import serp.project.school_bus_service.service.IRouteCalculationTraceService;
import serp.project.school_bus_service.service.IRoutePlanningSessionService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SchoolBusServiceApplication.class)
public class RouteCalculationTracePersistenceTest {
    
    static {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    private static final Logger log = LoggerFactory.getLogger(RouteCalculationTracePersistenceTest.class);

    @Autowired
    private IRouteCalculationTraceService traceService;

    @Autowired
    private RouteCalculationTraceRepository traceRepository;

    @Autowired
    private RoutePlanRepository routePlanRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private IRoutePlanningSessionService sessionService;

    @Autowired
    private RoutePlanningSessionRepository sessionRepository;

    @Autowired
    private SchoolScheduleRepository schoolScheduleRepository;

    @Autowired
    private DepotRepository depotRepository;

    @Autowired
    private RouteStopRepository routeStopRepository;

    @Test
    public void testRouteCalculationTraceJsonbPersistence() {
        log.info("Starting integration test for RouteCalculationTraceEntity JSONB persistence...");

        SchoolEntity school = null;
        RoutePlanEntity routePlan = null;
        RouteCalculationTraceEntity savedTrace = null;
        boolean createdMockSchool = false;
        boolean createdMockRoute = false;

        try {
            // 1. Fetch or create School
            List<SchoolEntity> schools = schoolRepository.findAll();
            if (schools.isEmpty()) {
                school = new SchoolEntity();
                school.setName("Test School Integration");
                school.setCode("TS-INT");
                school.setAddress("123 Test Street");
                school.setLatitude(21.0);
                school.setLongitude(105.0);
                school.markCreated(1L, "SYSTEM");
                school = schoolRepository.save(school);
                createdMockSchool = true;
                log.info("Created mock SchoolEntity in DB with ID: {}", school.getId());
            } else {
                school = schools.get(0);
                log.info("Using existing SchoolEntity with ID: {}", school.getId());
            }

            // 2. Fetch or create RoutePlan
            List<RoutePlanEntity> routes = routePlanRepository.findAll();
            if (routes.isEmpty()) {
                routePlan = new RoutePlanEntity();
                routePlan.setSchool(school);
                routePlan.setRouteDirection(RouteDirection.OUTBOUND);
                routePlan.setStartLocationType(RouteLocationType.SCHOOL);
                routePlan.setStartSchool(school);
                routePlan.setEndLocationType(RouteLocationType.SCHOOL);
                routePlan.setEndSchool(school);
                routePlan.setRouteCode("R-TEST-INT");
                routePlan.setRouteName("Integration Test Route");
                routePlan.setServiceDate(LocalDate.now());
                routePlan.setShiftType(ShiftType.MORNING);
                routePlan.setStatus(RouteStatus.DRAFT);
                routePlan.setPlannedStudentCount(0);
                routePlan.markCreated(1L, "SYSTEM");
                routePlan = routePlanRepository.save(routePlan);
                createdMockRoute = true;
                log.info("Created mock RoutePlanEntity in DB with ID: {}", routePlan.getId());
            } else {
                routePlan = routes.get(0);
                log.info("Using existing RoutePlanEntity with ID: {}", routePlan.getId());
            }

            // 3. Prepare trace creation command with JSONB payloads
            RouteCalculationTraceCreateCommand command = new RouteCalculationTraceCreateCommand();
            command.setRoutePlanId(routePlan.getId());
            command.setTenantId(1L);
            command.setCalculationType(RouteCalculationType.MATRIX_AND_TIMELINE);
            command.setCalculationStatus(RouteCalculationStatus.SUCCESS);
            command.setSourceSummary("OSRM");
            
            command.setInputJson("{\"routePlanId\": " + routePlan.getId() + ", \"stops\": []}");
            command.setMatrixJson("{\"durations\": [1.2, 3.4], \"distances\": [0.5, 1.2]}");
            command.setTimelineJson("{\"stops\": [{\"stopOrder\": 1, \"arrivalTime\": \"08:00\"}]}");
            command.setIssuesJson("{\"issueCount\": 0, \"blockingIssueCount\": 0, \"issues\": []}");
            command.setConfigSnapshotJson("{\"ROUTING_OSRM_ENABLED\": \"true\", \"ROUTING_AVERAGE_SPEED_KMPH\": \"30.0\"}");

            // 4. Save trace via service (uses REQUIRES_NEW propagation)
            log.info("Saving RouteCalculationTraceEntity via service...");
            savedTrace = traceService.saveTrace(command);
            assertNotNull(savedTrace);
            assertNotNull(savedTrace.getId());
            log.info("RouteCalculationTraceEntity saved successfully with ID: {}", savedTrace.getId());

            // 5. Query from database to verify JSONB fields deserialized correctly
            RouteCalculationTraceEntity fetched = traceRepository.findById(savedTrace.getId()).orElse(null);
            assertNotNull(fetched);
            
            // Assert JSON structures are preserved in DB
            assertTrue(fetched.getInputJson().contains("routePlanId"));
            assertTrue(fetched.getMatrixJson().contains("durations"));
            assertTrue(fetched.getTimelineJson().contains("stops"));
            assertTrue(fetched.getIssuesJson().contains("issueCount"));
            assertTrue(fetched.getConfigSnapshotJson().contains("ROUTING_OSRM_ENABLED"));

            log.info("Integration test passed successfully. JSONB columns read and write without any issues!");
        } finally {
            // Cleanup to keep database clean
            if (savedTrace != null) {
                try {
                    traceRepository.delete(savedTrace);
                    log.info("Cleaned up saved RouteCalculationTraceEntity");
                } catch (Exception e) {
                    log.warn("Failed to delete saved trace: {}", e.getMessage());
                }
            }
            if (createdMockRoute && routePlan != null) {
                try {
                    routePlanRepository.delete(routePlan);
                    log.info("Cleaned up mock RoutePlanEntity");
                } catch (Exception e) {
                    log.warn("Failed to delete mock routePlan: {}", e.getMessage());
                }
            }
            if (createdMockSchool && school != null) {
                try {
                    schoolRepository.delete(school);
                    log.info("Cleaned up mock SchoolEntity");
                } catch (Exception e) {
                    log.warn("Failed to delete mock school: {}", e.getMessage());
                }
            }
        }
    }

    @Test
    public void testCreateRouteInSessionAutomaticallyGeneratesTrace() {
        log.info("Starting integration test for automatic trace generation during manual route creation...");

        SchoolEntity school = null;
        SchoolScheduleEntity schedule = null;
        DepotEntity depot = null;
        RoutePlanningSessionEntity session = null;
        RoutePlanResponse routeResponse = null;

        boolean createdMockSchool = false;
        boolean createdMockSchedule = false;
        boolean createdMockDepot = false;
        boolean createdMockSession = false;

        Long tenantId = 1L;
        Long actorId = 99L;

        try {
            // 1. Fetch or create School
            List<SchoolEntity> schools = schoolRepository.findAll();
            if (schools.isEmpty()) {
                school = new SchoolEntity();
                school.setName("Test School AutoTrace");
                school.setCode("SCH-AT");
                school.setAddress("456 Auto Trace Lane");
                school.setLatitude(21.03);
                school.setLongitude(105.78);
                school.markCreated(tenantId, "TEST");
                school = schoolRepository.save(school);
                createdMockSchool = true;
            } else {
                school = schools.get(0);
            }

            // 2. Fetch or create Depot
            List<DepotEntity> depots = depotRepository.findAll();
            if (depots.isEmpty()) {
                depot = new DepotEntity();
                depot.setName("Test Depot AutoTrace");
                depot.setCode("DEP-AT");
                depot.setAddress("789 Depot Boulevard");
                depot.setLatitude(21.01);
                depot.setLongitude(105.80);
                depot.markCreated(tenantId, "TEST");
                depot = depotRepository.save(depot);
                createdMockDepot = true;
            } else {
                depot = depots.get(0);
            }

            // 3. Fetch or create SchoolSchedule
            List<SchoolScheduleEntity> schedules = schoolScheduleRepository.findAll();
            if (schedules.isEmpty()) {
                schedule = new SchoolScheduleEntity();
                schedule.setSchool(school);
                schedule.setScheduleCode("SCHED-AT");
                schedule.setScheduleName("Test Schedule AutoTrace");
                schedule.setShiftType("MORNING");
                schedule.setEffectiveFrom(LocalDate.now());
                schedule.setIsDefaultSchedule(true);
                schedule.markCreated(tenantId, "TEST");
                schedule = schoolScheduleRepository.save(schedule);
                createdMockSchedule = true;
            } else {
                schedule = schedules.get(0);
            }

            // 4. Create RoutePlanningSession (must be MANUAL and DRAFT)
            session = new RoutePlanningSessionEntity();
            session.setSchool(school);
            session.setSchoolSchedule(schedule);
            session.setServiceDate(LocalDate.now().plusDays(new java.util.Random().nextInt(10000) + 100));
            session.setRouteDirection(RouteDirection.OUTBOUND);
            session.setPlanningMethod(PlanningMethod.MANUAL);
            session.setStatus(PlanningSessionStatus.DRAFT);
            session.setTotalEligibleStudents(10);
            session.setTotalPlannedStudents(0);
            session.setTotalUnassignedStudents(10);
            session.markCreated(tenantId, "TEST");
            session = sessionRepository.save(session);
            createdMockSession = true;

            // 5. Build RoutePlanUpsertRequest
            RoutePlanUpsertRequest request = new RoutePlanUpsertRequest();
            request.setSchoolId(school.getId());
            request.setRouteDirection("OUTBOUND");
            request.setStartLocationType("DEPOT");
            request.setStartDepotId(depot.getId());
            request.setEndLocationType("SCHOOL");
            request.setEndSchoolId(school.getId());
            request.setRouteName("Integration Test AutoTrace Route");
            request.setServiceDate(session.getServiceDate());
            request.setSchoolScheduleId(schedule.getId());

            // 6. Call sessionService.createRouteInSession
            log.info("Creating route in session via service: sessionId={}", session.getId());
            routeResponse = sessionService.createRouteInSession(session.getId(), request, tenantId, actorId);
            assertNotNull(routeResponse);
            assertNotNull(routeResponse.getId());
            log.info("Route created successfully with ID: {}", routeResponse.getId());

            // 7. Verify terminal stops are created
            List<RouteStopEntity> stops = routeStopRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeResponse.getId(), tenantId);
            assertFalse(stops.isEmpty(), "Initial stops should be created");
            assertEquals(2, stops.size(), "Should have exactly 2 terminal stops (start and end)");

            // 8. Verify calculation trace is automatically generated and saved
            List<RouteCalculationTraceEntity> traces = traceRepository.findByRoutePlanIdAndIsDeletedFalseOrderByCreatedAtDesc(routeResponse.getId());
            assertFalse(traces.isEmpty(), "Route calculation trace should be automatically generated");
            RouteCalculationTraceEntity trace = traces.get(0);
            assertNotNull(trace.getId());
            assertEquals(RouteCalculationStatus.SUCCESS, trace.getCalculationStatus());
            log.info("Verified calculation trace generated: traceId={}, status={}", trace.getId(), trace.getCalculationStatus());

        } finally {
            // Clean up to keep database clean (dependencies ordered properly)
            if (routeResponse != null) {
                try {
                    // Delete trace first (foreign key to route plan)
                    List<RouteCalculationTraceEntity> traces = traceRepository.findByRoutePlanIdAndIsDeletedFalseOrderByCreatedAtDesc(routeResponse.getId());
                    for (RouteCalculationTraceEntity tr : traces) {
                        traceRepository.delete(tr);
                    }
                    // Delete stops (foreign key to route plan)
                    List<RouteStopEntity> stops = routeStopRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeResponse.getId(), tenantId);
                    for (RouteStopEntity st : stops) {
                        routeStopRepository.delete(st);
                    }
                    // Delete route plan
                    RoutePlanEntity routeEntity = routePlanRepository.findById(routeResponse.getId()).orElse(null);
                    if (routeEntity != null) {
                        routePlanRepository.delete(routeEntity);
                    }
                    log.info("Cleaned up route, stops and traces for routeId={}", routeResponse.getId());
                } catch (Exception e) {
                    log.warn("Failed to delete route plan components: {}", e.getMessage());
                }
            }

            if (createdMockSession && session != null) {
                try {
                    sessionRepository.delete(session);
                    log.info("Cleaned up mock session");
                } catch (Exception e) {
                    log.warn("Failed to delete mock session: {}", e.getMessage());
                }
            }

            if (createdMockSchedule && schedule != null) {
                try {
                    schoolScheduleRepository.delete(schedule);
                    log.info("Cleaned up mock schedule");
                } catch (Exception e) {
                    log.warn("Failed to delete mock schedule: {}", e.getMessage());
                }
            }

            if (createdMockDepot && depot != null) {
                try {
                    depotRepository.delete(depot);
                    log.info("Cleaned up mock depot");
                } catch (Exception e) {
                    log.warn("Failed to delete mock depot: {}", e.getMessage());
                }
            }

            if (createdMockSchool && school != null) {
                try {
                    schoolRepository.delete(school);
                    log.info("Cleaned up mock school");
                } catch (Exception e) {
                    log.warn("Failed to delete mock school: {}", e.getMessage());
                }
            }
        }
    }
}
