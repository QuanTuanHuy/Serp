package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.AttendanceParamsRequest;
import serp.project.school_bus_service.application.dto.request.AttendanceActionRequest;
import serp.project.school_bus_service.application.dto.response.AttendanceResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.core.service.IAttendanceService;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.IMasterDataService;
import serp.project.school_bus_service.core.service.ITransportRequestService;
import serp.project.school_bus_service.enums.AttendanceStatus;
import serp.project.school_bus_service.enums.AttendanceType;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.AttendanceEntity;
import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.infrastructure.store.model.StudentEntity;
import serp.project.school_bus_service.infrastructure.store.repository.AttendanceRepository;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl extends AbstractBaseService<AttendanceEntity, Long> implements IAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final RouteServiceImpl routeService;
    private final IMasterDataService masterDataService;
    private final ITransportRequestService transportRequestService;
    private final IAuditLogService auditLogService;
    private final SchoolBusMapper mapper;

    @Override
    protected BaseRepository<AttendanceEntity, Long> getRepository() {
        return attendanceRepository;
    }

    @Override
    public PageResponse<AttendanceResponse> getAttendance(AttendanceParamsRequest params, Long tenantId) {
        return PageResponse.from(attendanceRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "route.routeCode", "student.fullName",
                        "attendanceType", "status", "notes"),
                pageable(params, Set.of("id", "recordedAt", "attendanceType", "status", "createdAt", "updatedAt"),
                        "recordedAt")),
                mapper::toAttendanceResponse);
    }

    @Override
    @Transactional
    public AttendanceResponse checkIn(AttendanceActionRequest request, Long tenantId, Long actorId) {
        return recordAttendance(request, tenantId, actorId, AttendanceType.CHECKED_IN);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(AttendanceActionRequest request, Long tenantId, Long actorId) {
        return recordAttendance(request, tenantId, actorId, AttendanceType.CHECKED_OUT);
    }

    private AttendanceResponse recordAttendance(AttendanceActionRequest request, Long tenantId, Long actorId,
            AttendanceType attendanceType) {
        RoutePlanEntity route = routeService.findById(request.getRouteId(), tenantId);
        StudentEntity student = masterDataService.getStudent(request.getStudentId(), tenantId);

        if (route.getStatus() != RouteStatus.IN_PROGRESS && route.getStatus() != RouteStatus.ASSIGNED) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }

        if (!student.getSchool().getId().equals(route.getSchool().getId())) {
            throw new AppException(AppErrorCode.INVALID_REQUEST);
        }

        if (!transportRequestService.hasApprovedRequestForStudent(student.getId(), route.getSchool().getId(),
                route.getServiceDate(), tenantId)) {
            throw new AppException(AppErrorCode.INVALID_REQUEST);
        }

        AttendanceEntity attendance = new AttendanceEntity();
        attendance.markCreated(tenantId, actor(actorId));
        attendance.setRoute(route);
        attendance.setStudent(student);
        attendance.setAttendanceType(attendanceType);
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setRecordedAt(LocalDateTime.now());
        attendance.setRecordedBy(actorId);
        attendance.setNotes(request.getNotes());
        AttendanceEntity saved = attendanceRepository.save(attendance);
        auditLogService.log(tenantId, actorId, "Attendance", saved.getId(), attendanceType.name(),
                "Recorded " + attendanceType.name().toLowerCase());
        return mapper.toAttendanceResponse(saved);
    }

    private Specification<AttendanceEntity> spec(Long tenantId, String keyword, String... fields) {
        return BaseSpecification.tenantActiveWithKeyword(tenantId, keyword, fields);
    }

    private Pageable pageable(
            serp.project.school_bus_service.application.dto.request.BaseParamsRequest params,
            Set<String> allowedSorts,
            String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
    }
}
