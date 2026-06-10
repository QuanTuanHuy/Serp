package serp.project.school_bus_service.service.domain.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import serp.project.school_bus_service.entity.*;
import serp.project.school_bus_service.enums.*;
import serp.project.school_bus_service.repository.*;

import java.time.LocalDate;

/**
 * Test data factory for scenario/integration tests.
 * SchoolSchedule has been removed in Phase 2 refactoring.
 */
@Component
public class SchoolBusScenarioTestDataFactory {

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private DepotRepository depotRepository;

    @Autowired
    private PickupPointRepository pickupPointRepository;

    @Autowired
    private SchoolPickupPointRepository schoolPickupPointRepository;

    @Autowired
    private ParentProfileRepository parentProfileRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentSubscriptionRepository studentSubscriptionRepository;

    @Autowired
    private RoutePlanningSessionRepository sessionRepository;

    @Autowired
    private RoutePlanRepository routePlanRepository;

    @Autowired
    private RouteStopRepository routeStopRepository;

    @Autowired
    private RoutePlanStudentRepository routePlanStudentRepository;

    @Autowired
    private SchoolBusAppConfigRepository appConfigRepository;

    public SchoolEntity createSchool(String code, String name, Double lat, Double lon, Long tenantId) {
        SchoolEntity school = new SchoolEntity();
        school.setCode(code);
        school.setName(name);
        school.setAddress("Test Address");
        school.setLatitude(lat);
        school.setLongitude(lon);
        school.markCreated(tenantId, "TEST");
        return schoolRepository.save(school);
    }

    public DepotEntity createDepot(String code, String name, Double lat, Double lon, Long tenantId) {
        DepotEntity depot = new DepotEntity();
        depot.setCode(code);
        depot.setName(name);
        depot.setAddress("Test Depot Address");
        depot.setLatitude(lat);
        depot.setLongitude(lon);
        depot.markCreated(tenantId, "TEST");
        return depotRepository.save(depot);
    }

    public PickupPointEntity createPickupPoint(String code, String name, String usageType, Double lat, Double lon, Long tenantId) {
        PickupPointEntity pt = new PickupPointEntity();
        pt.setCode(code);
        pt.setName(name);
        pt.setAddress("Test Pickup Point Address");
        pt.setLatitude(lat);
        pt.setLongitude(lon);
        pt.setUsageType(usageType);
        pt.markCreated(tenantId, "TEST");
        return pickupPointRepository.save(pt);
    }

    public SchoolPickupPointEntity createSchoolPickupPoint(SchoolEntity school, PickupPointEntity point, Long tenantId) {
        SchoolPickupPointEntity spp = new SchoolPickupPointEntity();
        spp.setSchool(school);
        spp.setPickupPoint(point);
        spp.setIsDefaultPoint(false);
        spp.markCreated(tenantId, "TEST");
        return schoolPickupPointRepository.save(spp);
    }

    public ParentProfileEntity createParentProfile(String fullName, Long tenantId) {
        ParentProfileEntity parent = new ParentProfileEntity();
        parent.setUserId(100L + System.currentTimeMillis() % 10000);
        parent.setFullName(fullName);
        parent.setEmail("parent@test.com");
        parent.setPhone("0987654321");
        parent.markCreated(tenantId, "TEST");
        return parentProfileRepository.save(parent);
    }

    public StudentEntity createStudent(SchoolEntity school, ParentProfileEntity parent, String fullName, Long tenantId) {
        StudentEntity student = new StudentEntity();
        student.setSchool(school);
        student.setParentProfile(parent);
        student.setFullName(fullName);
        student.setStudentCode("STU-" + System.currentTimeMillis());
        student.markCreated(tenantId, "TEST");
        return studentRepository.save(student);
    }

    public StudentSubscriptionEntity createStudentSubscription(
            StudentEntity student, SchoolEntity school, PickupPointEntity pickupPoint, PickupPointEntity dropoffPoint,
            TripOption tripOption, Long tenantId) {
        StudentSubscriptionEntity sub = new StudentSubscriptionEntity();
        sub.setStudent(student);
        sub.setSchool(school);
        sub.setPickupPoint(pickupPoint);
        sub.setDropoffPoint(dropoffPoint);
        sub.setSubscriptionCode("SUB-" + System.currentTimeMillis());
        sub.setTripOption(tripOption);
        sub.setEffectiveFrom(LocalDate.now().minusDays(5));
        sub.setEffectiveTo(LocalDate.now().plusDays(20));
        sub.setStatus(SubscriptionStatus.ACTIVE);

        // Active everyday
        sub.setMonday(true);
        sub.setTuesday(true);
        sub.setWednesday(true);
        sub.setThursday(true);
        sub.setFriday(true);
        sub.setSaturday(true);
        sub.setSunday(true);

        sub.markCreated(tenantId, "TEST");
        return studentSubscriptionRepository.save(sub);
    }

    public RoutePlanningSessionEntity createRoutePlanningSession(SchoolEntity school, RouteDirection direction, Long tenantId) {
        RoutePlanningSessionEntity session = new RoutePlanningSessionEntity();
        session.setSchool(school);
        session.setServiceDate(LocalDate.now());
        session.setRouteDirection(direction);
        session.setPlanningMethod(PlanningMethod.GREEDY);
        session.setStatus(PlanningSessionStatus.DRAFT);
        session.setTotalEligibleStudents(0);
        session.setTotalPlannedStudents(0);
        session.setTotalUnassignedStudents(0);
        session.setTotalRoutes(0);
        session.setTotalStops(0);
        session.markCreated(tenantId, "TEST");
        return sessionRepository.save(session);
    }

    public RoutePlanEntity createRoutePlan(SchoolEntity school, RoutePlanningSessionEntity session, RouteDirection direction, Long tenantId) {
        DepotEntity depot = createDepot("D_TEMP_" + System.currentTimeMillis(), "Temp Depot", 21.0385, 105.8642, tenantId);

        RoutePlanEntity route = new RoutePlanEntity();
        route.setSchool(school);
        route.setRouteDirection(direction);
        route.setStartLocationType(direction == RouteDirection.OUTBOUND ? RouteLocationType.DEPOT : RouteLocationType.SCHOOL);
        route.setEndLocationType(direction == RouteDirection.OUTBOUND ? RouteLocationType.SCHOOL : RouteLocationType.DEPOT);

        if (direction == RouteDirection.OUTBOUND) {
            route.setStartDepot(depot);
            route.setEndSchool(school);
        } else {
            route.setStartSchool(school);
            route.setEndDepot(depot);
        }

        route.setRouteCode("ROT-" + System.currentTimeMillis());
        route.setRouteName("Test Route Plan");
        route.setServiceDate(LocalDate.now());
        route.setStatus(RouteStatus.DRAFT);
        route.setPlanningSession(session);
        route.setPlannedStudentCount(0);
        route.setAssignedBusCapacity(30);
        route.markCreated(tenantId, "TEST");
        return routePlanRepository.save(route);
    }

    public RouteStopEntity createRouteStop(
            RoutePlanEntity route, Object location, RouteLocationType locationType, RouteStopPurpose purpose, int order, Long tenantId) {
        RouteStopEntity stop = new RouteStopEntity();
        stop.setRoute(route);
        stop.setLocationType(locationType);
        stop.setStopPurpose(purpose);
        stop.setStopOrder(order);
        stop.setEstimatedStudentCount(0);
        stop.setPlannedBoardingCount(0);
        stop.setPlannedDropoffCount(0);

        if (locationType == RouteLocationType.DEPOT) {
            stop.setDepot((DepotEntity) location);
        } else if (locationType == RouteLocationType.SCHOOL) {
            stop.setSchool((SchoolEntity) location);
        } else if (locationType == RouteLocationType.PICKUP_POINT) {
            stop.setPickupPoint((PickupPointEntity) location);
        }

        stop.markCreated(tenantId, "TEST");
        return routeStopRepository.save(stop);
    }

    public RoutePlanStudentEntity createRoutePlanStudent(
            RoutePlanEntity route, RouteStopEntity stop, StudentEntity student, StudentSubscriptionEntity sub, Long tenantId) {
        RoutePlanStudentEntity ps = new RoutePlanStudentEntity();
        ps.setRoute(route);
        ps.setRouteStop(stop);
        ps.setStudent(student);
        ps.setSubscription(sub);
        ps.setServiceAction(route.getRouteDirection() == RouteDirection.OUTBOUND ? RoutePlanStudentAction.BOARD : RoutePlanStudentAction.DROPOFF);
        ps.markCreated(tenantId, "TEST");
        return routePlanStudentRepository.save(ps);
    }

    public void createAppConfig(String code, String value) {
        java.util.Optional<SchoolBusAppConfigEntity> existing = appConfigRepository
                .findFirstByConfigCodeAndIsActiveTrueAndIsDeletedFalse(code);
        if (existing.isPresent()) {
            SchoolBusAppConfigEntity config = existing.get();
            config.setConfigValue(value);
            config.markUpdated("TEST");
            appConfigRepository.save(config);
        } else {
            SchoolBusAppConfigEntity config = new SchoolBusAppConfigEntity();
            config.setConfigCode(code);
            config.setConfigName(code);
            config.setConfigType("DECIMAL");
            config.setConfigValue(value);
            config.setIsDeleted(false);
            config.setIsActive(true);
            config.markCreated("TEST");
            appConfigRepository.save(config);
        }
    }
}
