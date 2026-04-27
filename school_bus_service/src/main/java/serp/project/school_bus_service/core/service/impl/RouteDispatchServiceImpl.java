package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.request.ManualDispatchRequest;
import serp.project.school_bus_service.application.dto.request.ReorderStopsRequest;
import serp.project.school_bus_service.application.dto.request.RouteAssignmentRequest;
import serp.project.school_bus_service.application.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.IBusService;
import serp.project.school_bus_service.core.service.IDriverService;
import serp.project.school_bus_service.core.service.IAttendantService;
import serp.project.school_bus_service.core.service.IRouteDispatchService;
import serp.project.school_bus_service.core.service.IRouteStopService;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.BusEntity;
import serp.project.school_bus_service.infrastructure.store.model.RouteAssignmentEntity;
import serp.project.school_bus_service.infrastructure.store.model.RouteAssignmentHistoryEntity;
import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.infrastructure.store.repository.RouteAssignmentHistoryRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RouteAssignmentRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RoutePlanRepository;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteDispatchServiceImpl extends AbstractBaseService<RoutePlanEntity, Long>
        implements IRouteDispatchService {

    private final RoutePlanRepository routePlanRepository;
    private final RouteAssignmentRepository routeAssignmentRepository;
    private final RouteAssignmentHistoryRepository routeAssignmentHistoryRepository;
    private final IBusService busService;
    private final IDriverService driverService;
    private final IAttendantService attendantService;
    private final IRouteStopService routeStopService;
    private final IAuditLogService auditLogService;
    private final SchoolBusMapper mapper;

    @Override
    protected BaseRepository<RoutePlanEntity, Long> getRepository() {
        return routePlanRepository;
    }

    @Override
    @Transactional
    public RouteAssignmentResponse assignRoute(Long routeId, RouteAssignmentRequest request, Long tenantId,
            Long actorId) {
        RoutePlanEntity route = findById(routePlanRepository, routeId, tenantId);
        if (route.getStatus() == RouteStatus.COMPLETED || route.getStatus() == RouteStatus.CANCELLED) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }

        validateAssignmentConflict(routeId, route.getServiceDate(), request, tenantId);
        BusEntity bus = busService.getBus(request.getBusId(), tenantId);
        validateCapacity(route, bus);

        RouteAssignmentEntity assignment = routeAssignmentRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId)
                .orElseGet(RouteAssignmentEntity::new);
        RouteAssignmentSnapshot oldSnapshot = RouteAssignmentSnapshot.from(assignment);
        if (assignment.getId() == null) {
            assignment.markCreated(tenantId, actor(actorId));
        } else {
            assignment.markUpdated(actor(actorId));
        }
        assignment.setRoute(route);
        assignment.setBus(bus);
        assignment.setDriver(driverService.getDriver(request.getDriverId(), tenantId));
        assignment.setAttendant(request.getAttendantId() == null ? null
                : attendantService.getAttendant(request.getAttendantId(), tenantId));
        assignment.setAssignedAt(LocalDateTime.now());
        RouteAssignmentEntity saved = routeAssignmentRepository.save(assignment);
        route.markUpdated(actor(actorId));
        route.setStatus(RouteStatus.ASSIGNED);
        route.setAssignedBusCapacity(bus.getCapacity());
        routePlanRepository.save(route);
        recordAssignmentHistory(route, oldSnapshot, saved, tenantId, actorId, "ASSIGN");
        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "ASSIGN", "Assigned route resources");
        return mapper.toRouteAssignmentResponse(saved);
    }

    @Override
    @Transactional
    public RouteAssignmentResponse manualDispatchRoute(Long routeId, ManualDispatchRequest request, Long tenantId,
            Long actorId) {
        RoutePlanEntity route = findById(routePlanRepository, routeId, tenantId);
        if (route.getStatus() == RouteStatus.COMPLETED || route.getStatus() == RouteStatus.CANCELLED) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }

        RouteAssignmentRequest assignReq = new RouteAssignmentRequest();
        assignReq.setBusId(request.getBusId());
        assignReq.setDriverId(request.getDriverId());
        assignReq.setAttendantId(request.getAttendantId());
        validateAssignmentConflict(routeId, route.getServiceDate(), assignReq, tenantId);
        BusEntity bus = busService.getBus(request.getBusId(), tenantId);
        validateCapacity(route, bus);

        if (request.getOrderedStopIds() != null && !request.getOrderedStopIds().isEmpty()) {
            ReorderStopsRequest reorderReq = new ReorderStopsRequest();
            reorderReq.setOrderedStopIds(request.getOrderedStopIds());
            routeStopService.reorderRouteStops(routeId, reorderReq, tenantId, actorId);
        }

        RouteAssignmentEntity assignment = routeAssignmentRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId)
                .orElseGet(RouteAssignmentEntity::new);
        RouteAssignmentSnapshot oldSnapshot = RouteAssignmentSnapshot.from(assignment);
        if (assignment.getId() == null) {
            assignment.markCreated(tenantId, actor(actorId));
        } else {
            assignment.markUpdated(actor(actorId));
        }
        assignment.setRoute(route);
        assignment.setBus(bus);
        assignment.setDriver(driverService.getDriver(request.getDriverId(), tenantId));
        assignment.setAttendant(request.getAttendantId() == null ? null
                : attendantService.getAttendant(request.getAttendantId(), tenantId));
        assignment.setAssignedAt(LocalDateTime.now());
        RouteAssignmentEntity saved = routeAssignmentRepository.save(assignment);

        if (request.getNotes() != null) {
            route.setPlanningNotes(request.getNotes());
        }
        route.markUpdated(actor(actorId));
        route.setStatus(RouteStatus.ASSIGNED);
        route.setAssignedBusCapacity(bus.getCapacity());
        routePlanRepository.save(route);
        recordAssignmentHistory(route, oldSnapshot, saved, tenantId, actorId, "MANUAL_DISPATCH");
        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "MANUAL_DISPATCH",
                "Manual dispatch of route resources");
        return mapper.toRouteAssignmentResponse(saved);
    }

    private void validateAssignmentConflict(Long routeId, LocalDate serviceDate, RouteAssignmentRequest request,
            Long tenantId) {
        validateRouteResourceConflict(routeId, serviceDate,
                routeAssignmentRepository.findByBusIdAndTenantIdAndIsDeletedFalse(request.getBusId(), tenantId));
        validateRouteResourceConflict(routeId, serviceDate,
                routeAssignmentRepository.findByDriverIdAndTenantIdAndIsDeletedFalse(request.getDriverId(), tenantId));
        if (request.getAttendantId() != null) {
            validateRouteResourceConflict(routeId, serviceDate,
                    routeAssignmentRepository.findByAttendantIdAndTenantIdAndIsDeletedFalse(request.getAttendantId(),
                            tenantId));
        }
    }

    private void validateRouteResourceConflict(Long routeId, LocalDate serviceDate,
            List<RouteAssignmentEntity> assignments) {
        boolean hasConflict = assignments.stream()
                .map(RouteAssignmentEntity::getRoute)
                .filter(route -> !route.getId().equals(routeId))
                .filter(route -> route.getServiceDate().equals(serviceDate))
                .anyMatch(route -> route.getStatus() != RouteStatus.CANCELLED
                        && route.getStatus() != RouteStatus.COMPLETED);
        if (hasConflict) {
            throw new AppException(AppErrorCode.CONFLICT);
        }
    }

    private void validateCapacity(RoutePlanEntity route, BusEntity bus) {
        int plannedStudents = route.getPlannedStudentCount() == null ? 0 : route.getPlannedStudentCount();
        if (plannedStudents > 0 && bus.getCapacity() != null && plannedStudents > bus.getCapacity()) {
            throw new AppException(AppErrorCode.CONFLICT,
                    "Planned student count exceeds selected bus capacity");
        }
    }

    private void recordAssignmentHistory(RoutePlanEntity route, RouteAssignmentSnapshot oldSnapshot,
            RouteAssignmentEntity newAssignment, Long tenantId, Long actorId, String reason) {
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

    private record RouteAssignmentSnapshot(Long busId, Long driverId, Long attendantId) {
        private static RouteAssignmentSnapshot from(RouteAssignmentEntity assignment) {
            return new RouteAssignmentSnapshot(
                    assignment.getBus() == null ? null : assignment.getBus().getId(),
                    assignment.getDriver() == null ? null : assignment.getDriver().getId(),
                    assignment.getAttendant() == null ? null : assignment.getAttendant().getId());
        }
    }
}
