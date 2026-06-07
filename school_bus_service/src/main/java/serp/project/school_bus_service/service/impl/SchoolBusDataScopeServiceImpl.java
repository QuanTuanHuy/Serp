package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;
import serp.project.school_bus_service.entity.DriverProfileEntity;
import serp.project.school_bus_service.entity.ParentProfileEntity;
import serp.project.school_bus_service.entity.StudentEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.entity.TransportRequestEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.repository.BusAttendantProfileRepository;
import serp.project.school_bus_service.repository.DriverProfileRepository;
import serp.project.school_bus_service.repository.ParentProfileRepository;
import serp.project.school_bus_service.repository.StudentRepository;
import serp.project.school_bus_service.repository.StudentSubscriptionRepository;
import serp.project.school_bus_service.repository.TransportRequestRepository;
import serp.project.school_bus_service.repository.TripExecutionRepository;
import serp.project.school_bus_service.repository.TripStudentRepository;
import serp.project.school_bus_service.service.ISchoolBusDataScopeService;
import serp.project.school_bus_service.shared.auth.SchoolBusSecurityService;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

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
            SchoolBusSecurityService securityService) {
        this.tripExecutionRepository = tripExecutionRepository;
        this.tripStudentRepository = tripStudentRepository;
        this.studentRepository = studentRepository;
        this.transportRequestRepository = transportRequestRepository;
        this.studentSubscriptionRepository = studentSubscriptionRepository;
        this.parentProfileRepository = parentProfileRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.busAttendantProfileRepository = busAttendantProfileRepository;
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

    @Override
    public Long getCurrentParentProfileIdRequired() {
        Long tenantId = securityService.getCurrentTenantId();
        Long userId = securityService.getCurrentUserId();
        return parentProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, userId)
                .map(ParentProfileEntity::getId)
                .orElseThrow(() -> new AppException(AppErrorCode.Security.PARENT_PROFILE_NOT_FOUND));
    }

    @Override
    public Long getCurrentDriverProfileIdRequired() {
        Long tenantId = securityService.getCurrentTenantId();
        Long userId = securityService.getCurrentUserId();
        return driverProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, userId)
                .map(DriverProfileEntity::getId)
                .orElseThrow(() -> new AppException(AppErrorCode.Security.DRIVER_PROFILE_NOT_FOUND));
    }

    @Override
    public Long getCurrentAttendantProfileIdRequired() {
        Long tenantId = securityService.getCurrentTenantId();
        Long userId = securityService.getCurrentUserId();
        return busAttendantProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, userId)
                .map(BusAttendantProfileEntity::getId)
                .orElseThrow(() -> new AppException(AppErrorCode.Security.ATTENDANT_PROFILE_NOT_FOUND));
    }
}
