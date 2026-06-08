package serp.project.school_bus_service.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.ManualDispatchRequest;
import serp.project.school_bus_service.dto.request.ReorderStopsRequest;
import serp.project.school_bus_service.dto.request.RouteAssignmentRequest;
import serp.project.school_bus_service.dto.response.AssignmentHistoryResponse;
import serp.project.school_bus_service.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;
import serp.project.school_bus_service.entity.BusEntity;
import serp.project.school_bus_service.entity.DriverProfileEntity;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.entity.RouteAssignmentHistoryEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.enums.RouteAssignmentStatus;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.repository.RouteAssignmentHistoryRepository;
import serp.project.school_bus_service.repository.RouteAssignmentRepository;
import serp.project.school_bus_service.repository.RoutePlanRepository;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.IAttendantService;
import serp.project.school_bus_service.service.IBusService;
import serp.project.school_bus_service.service.IDriverService;
import serp.project.school_bus_service.service.IRouteDispatchService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;

import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class RouteDispatchServiceImpl extends AbstractBaseService<RouteAssignmentEntity, Long>
        implements IRouteDispatchService {

    // Sentinel window used when a route has no planned times — covers the full day
    private static final LocalTime DAY_START = LocalTime.of(0, 0);
    private static final LocalTime DAY_END   = LocalTime.of(23, 59, 59);

    private final RouteAssignmentRepository routeAssignmentRepository;
    private final RouteAssignmentHistoryRepository routeAssignmentHistoryRepository;
    private final IRouteService routeService;
    private final IBusService busService;
    private final IDriverService driverService;
    private final IAttendantService attendantService;
    private final IRouteStopService routeStopService;
    private final IAuditLogService auditLogService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;


    public RouteDispatchServiceImpl(
    RouteAssignmentRepository routeAssignmentRepository,
                                 RouteAssignmentHistoryRepository routeAssignmentHistoryRepository,
                                 @Lazy IRouteService routeService,
                                 IBusService busService,
                                 IDriverService driverService,
                                 IAttendantService attendantService,
                                 // @Lazy: breaks circular dep — RouteDispatchServiceImpl ↔ RouteStopServiceImpl
                                 @Lazy IRouteStopService routeStopService,
                                 IAuditLogService auditLogService,
                                 SchoolBusMapper mapper,
                                 MessageCommon messageCommon) {
        this.routeAssignmentRepository = routeAssignmentRepository;
        this.routeAssignmentHistoryRepository = routeAssignmentHistoryRepository;
        this.routeService = routeService;
        this.busService = busService;
        this.driverService = driverService;
        this.attendantService = attendantService;
        this.routeStopService = routeStopService;
        this.auditLogService = auditLogService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
    }


    @Override
    protected BaseRepository<RouteAssignmentEntity, Long> getRepository() {
        return routeAssignmentRepository;
    }

    // ── Assign route (Phase 3 — strict PUBLISHED gate) ───────────────────────

    @Override
    @Transactional
    public RouteAssignmentResponse assignRoute(Long routeId, RouteAssignmentRequest request,
                                               Long tenantId, Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);

        // 1A — only PUBLISHED routes can be assigned
        if (route.getStatus() != RouteStatus.PUBLISHED) {
            throw new AppException(AppErrorCode.Dispatch.ROUTE_STATUS_INVALID,
                    messageCommon.getMessage(AppErrorCode.Dispatch.ROUTE_STATUS_INVALID, route.getStatus()));
        }

        // Load resources first — validates they exist and belong to tenant
        BusEntity bus             = busService.getBus(request.getBusId(), tenantId);
        DriverProfileEntity driver = driverService.getDriver(request.getDriverId(), tenantId);
        BusAttendantProfileEntity attendant = request.getAttendantId() == null ? null
                : attendantService.getAttendant(request.getAttendantId(), tenantId);

        // 2 — Resource status validation
        validateBusAvailable(bus);
        validateDriverAvailable(driver);
        if (attendant != null) validateAttendantAvailable(attendant);

        // 3 — Driver license validation
        String licenseWarning = checkDriverLicense(driver);

        // 4 — Bus capacity validation against planned student count
        String capacityWarning = checkCapacity(route, bus);

        // 5 — Time-window conflict detection (2B)
        LocalTime windowStart = route.getPlannedStartTime() != null ? route.getPlannedStartTime() : DAY_START;
        LocalTime windowEnd   = route.getPlannedEndTime()   != null ? route.getPlannedEndTime()   : DAY_END;
        validateTimeWindowConflicts(routeId, route.getServiceDate(),
                bus.getId(), driver.getId(),
                attendant == null ? null : attendant.getId(),
                windowStart, windowEnd, tenantId);

        // 6 — Upsert assignment
        RouteAssignmentEntity assignment = routeAssignmentRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId)
                .orElseGet(RouteAssignmentEntity::new);

        RouteAssignmentSnapshot oldSnapshot = RouteAssignmentSnapshot.from(assignment);

        boolean isNew = assignment.getId() == null;
        if (isNew) {
            assignment.markCreated(tenantId, actor(actorId));
        } else {
            // Mark old assignment as REPLACED, create a fresh one
            assignment.setStatus(RouteAssignmentStatus.REPLACED);
            assignment.markUpdated(actor(actorId));
            routeAssignmentRepository.save(assignment);
            assignment = new RouteAssignmentEntity();
            assignment.markCreated(tenantId, actor(actorId));
        }

        assignment.setRoute(route);
        assignment.setBus(bus);
        assignment.setDriver(driver);
        assignment.setAttendant(attendant);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setAssignedBy(actorId);
        assignment.setStatus(RouteAssignmentStatus.ASSIGNED);
        if (request.getAssignmentNote() != null) {
            assignment.setAssignmentNote(request.getAssignmentNote());
        }

        RouteAssignmentEntity saved = routeAssignmentRepository.save(assignment);

        // 7 — Update route status + capacity
        route.setStatus(RouteStatus.ASSIGNED);
        route.setAssignedBusCapacity(bus.getCapacity());
        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);

        // 8 — History
        String reason = request.getReason() != null ? request.getReason()
                : (isNew ? "ASSIGN" : "REPLACE");
        recordAssignmentHistory(route, oldSnapshot, saved, tenantId, actorId, reason);

        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "ASSIGN",
                "Assigned route resources: bus=" + bus.getPlateNumber()
                        + ", driver=" + driver.getFullName());

        RouteAssignmentResponse response = mapper.toRouteAssignmentResponse(saved);
        if (licenseWarning != null)  response.setLicenseWarning(licenseWarning);
        if (capacityWarning != null) response.setCapacityWarning(capacityWarning);
        return response;
    }

    // ── Manual dispatch (legacy — keeps PUBLISHED gate too) ──────────────────

    @Override
    @Transactional
    public RouteAssignmentResponse manualDispatchRoute(Long routeId, ManualDispatchRequest request,
                                                       Long tenantId, Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);

        if (route.getStatus() != RouteStatus.PUBLISHED) {
            throw new AppException(AppErrorCode.Dispatch.ROUTE_STATUS_INVALID,
                    messageCommon.getMessage(AppErrorCode.Dispatch.ROUTE_STATUS_INVALID, route.getStatus()));
        }

        RouteAssignmentRequest assignReq = new RouteAssignmentRequest();
        assignReq.setBusId(request.getBusId());
        assignReq.setDriverId(request.getDriverId());
        assignReq.setAttendantId(request.getAttendantId());

        // Reorder stops if provided
        if (request.getOrderedStopIds() != null && !request.getOrderedStopIds().isEmpty()) {
            ReorderStopsRequest reorderReq = new ReorderStopsRequest();
            reorderReq.setOrderedStopIds(request.getOrderedStopIds());
            routeStopService.reorderRouteStops(routeId, reorderReq, tenantId, actorId);
        }
        if (request.getNotes() != null) {
            route.setPlanningNotes(request.getNotes());
        }

        return assignRoute(routeId, assignReq, tenantId, actorId);
    }

    // ── Assignment history ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentHistoryResponse> getAssignmentHistory(Long routeId, Long tenantId) {
        return routeAssignmentHistoryRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByChangedAtDesc(routeId, tenantId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    // ── Validation helpers ────────────────────────────────────────────────────

    private void validateBusAvailable(BusEntity bus) {
        if (bus.getIsDeleted() != null && bus.getIsDeleted()) {
            throw new AppException(AppErrorCode.Bus.DELETED, messageCommon.getMessage(AppErrorCode.Bus.DELETED));
        }
        if (bus.getIsActive() != null && !bus.getIsActive()) {
            throw new AppException(AppErrorCode.Bus.INACTIVE, messageCommon.getMessage(AppErrorCode.Bus.INACTIVE));
        }
        if (bus.getStatus() != null && !"AVAILABLE".equalsIgnoreCase(bus.getStatus())) {
            throw new AppException(AppErrorCode.Bus.NOT_AVAILABLE,
                    messageCommon.getMessage(AppErrorCode.Bus.NOT_AVAILABLE, bus.getStatus()));
        }
    }

    private void validateDriverAvailable(DriverProfileEntity driver) {
        if (driver.getIsDeleted() != null && driver.getIsDeleted()) {
            throw new AppException(AppErrorCode.Driver.DELETED, messageCommon.getMessage(AppErrorCode.Driver.DELETED));
        }
        if (driver.getIsActive() != null && !driver.getIsActive()) {
            throw new AppException(AppErrorCode.Driver.INACTIVE, messageCommon.getMessage(AppErrorCode.Driver.INACTIVE));
        }
        if (driver.getStatus() != null && !"AVAILABLE".equalsIgnoreCase(driver.getStatus())) {
            throw new AppException(AppErrorCode.Driver.NOT_AVAILABLE,
                    messageCommon.getMessage(AppErrorCode.Driver.NOT_AVAILABLE, driver.getStatus()));
        }
    }

    private void validateAttendantAvailable(BusAttendantProfileEntity attendant) {
        if (attendant.getIsDeleted() != null && attendant.getIsDeleted()) {
            throw new AppException(AppErrorCode.Attendant.DELETED, messageCommon.getMessage(AppErrorCode.Attendant.DELETED));
        }
        if (attendant.getIsActive() != null && !attendant.getIsActive()) {
            throw new AppException(AppErrorCode.Attendant.INACTIVE, messageCommon.getMessage(AppErrorCode.Attendant.INACTIVE));
        }
        if (attendant.getStatus() != null && !"AVAILABLE".equalsIgnoreCase(attendant.getStatus())) {
            throw new AppException(AppErrorCode.Attendant.NOT_AVAILABLE,
                    messageCommon.getMessage(AppErrorCode.Attendant.NOT_AVAILABLE, attendant.getStatus()));
        }
    }

    /**
     * Returns a warning string if driver license is expired or expires within 30 days;
     * never blocks assignment (per plan: "display license warning").
     */
    private String checkDriverLicense(DriverProfileEntity driver) {
        if (driver.getLicenseExpiryDate() == null) return null;
        LocalDate today = LocalDate.now();
        if (driver.getLicenseExpiryDate().isBefore(today)) {
            return "Driver license has EXPIRED on " + driver.getLicenseExpiryDate();
        }
        if (!driver.getLicenseExpiryDate().isAfter(today.plusDays(30))) {
            return "Driver license expires soon: " + driver.getLicenseExpiryDate();
        }
        return null;
    }

    /**
     * Returns a warning string if bus capacity < planned student count.
     * Throws BLOCKING exception only if capacity is strictly less than student count.
     */
    private String checkCapacity(RoutePlanEntity route, BusEntity bus) {
        int planned = route.getPlannedStudentCount() == null ? 0 : route.getPlannedStudentCount();
        if (planned == 0 || bus.getCapacity() == null) return null;
        if (planned > bus.getCapacity()) {
            throw new AppException(AppErrorCode.Bus.CAPACITY_EXCEEDED,
                    messageCommon.getMessage(AppErrorCode.Bus.CAPACITY_EXCEEDED, bus.getCapacity(), planned));
        }
        int utilization = (int) Math.round(planned * 100.0 / bus.getCapacity());
        if (utilization > 90) {
            return "Bus utilization is " + utilization + "% — consider a larger vehicle";
        }
        return null;
    }

    /**
     * Time-window conflict detection (2B).
     * Uses precise overlap check when times are available; falls back to full-day sentinel.
     */
    private void validateTimeWindowConflicts(Long routeId, LocalDate serviceDate,
                                             Long busId, Long driverId, Long attendantId,
                                             LocalTime windowStart, LocalTime windowEnd,
                                             Long tenantId) {
        if (!routeAssignmentRepository
                .findBusConflicts(tenantId, busId, routeId, serviceDate, windowStart, windowEnd)
                .isEmpty()) {
            throw new AppException(AppErrorCode.Dispatch.BUS_TIME_CONFLICT,
                    messageCommon.getMessage(AppErrorCode.Dispatch.BUS_TIME_CONFLICT));
        }
        if (!routeAssignmentRepository
                .findDriverConflicts(tenantId, driverId, routeId, serviceDate, windowStart, windowEnd)
                .isEmpty()) {
            throw new AppException(AppErrorCode.Dispatch.DRIVER_TIME_CONFLICT,
                    messageCommon.getMessage(AppErrorCode.Dispatch.DRIVER_TIME_CONFLICT));
        }
        if (attendantId != null && !routeAssignmentRepository
                .findAttendantConflicts(tenantId, attendantId, routeId, serviceDate, windowStart, windowEnd)
                .isEmpty()) {
            throw new AppException(AppErrorCode.Dispatch.ATTENDANT_TIME_CONFLICT,
                    messageCommon.getMessage(AppErrorCode.Dispatch.ATTENDANT_TIME_CONFLICT));
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private void recordAssignmentHistory(RoutePlanEntity route, RouteAssignmentSnapshot oldSnapshot,
                                         RouteAssignmentEntity newAssignment, Long tenantId,
                                         Long actorId, String reason) {
        RouteAssignmentHistoryEntity history = new RouteAssignmentHistoryEntity();
        history.markCreated(tenantId, actor(actorId));
        history.setRoute(route);
        history.setOldBusId(oldSnapshot.busId());
        history.setNewBusId(newAssignment.getBus() == null ? null : newAssignment.getBus().getId());
        history.setOldDriverId(oldSnapshot.driverId());
        history.setNewDriverId(newAssignment.getDriver() == null ? null : newAssignment.getDriver().getId());
        history.setOldAttendantId(oldSnapshot.attendantId());
        history.setNewAttendantId(newAssignment.getAttendant() == null ? null : newAssignment.getAttendant().getId());
        history.setChangedBy(actorId);
        history.setChangedAt(LocalDateTime.now());
        history.setReason(reason);
        routeAssignmentHistoryRepository.save(history);
    }

    private AssignmentHistoryResponse toHistoryResponse(RouteAssignmentHistoryEntity e) {
        AssignmentHistoryResponse r = new AssignmentHistoryResponse();
        r.setRouteId(e.getRoute().getId());
        r.setOldBusId(e.getOldBusId());
        r.setNewBusId(e.getNewBusId());
        r.setOldDriverId(e.getOldDriverId());
        r.setNewDriverId(e.getNewDriverId());
        r.setOldAttendantId(e.getOldAttendantId());
        r.setNewAttendantId(e.getNewAttendantId());
        r.setChangedBy(e.getChangedBy());
        r.setChangedAt(e.getChangedAt());
        r.setReason(e.getReason());
        return r;
    }

    private record RouteAssignmentSnapshot(Long busId, Long driverId, Long attendantId) {
        private static RouteAssignmentSnapshot from(RouteAssignmentEntity assignment) {
            return new RouteAssignmentSnapshot(
                    assignment.getBus() == null ? null : assignment.getBus().getId(),
                    assignment.getDriver() == null ? null : assignment.getDriver().getId(),
                    assignment.getAttendant() == null ? null : assignment.getAttendant().getId());
        }
    }

    @Override
    public Optional<RouteAssignmentEntity> findAssignmentEntityByRoute(Long routeId, Long tenantId) {
        return routeAssignmentRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId);
    }
}
