package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.SchoolPickupPointWindowUpsertRequest;
import serp.project.school_bus_service.dto.response.SchoolPickupPointWindowResponse;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.ISchoolPickupPointService;
import serp.project.school_bus_service.service.ISchoolPickupPointWindowService;
import serp.project.school_bus_service.service.ISchoolScheduleService;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;
import serp.project.school_bus_service.entity.SchoolPickupPointWindowEntity;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;
import serp.project.school_bus_service.repository.SchoolPickupPointWindowRepository;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SchoolPickupPointWindowServiceImpl extends AbstractBaseService<SchoolPickupPointWindowEntity, Long>
        implements ISchoolPickupPointWindowService {

    private static final Set<String> VALID_DIRECTIONS = Set.of("PICKUP_TO_SCHOOL", "DROPOFF_FROM_SCHOOL");

    /** usageType → allowed directions */
    private static final Map<String, Set<String>> USAGE_TYPE_ALLOWED_DIRECTIONS = Map.of(
            "PICKUP_ONLY", Set.of("PICKUP_TO_SCHOOL"),
            "DROPOFF_ONLY", Set.of("DROPOFF_FROM_SCHOOL"),
            "PICKUP_DROPOFF", Set.of("PICKUP_TO_SCHOOL", "DROPOFF_FROM_SCHOOL")
    );

    private final SchoolPickupPointWindowRepository windowRepository;
    private final ISchoolPickupPointService sppService;
    private final ISchoolScheduleService scheduleService;
    private final IAuditLogService auditLogService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;


    public SchoolPickupPointWindowServiceImpl(
    SchoolPickupPointWindowRepository windowRepository,
                                 ISchoolPickupPointService sppService,
                                 ISchoolScheduleService scheduleService,
                                 IAuditLogService auditLogService,
                                 SchoolBusMapper mapper,
                                 MessageCommon messageCommon) {
        this.windowRepository = windowRepository;
        this.sppService = sppService;
        this.scheduleService = scheduleService;
        this.auditLogService = auditLogService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
    }


    @Override
    protected BaseRepository<SchoolPickupPointWindowEntity, Long> getRepository() {
        return windowRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolPickupPointWindowResponse> getBySchoolPickupPoint(Long schoolPickupPointId, Long tenantId) {
        return windowRepository.findBySchoolPickupPointIdAndTenantIdAndIsDeletedFalse(schoolPickupPointId, tenantId)
                .stream().map(mapper::toSchoolPickupPointWindowResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolPickupPointWindowResponse> getBySchedule(Long schoolScheduleId, Long tenantId) {
        return windowRepository.findBySchoolScheduleIdAndTenantIdAndIsDeletedFalse(schoolScheduleId, tenantId)
                .stream().map(mapper::toSchoolPickupPointWindowResponse).toList();
    }

    @Override
    @Transactional
    public SchoolPickupPointWindowResponse create(SchoolPickupPointWindowUpsertRequest request,
            Long tenantId, Long actorId) {
        Long sppId = request.getSchoolPickupPointId();
        SchoolPickupPointEntity spp = sppService.getSchoolPickupPoint(sppId, tenantId);

        SchoolScheduleEntity schedule = scheduleService.getSchedule(request.getSchoolScheduleId(), tenantId);

        // Validate: schedule must belong to the same school as the linked pickup point
        validateSameSchool(spp, schedule);

        // Validate direction, usageType, time range, etc.
        validateWindow(request, spp);

        // Validate window times against schedule arrival/departure
        validateWindowAgainstSchedule(request, schedule);

        // Check uniqueness: (spp, schedule, direction) must be unique among active records
        checkUniqueness(sppId, request.getSchoolScheduleId(), request.getDirection(), null, tenantId);

        SchoolPickupPointWindowEntity entity = new SchoolPickupPointWindowEntity();
        entity.markCreated(tenantId, actor(actorId));
        entity.setSchoolPickupPoint(spp);
        entity.setSchoolSchedule(schedule);
        applyFields(entity, request);

        SchoolPickupPointWindowEntity saved = windowRepository.save(entity);
        auditLogService.log(tenantId, actorId, "SchoolPickupPointWindow", saved.getId(), "CREATE",
                "Created window: " + request.getDirection() + " for schedule " + schedule.getScheduleName());
        return mapper.toSchoolPickupPointWindowResponse(saved);
    }

    @Override
    @Transactional
    public SchoolPickupPointWindowResponse update(Long windowId,
            SchoolPickupPointWindowUpsertRequest request, Long tenantId, Long actorId) {
        SchoolPickupPointWindowEntity entity = windowRepository.findByIdAndTenantIdAndIsDeletedFalse(windowId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "Window not found"));

        SchoolPickupPointEntity spp = entity.getSchoolPickupPoint();
        SchoolScheduleEntity schedule = scheduleService.getSchedule(request.getSchoolScheduleId(), tenantId);

        // Validate: schedule must belong to the same school as the linked pickup point
        validateSameSchool(spp, schedule);

        // Validate direction, usageType, time range
        validateWindow(request, spp);

        // Validate window times against schedule arrival/departure
        validateWindowAgainstSchedule(request, schedule);

        // Check uniqueness: exclude current record
        checkUniqueness(spp.getId(), request.getSchoolScheduleId(), request.getDirection(), windowId, tenantId);

        entity.markUpdated(actor(actorId));
        entity.setSchoolSchedule(schedule);
        applyFields(entity, request);

        SchoolPickupPointWindowEntity saved = windowRepository.save(entity);
        auditLogService.log(tenantId, actorId, "SchoolPickupPointWindow", saved.getId(), "UPDATE",
                "Updated window: " + request.getDirection());
        return mapper.toSchoolPickupPointWindowResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long windowId, Long tenantId, Long actorId) {
        softDeleteById(windowRepository, windowId, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "SchoolPickupPointWindow", windowId, "SOFT_DELETE",
                "Deleted pickup point window");
    }

    @Override
    @Transactional
    public void softDeleteWindowsBySchoolPickupPointId(Long schoolPickupPointId, Long tenantId, Long actorId) {
        List<SchoolPickupPointWindowEntity> activeWindows =
                windowRepository.findBySchoolPickupPointIdAndTenantIdAndIsDeletedFalse(schoolPickupPointId, tenantId);
        String actorStr = actor(actorId);
        for (SchoolPickupPointWindowEntity w : activeWindows) {
            w.setIsDeleted(true);
            w.setIsActive(false);
            w.markUpdated(actorStr);
        }
        if (!activeWindows.isEmpty()) {
            windowRepository.saveAll(activeWindows);
            auditLogService.log(tenantId, actorId, "SchoolPickupPointWindow", schoolPickupPointId,
                    "CASCADE_SOFT_DELETE", "Cascade soft-deleted " + activeWindows.size()
                            + " windows for unlinked pickup point");
        }
    }

    @Override
    @Transactional
    public void softDeleteWindowsByScheduleId(Long schoolScheduleId, Long tenantId, Long actorId) {
        List<SchoolPickupPointWindowEntity> activeWindows =
                windowRepository.findBySchoolScheduleIdAndTenantIdAndIsDeletedFalse(schoolScheduleId, tenantId);
        String actorStr = actor(actorId);
        for (SchoolPickupPointWindowEntity w : activeWindows) {
            w.setIsDeleted(true);
            w.setIsActive(false);
            w.markUpdated(actorStr);
        }
        if (!activeWindows.isEmpty()) {
            windowRepository.saveAll(activeWindows);
            auditLogService.log(tenantId, actorId, "SchoolPickupPointWindow", schoolScheduleId,
                    "CASCADE_SOFT_DELETE", "Cascade soft-deleted " + activeWindows.size()
                            + " windows for deleted schedule");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasWindow(Long schoolPickupPointId, Long schoolScheduleId, String direction, Long tenantId) {
        return windowRepository
                .findBySchoolPickupPointIdAndSchoolScheduleIdAndTenantIdAndIsDeletedFalse(
                        schoolPickupPointId, schoolScheduleId, tenantId)
                .stream()
                .anyMatch(w -> direction.equals(w.getDirection()));
    }

    // ===== Private helpers =====

    private void validateSameSchool(SchoolPickupPointEntity spp, SchoolScheduleEntity schedule) {
        Long sppSchoolId = spp.getSchool().getId();
        Long scheduleSchoolId = schedule.getSchool().getId();
        if (!sppSchoolId.equals(scheduleSchoolId)) {
            throw new AppException(AppErrorCode.Window.SCHOOL_MISMATCH,
                    messageCommon.getMessage(AppErrorCode.Window.SCHOOL_MISMATCH, sppSchoolId, scheduleSchoolId));
        }
    }

    private void validateWindow(SchoolPickupPointWindowUpsertRequest request, SchoolPickupPointEntity spp) {
        // 1. Direction must be valid
        if (!VALID_DIRECTIONS.contains(request.getDirection())) {
            throw new AppException(AppErrorCode.Window.DIRECTION_INVALID,
                    messageCommon.getMessage(AppErrorCode.Window.DIRECTION_INVALID));
        }

        // 2. Validate direction vs usageType
        String usageType = spp.getPickupPoint().getUsageType();
        if (usageType != null) {
            Set<String> allowed = USAGE_TYPE_ALLOWED_DIRECTIONS.get(usageType);
            if (allowed != null && !allowed.contains(request.getDirection())) {
                throw new AppException(AppErrorCode.Window.DIRECTION_USAGE_MISMATCH,
                        messageCommon.getMessage(AppErrorCode.Window.DIRECTION_USAGE_MISMATCH,
                                request.getDirection(), usageType, allowed));
            }
        }

        // 3. Time range
        if (request.getWindowEnd().isBefore(request.getWindowStart())) {
            throw new AppException(AppErrorCode.Window.END_BEFORE_START,
                    messageCommon.getMessage(AppErrorCode.Window.END_BEFORE_START));
        }

        // 4. Distance & duration >= 0
        if (request.getEstimatedDistanceToSchoolKm() != null && request.getEstimatedDistanceToSchoolKm() < 0) {
            throw new AppException(AppErrorCode.Window.DISTANCE_NEGATIVE, messageCommon.getMessage(AppErrorCode.Window.DISTANCE_NEGATIVE));
        }
        if (request.getEstimatedDurationToSchoolMin() != null && request.getEstimatedDurationToSchoolMin() < 0) {
            throw new AppException(AppErrorCode.Window.DURATION_NEGATIVE, messageCommon.getMessage(AppErrorCode.Window.DURATION_NEGATIVE));
        }
    }

    private void checkUniqueness(Long sppId, Long scheduleId, String direction, Long excludeId, Long tenantId) {
        windowRepository.findBySchoolPickupPointIdAndSchoolScheduleIdAndTenantIdAndIsDeletedFalse(sppId, scheduleId, tenantId)
                .stream()
                .filter(w -> w.getDirection().equals(direction)
                        && (excludeId == null || !w.getId().equals(excludeId)))
                .findFirst()
                .ifPresent(existing -> {
                    throw new AppException(AppErrorCode.Window.CONFLICT,
                            messageCommon.getMessage(AppErrorCode.Window.CONFLICT));
                });
    }

    private void applyFields(SchoolPickupPointWindowEntity entity, SchoolPickupPointWindowUpsertRequest request) {
        entity.setDirection(request.getDirection());
        entity.setWindowStart(request.getWindowStart());
        entity.setWindowEnd(request.getWindowEnd());
        entity.setEstimatedDistanceToSchoolKm(request.getEstimatedDistanceToSchoolKm());
        entity.setEstimatedDurationToSchoolMin(request.getEstimatedDurationToSchoolMin());
    }

    /**
     * Validate that the window times are compatible with the schedule's arrival/departure times.
     * <ul>
     *   <li>PICKUP_TO_SCHOOL: windowEnd must be before arrivalDeadline; with travel duration, arrival must still be on time.</li>
     *   <li>DROPOFF_FROM_SCHOOL: windowStart must be after departureTime; with travel duration, must allow enough travel.</li>
     * </ul>
     */
    private void validateWindowAgainstSchedule(SchoolPickupPointWindowUpsertRequest request,
            SchoolScheduleEntity schedule) {
        String direction = request.getDirection();
        LocalTime windowStart = request.getWindowStart();
        LocalTime windowEnd = request.getWindowEnd();
        Integer durationMin = request.getEstimatedDurationToSchoolMin();

        if ("PICKUP_TO_SCHOOL".equals(direction)) {
            LocalTime arrivalDeadline = schedule.getArrivalDeadline();
            if (arrivalDeadline != null) {
                // windowEnd must be <= arrivalDeadline
                if (windowEnd.isAfter(arrivalDeadline)) {
                    throw new AppException(AppErrorCode.Window.PICKUP_AFTER_DEADLINE,
                            messageCommon.getMessage(AppErrorCode.Window.PICKUP_AFTER_DEADLINE, arrivalDeadline));
                }
                // With travel duration: windowEnd + duration must still be <= arrivalDeadline
                if (durationMin != null && durationMin > 0) {
                    LocalTime estimatedArrival = windowEnd.plusMinutes(durationMin);
                    if (estimatedArrival.isAfter(arrivalDeadline)) {
                        throw new AppException(AppErrorCode.Window.PICKUP_TRAVEL_EXCEEDS,
                                messageCommon.getMessage(AppErrorCode.Window.PICKUP_TRAVEL_EXCEEDS, durationMin, arrivalDeadline));
                    }
                }
            }
        } else if ("DROPOFF_FROM_SCHOOL".equals(direction)) {
            LocalTime departureTime = schedule.getDepartureTime();
            if (departureTime != null) {
                // windowStart must be >= departureTime
                if (windowStart.isBefore(departureTime)) {
                    throw new AppException(AppErrorCode.Window.DROPOFF_BEFORE_DEPARTURE,
                            messageCommon.getMessage(AppErrorCode.Window.DROPOFF_BEFORE_DEPARTURE, departureTime));
                }
                // With travel duration: departureTime + duration must be <= windowStart
                if (durationMin != null && durationMin > 0) {
                    LocalTime earliestArrivalAtPoint = departureTime.plusMinutes(durationMin);
                    if (earliestArrivalAtPoint.isAfter(windowStart)) {
                        throw new AppException(AppErrorCode.Window.DROPOFF_TRAVEL_INSUFFICIENT,
                                messageCommon.getMessage(AppErrorCode.Window.DROPOFF_TRAVEL_INSUFFICIENT, departureTime, durationMin));
                    }
                }
            }
        }
    }

    @Override
    public List<Long> findPointIdsWithWindow(Long schoolId, List<Long> pointIds, Long scheduleId,
                                              String direction, Long tenantId) {
        if (pointIds == null || pointIds.isEmpty()) {
            return List.of();
        }
        return windowRepository.findPointIdsWithWindow(schoolId, pointIds, scheduleId, direction, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolPickupPointWindowEntity> getWindowsForLinks(List<Long> linkIds, Long tenantId) {
        if (linkIds == null || linkIds.isEmpty()) {
            return List.of();
        }
        return windowRepository.findBySchoolPickupPointIdInAndTenantIdAndIsDeletedFalse(linkIds, tenantId);
    }
}
