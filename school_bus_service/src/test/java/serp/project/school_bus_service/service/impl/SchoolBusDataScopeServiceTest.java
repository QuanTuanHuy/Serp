package serp.project.school_bus_service.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.SchoolBusServiceApplication;
import serp.project.school_bus_service.entity.*;
import serp.project.school_bus_service.enums.*;
import serp.project.school_bus_service.repository.*;
import serp.project.school_bus_service.service.ISchoolBusDataScopeService;
import serp.project.school_bus_service.service.domain.impl.SchoolBusScenarioTestDataFactory;
import serp.project.school_bus_service.shared.auth.SchoolBusSecurityService;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = SchoolBusServiceApplication.class)
@Transactional
public class SchoolBusDataScopeServiceTest {

    static {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    private static final Long TENANT_1 = 1L;
    private static final Long TENANT_2 = 2L;

    @Autowired
    private SchoolBusScenarioTestDataFactory dataFactory;

    @Autowired
    private ISchoolBusDataScopeService dataScopeService;

    @Autowired
    private TripExecutionRepository tripExecutionRepository;

    @Autowired
    private TransportRequestRepository transportRequestRepository;

    @Autowired
    private StudentSubscriptionRepository studentSubscriptionRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ParentProfileRepository parentProfileRepository;

    @MockitoBean
    private SchoolBusSecurityService securityService;

    private SchoolEntity school1;
    private ParentProfileEntity parentTenant1;
    private StudentEntity studentTenant1;
    private StudentSubscriptionEntity subscriptionTenant1;
    private TransportRequestEntity requestTenant1;
    private TripExecutionEntity tripTenant1;

    @BeforeEach
    public void setUp() {
        // Create test data under Tenant 1
        school1 = dataFactory.createSchool("SCH-SCOPE", "Scope Test School", 21.0, 105.0, TENANT_1);
        
        parentTenant1 = new ParentProfileEntity();
        parentTenant1.setUserId(201L);
        parentTenant1.setFullName("Parent Tenant 1");
        parentTenant1.setEmail("parent1@test.com");
        parentTenant1.setPhone("0987654321");
        parentTenant1.markCreated(TENANT_1, "TEST");
        parentTenant1 = parentProfileRepository.save(parentTenant1);

        studentTenant1 = dataFactory.createStudent(school1, parentTenant1, "Student Tenant 1", TENANT_1);
        
        PickupPointEntity p1 = dataFactory.createPickupPoint("P-SCOPE", "Scope Point", "PICKUP_DROPOFF", 21.01, 105.01, TENANT_1);
        subscriptionTenant1 = dataFactory.createStudentSubscription(studentTenant1, school1, p1, null, TripOption.MORNING, TENANT_1);

        requestTenant1 = new TransportRequestEntity();
        requestTenant1.setParentProfile(parentTenant1);
        requestTenant1.setSchool(school1);
        requestTenant1.setRequestCode("REQ-SCOPE");
        requestTenant1.setRequestType(RequestType.NEW_SERVICE);
        requestTenant1.setStatus(RequestStatus.SUBMITTED);
        requestTenant1.setRequestSource(RequestSource.PARENT);
        requestTenant1.setRequestedAt(LocalDateTime.now());
        requestTenant1.setEffectiveFrom(LocalDate.now());
        requestTenant1.markCreated(TENANT_1, "TEST");
        requestTenant1 = transportRequestRepository.save(requestTenant1);

        SchoolScheduleEntity schedule1 = dataFactory.createSchoolSchedule(school1, "Morning Shift", LocalTime.of(7, 30), LocalTime.of(12, 0), TENANT_1);
        RoutePlanningSessionEntity session1 = dataFactory.createRoutePlanningSession(school1, schedule1, RouteDirection.OUTBOUND, TENANT_1);
        RoutePlanEntity route1 = dataFactory.createRoutePlan(school1, session1, RouteDirection.OUTBOUND, TENANT_1);

        tripTenant1 = new TripExecutionEntity();
        tripTenant1.setTripCode("TRIP-SCOPE");
        tripTenant1.setRoute(route1);
        tripTenant1.setServiceDate(LocalDate.now());
        tripTenant1.setRouteDirection(RouteDirection.OUTBOUND);
        tripTenant1.setShiftType(ShiftType.MORNING);
        tripTenant1.setStatus(TripStatus.PLANNED);
        tripTenant1.markCreated(TENANT_1, "TEST");
        tripTenant1 = tripExecutionRepository.save(tripTenant1);
    }

    @Test
    public void testAdminCanAccessTenant1Data() {
        // Given admin user in Tenant 1
        when(securityService.getCurrentTenantId()).thenReturn(TENANT_1);
        when(securityService.isAdminOrDispatcher()).thenReturn(true);

        // When/Then: no exceptions should be thrown
        assertDoesNotThrow(() -> dataScopeService.assertCanAccessStudent(studentTenant1.getId()));
        assertDoesNotThrow(() -> dataScopeService.assertCanAccessSubscription(subscriptionTenant1.getId()));
        assertDoesNotThrow(() -> dataScopeService.assertCanAccessTransportRequest(requestTenant1.getId()));
        assertDoesNotThrow(() -> dataScopeService.assertCanAccessTrip(tripTenant1.getId()));
    }

    @Test
    public void testAdminCannotAccessCrossTenantData() {
        // Given admin user in Tenant 2
        when(securityService.getCurrentTenantId()).thenReturn(TENANT_2);
        when(securityService.isAdminOrDispatcher()).thenReturn(true);

        // When/Then: since records are in TENANT_1, searching with TENANT_2 tenantId context must fail (NotFound)
        AppException ex = assertThrows(AppException.class, () -> dataScopeService.assertCanAccessStudent(studentTenant1.getId()));
        assertEquals(AppErrorCode.NOT_FOUND, ex.getErrorCode());

        ex = assertThrows(AppException.class, () -> dataScopeService.assertCanAccessSubscription(subscriptionTenant1.getId()));
        assertEquals(AppErrorCode.NOT_FOUND, ex.getErrorCode());

        ex = assertThrows(AppException.class, () -> dataScopeService.assertCanAccessTransportRequest(requestTenant1.getId()));
        assertEquals(AppErrorCode.NOT_FOUND, ex.getErrorCode());

        ex = assertThrows(AppException.class, () -> dataScopeService.assertCanAccessTrip(tripTenant1.getId()));
        assertEquals(AppErrorCode.NOT_FOUND, ex.getErrorCode());
    }

    @Test
    public void testParentCanAccessOwnStudent() {
        // Given parent user in Tenant 1 who owns the student
        when(securityService.getCurrentTenantId()).thenReturn(TENANT_1);
        when(securityService.getCurrentUserId()).thenReturn(201L);
        when(securityService.isParent()).thenReturn(true);
        when(securityService.isAdminOrDispatcher()).thenReturn(false);

        // When/Then: no exceptions should be thrown for accessing their own student
        assertDoesNotThrow(() -> dataScopeService.assertCanAccessStudent(studentTenant1.getId()));
    }

    @Test
    public void testParentCannotAccessOtherStudent() {
        // Given parent user in Tenant 1 who does NOT own the student
        // Let's create another parent in Tenant 1
        ParentProfileEntity otherParent = new ParentProfileEntity();
        otherParent.setUserId(202L);
        otherParent.setFullName("Other Parent Tenant 1");
        otherParent.setEmail("otherparent1@test.com");
        otherParent.setPhone("0987654322");
        otherParent.markCreated(TENANT_1, "TEST");
        parentProfileRepository.save(otherParent);

        when(securityService.getCurrentTenantId()).thenReturn(TENANT_1);
        when(securityService.getCurrentUserId()).thenReturn(202L);
        when(securityService.isParent()).thenReturn(true);
        when(securityService.isAdminOrDispatcher()).thenReturn(false);

        // When/Then: must throw STUDENT_NOT_BELONG_TO_PARENT
        AppException ex = assertThrows(AppException.class, () -> dataScopeService.assertCanAccessStudent(studentTenant1.getId()));
        assertEquals(AppErrorCode.Security.STUDENT_NOT_BELONG_TO_PARENT, ex.getErrorCode());
    }
}
