package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;
import serp.project.school_bus_service.entity.DriverProfileEntity;
import serp.project.school_bus_service.entity.ParentProfileEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.StudentEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.entity.TransportRequestEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.repository.BusAttendantProfileRepository;
import serp.project.school_bus_service.repository.DriverProfileRepository;
import serp.project.school_bus_service.repository.ParentProfileRepository;
import serp.project.school_bus_service.repository.SchoolRepository;
import serp.project.school_bus_service.repository.StudentRepository;
import serp.project.school_bus_service.repository.StudentSubscriptionRepository;
import serp.project.school_bus_service.repository.TransportRequestRepository;
import serp.project.school_bus_service.repository.TripExecutionRepository;
import serp.project.school_bus_service.repository.TripStudentRepository;
import serp.project.school_bus_service.repository.SchoolBusUserRepository;
import serp.project.school_bus_service.entity.SchoolBusUserEntity;
import serp.project.school_bus_service.service.ISchoolBusDataScopeService;
import serp.project.school_bus_service.service.model.DashboardDataScope;
import serp.project.school_bus_service.shared.auth.SchoolBusSecurityService;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SchoolBusDataScopeServiceImpl implements ISchoolBusDataScopeService {

    private final TripExecutionRepository tripExecutionRepository;
    private final TripStudentRepository tripStudentRepository;
    private final StudentRepository studentRepository;
    private final TransportRequestRepository transportRequestRepository;
    private final StudentSubscriptionRepository studentSubscriptionRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final BusAttendantProfileRepository busAttendantProfileRepository;
    private final SchoolBusUserRepository schoolBusUserRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolBusSecurityService securityService;

    public SchoolBusDataScopeServiceImpl(
            TripExecutionRepository tripExecutionRepository,
            TripStudentRepository tripStudentRepository,
            StudentRepository studentRepository,
            TransportRequestRepository transportRequestRepository,
            StudentSubscriptionRepository studentSubscriptionRepository,
            ParentProfileRepository parentProfileRepository,
            DriverProfileRepository driverProfileRepository,
            BusAttendantProfileRepository busAttendantProfileRepository,
            SchoolBusUserRepository schoolBusUserRepository,
            SchoolRepository schoolRepository,
            SchoolBusSecurityService securityService) {
        this.tripExecutionRepository = tripExecutionRepository;
        this.tripStudentRepository = tripStudentRepository;
        this.studentRepository = studentRepository;
        this.transportRequestRepository = transportRequestRepository;
        this.studentSubscriptionRepository = studentSubscriptionRepository;
        this.parentProfileRepository = parentProfileRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.busAttendantProfileRepository = busAttendantProfileRepository;
        this.schoolBusUserRepository = schoolBusUserRepository;
        this.schoolRepository = schoolRepository;
        this.securityService = securityService;
    }

    @Override
    public void assertCanAccessTrip(Long tripId) {
        Long tenantId = securityService.getCurrentTenantId();
        TripExecutionEntity trip = tripExecutionRepository.findByIdAndTenantIdAndIsDeletedFalse(tripId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));

        if (securityService.isAdminOrDispatcher()) {
            return;
        }

        if (securityService.isDriver()) {
            Long driverProfileId = getCurrentDriverProfileIdRequired();
            if (trip.getDriver() == null || !trip.getDriver().getId().equals(driverProfileId)) {
                throw new AppException(AppErrorCode.Security.TRIP_NOT_ASSIGNED_TO_DRIVER);
            }
            return;
        }

        if (securityService.isAttendant()) {
            Long attendantProfileId = getCurrentAttendantProfileIdRequired();
            if (trip.getAttendant() == null || !trip.getAttendant().getId().equals(attendantProfileId)) {
                throw new AppException(AppErrorCode.Security.TRIP_NOT_ASSIGNED_TO_ATTENDANT);
            }
            return;
        }

        if (securityService.isParent()) {
            Long parentProfileId = getCurrentParentProfileIdRequired();
            boolean servesChild = tripStudentRepository.existsByTripIdAndStudentParentProfileIdAndIsDeletedFalse(tripId, parentProfileId);
            if (!servesChild) {
                throw new AppException(AppErrorCode.Security.FORBIDDEN_DATA_SCOPE);
            }
            return;
        }

        throw new AppException(AppErrorCode.Security.FORBIDDEN_DATA_SCOPE);
    }

    @Override
    public void assertCanOperateTrip(Long tripId) {
        Long tenantId = securityService.getCurrentTenantId();
        TripExecutionEntity trip = tripExecutionRepository.findByIdAndTenantIdAndIsDeletedFalse(tripId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));

        if (securityService.isAdminOrDispatcher()) {
            return;
        }

        if (securityService.isDriver()) {
            Long driverProfileId = getCurrentDriverProfileIdRequired();
            if (trip.getDriver() == null || !trip.getDriver().getId().equals(driverProfileId)) {
                throw new AppException(AppErrorCode.Security.TRIP_NOT_ASSIGNED_TO_DRIVER);
            }
            return;
        }

        throw new AppException(AppErrorCode.Security.FORBIDDEN_DATA_SCOPE);
    }

    @Override
    public void assertCanAccessAttendance(Long tripId) {
        assertCanAccessTrip(tripId);
    }

    @Override
    public void assertCanMarkAttendance(Long tripId) {
        Long tenantId = securityService.getCurrentTenantId();
        TripExecutionEntity trip = tripExecutionRepository.findByIdAndTenantIdAndIsDeletedFalse(tripId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));

        if (securityService.isAdminOrDispatcher()) {
            return;
        }

        if (securityService.isAttendant()) {
            Long attendantProfileId = getCurrentAttendantProfileIdRequired();
            if (trip.getAttendant() == null || !trip.getAttendant().getId().equals(attendantProfileId)) {
                throw new AppException(AppErrorCode.Security.TRIP_NOT_ASSIGNED_TO_ATTENDANT);
            }
            return;
        }

        throw new AppException(AppErrorCode.Security.FORBIDDEN_DATA_SCOPE);
    }

    @Override
    public void assertCanAccessStudent(Long studentId) {
        Long tenantId = securityService.getCurrentTenantId();
        StudentEntity student = studentRepository.findByIdAndTenantIdAndIsDeletedFalse(studentId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));

        if (securityService.isAdminOrDispatcher() || securityService.isDriver() || securityService.isAttendant()) {
            return;
        }

        if (securityService.isParent()) {
            Long parentProfileId = getCurrentParentProfileIdRequired();
            if (student.getParentProfile() == null || !student.getParentProfile().getId().equals(parentProfileId)) {
                throw new AppException(AppErrorCode.Security.STUDENT_NOT_BELONG_TO_PARENT);
            }
            return;
        }

        throw new AppException(AppErrorCode.Security.FORBIDDEN_DATA_SCOPE);
    }

    @Override
    public void assertCanAccessParentProfile(Long parentProfileId) {
        if (securityService.isAdminOrDispatcher()) {
            return;
        }

        if (securityService.isParent()) {
            Long currentParentId = getCurrentParentProfileIdRequired();
            if (!currentParentId.equals(parentProfileId)) {
                throw new AppException(AppErrorCode.Security.FORBIDDEN_DATA_SCOPE);
            }
            return;
        }

        throw new AppException(AppErrorCode.Security.FORBIDDEN_DATA_SCOPE);
    }

    @Override
    public void assertCanAccessTransportRequest(Long requestId) {
        Long tenantId = securityService.getCurrentTenantId();
        TransportRequestEntity request = transportRequestRepository.findByIdAndTenantIdAndIsDeletedFalse(requestId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));

        if (securityService.isAdminOrDispatcher()) {
            return;
        }

        if (securityService.isParent()) {
            Long parentProfileId = getCurrentParentProfileIdRequired();
            if (request.getParentProfile() == null || !request.getParentProfile().getId().equals(parentProfileId)) {
                throw new AppException(AppErrorCode.Security.REQUEST_NOT_BELONG_TO_PARENT);
            }
            return;
        }

        throw new AppException(AppErrorCode.Security.FORBIDDEN_DATA_SCOPE);
    }

    @Override
    public void assertCanAccessSubscription(Long subscriptionId) {
        Long tenantId = securityService.getCurrentTenantId();
        StudentSubscriptionEntity subscription = studentSubscriptionRepository.findByIdAndTenantIdAndIsDeletedFalse(subscriptionId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));

        if (securityService.isAdminOrDispatcher()) {
            return;
        }

        if (securityService.isParent()) {
            Long parentProfileId = getCurrentParentProfileIdRequired();
            if (subscription.getStudent() == null || subscription.getStudent().getParentProfile() == null
                    || !subscription.getStudent().getParentProfile().getId().equals(parentProfileId)) {
                throw new AppException(AppErrorCode.Security.SUBSCRIPTION_NOT_BELONG_TO_PARENT);
            }
            return;
        }

        throw new AppException(AppErrorCode.Security.FORBIDDEN_DATA_SCOPE);
    }

    private Optional<SchoolBusUserEntity> findCurrentSchoolBusUser() {
        Long accountUserId = securityService.getCurrentUserId();
        Optional<SchoolBusUserEntity> byAccountUserId =
                schoolBusUserRepository.findByAccountUserIdAndIsDeletedFalse(accountUserId);
        if (byAccountUserId.isPresent()) {
            return byAccountUserId;
        }

        try {
            String keycloakId = securityService.getCurrentKeycloakId();
            if (keycloakId != null && !keycloakId.isBlank()) {
                return schoolBusUserRepository.findByKeycloakIdAndIsDeletedFalse(keycloakId);
            }
        } catch (AppException ignored) {
            // Account user id remains the canonical lookup; Keycloak subject is a fallback.
        }
        return Optional.empty();
    }

    private SchoolBusUserEntity getCurrentSchoolBusUser() {
        return findCurrentSchoolBusUser()
                .orElseThrow(() -> new AppException(
                        AppErrorCode.Security.FORBIDDEN_DATA_SCOPE,
                        "Current shadow user not synchronized or found."));
    }

    @Override
    public Long getCurrentParentProfileIdRequired() {
        Long tenantId = securityService.getCurrentTenantId();
        SchoolBusUserEntity user = getCurrentSchoolBusUser();
        return parentProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, user.getId())
                .map(ParentProfileEntity::getId)
                .orElseThrow(() -> new AppException(AppErrorCode.Security.PARENT_PROFILE_NOT_FOUND));
    }

    @Override
    public Long getCurrentDriverProfileIdRequired() {
        Long tenantId = securityService.getCurrentTenantId();
        SchoolBusUserEntity user = getCurrentSchoolBusUser();
        return driverProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, user.getId())
                .map(DriverProfileEntity::getId)
                .orElseThrow(() -> new AppException(AppErrorCode.Security.DRIVER_PROFILE_NOT_FOUND));
    }

    @Override
    public Long getCurrentAttendantProfileIdRequired() {
        Long tenantId = securityService.getCurrentTenantId();
        SchoolBusUserEntity user = getCurrentSchoolBusUser();
        return busAttendantProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, user.getId())
                .map(BusAttendantProfileEntity::getId)
                .orElseThrow(() -> new AppException(AppErrorCode.Security.ATTENDANT_PROFILE_NOT_FOUND));
    }

    @Override
    public DashboardDataScope getDashboardDataScope(Long tenantId) {
        DashboardDataScope scope = new DashboardDataScope();
        scope.setTenantId(tenantId);

        List<SchoolEntity> schools;
        if (securityService.isAdminOrDispatcher()) {
            scope.setTenantWide(true);
            schools = schoolRepository.findByTenantIdAndIsDeletedFalseOrderByNameAsc(tenantId).stream()
                    .filter(school -> school.getIsActive() == Boolean.TRUE)
                    .toList();
        } else {
            Optional<SchoolBusUserEntity> user = findCurrentSchoolBusUser();
            if (user.isEmpty()) {
                return scope;
            }

            if (securityService.isDriver()) {
                Optional<DriverProfileEntity> driver =
                        driverProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, user.get().getId());
                if (driver.isEmpty()) {
                    return scope;
                }
                scope.setDriverProfileId(driver.get().getId());
                schools = schoolRepository.findDashboardSchoolsForDriver(tenantId, driver.get().getId());
            } else if (securityService.isAttendant()) {
                Optional<BusAttendantProfileEntity> attendant =
                        busAttendantProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(
                                tenantId,
                                user.get().getId());
                if (attendant.isEmpty()) {
                    return scope;
                }
                scope.setAttendantProfileId(attendant.get().getId());
                schools = schoolRepository.findDashboardSchoolsForAttendant(tenantId, attendant.get().getId());
            } else if (securityService.isParent()) {
                Optional<ParentProfileEntity> parent =
                        parentProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, user.get().getId());
                if (parent.isEmpty()) {
                    return scope;
                }
                scope.setParentProfileId(parent.get().getId());
                schools = schoolRepository.findDashboardSchoolsForParent(tenantId, parent.get().getId());
            } else {
                throw new AppException(AppErrorCode.Security.FORBIDDEN_DATA_SCOPE);
            }
        }

        scope.setAllowedSchoolIds(schools.stream()
                .map(SchoolEntity::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        return scope;
    }

    @Override
    public void assertCanAccessDashboardSchool(DashboardDataScope scope, Long schoolId) {
        if (schoolId == null) {
            return;
        }
        if (scope == null || !scope.getAllowedSchoolIds().contains(schoolId)) {
            throw new AppException(AppErrorCode.Security.FORBIDDEN_DATA_SCOPE);
        }
    }
}
