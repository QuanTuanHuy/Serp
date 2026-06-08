package serp.project.school_bus_service.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.SchoolScheduleUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.SchoolScheduleResponse;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.ICodeGeneratorService;

import serp.project.school_bus_service.service.ISchoolScheduleService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.SchoolScheduleDayEntity;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;
import serp.project.school_bus_service.repository.SchoolScheduleRepository;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SchoolScheduleServiceImpl extends AbstractBaseService<SchoolScheduleEntity, Long>
        implements ISchoolScheduleService {

    private static final Set<String> VALID_DAYS = Set.of(
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

    private final SchoolScheduleRepository scheduleRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;
    private final ICodeGeneratorService codeGeneratorService;
    private final ISchoolService schoolService;
    private final MessageCommon messageCommon;

    public SchoolScheduleServiceImpl(
            SchoolScheduleRepository scheduleRepository,
            SchoolBusMapper mapper,
            IAuditLogService auditLogService,
            ICodeGeneratorService codeGeneratorService,
            ISchoolService schoolService,
            MessageCommon messageCommon) {
        this.scheduleRepository = scheduleRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
        this.codeGeneratorService = codeGeneratorService;
        this.schoolService = schoolService;
        this.messageCommon = messageCommon;
    }

    @Override
    protected BaseRepository<SchoolScheduleEntity, Long> getRepository() {
        return scheduleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SchoolScheduleResponse> getSchedulesBySchool(Long schoolId, int page, int size, Long tenantId) {
        return PageResponse.from(
                scheduleRepository.findBySchoolIdWithDays(
                        schoolId, tenantId, PageRequest.of(page, size, Sort.by("scheduleName"))),
                mapper::toSchoolScheduleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolScheduleResponse> getActiveSchedulesBySchool(Long schoolId, Long tenantId) {
        return scheduleRepository.findActiveBySchoolIdWithDays(schoolId, tenantId)
                .stream().map(mapper::toSchoolScheduleResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolScheduleResponse getScheduleResponse(Long id, Long tenantId) {
        SchoolScheduleEntity entity = scheduleRepository.findByIdWithDays(id, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "School schedule not found"));
        return mapper.toSchoolScheduleResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolScheduleEntity getSchedule(Long id, Long tenantId) {
        return scheduleRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "School schedule not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolScheduleEntity getScheduleWithDays(Long id, Long tenantId) {
        return scheduleRepository.findByIdWithDays(id, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "School schedule not found"));
    }

    @Override
    @Transactional
    public SchoolScheduleResponse createSchedule(Long schoolId, SchoolScheduleUpsertRequest request,
            Long tenantId, Long actorId) {
        SchoolEntity school = schoolService.getSchool(schoolId, tenantId);
        SchoolScheduleEntity entity = new SchoolScheduleEntity();
        entity.markCreated(tenantId, actor(actorId));
        entity.setSchool(school);
        entity.setScheduleCode(codeGeneratorService.generate(
                SchoolBusCode.SCHEDULE.sequenceKey(), SchoolBusCode.SCHEDULE.prefix(), tenantId, actorId));
        applySchedule(entity, request, tenantId, actorId);
        SchoolScheduleEntity saved = scheduleRepository.save(entity);
        auditLogService.log(tenantId, actorId, "SchoolSchedule", saved.getId(), "CREATE", "Created school schedule");
        return mapper.toSchoolScheduleResponse(saved);
    }

    @Override
    @Transactional
    public SchoolScheduleResponse updateSchedule(Long id, SchoolScheduleUpsertRequest request,
            Long tenantId, Long actorId) {
        SchoolScheduleEntity entity = scheduleRepository.findByIdWithDays(id, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "School schedule not found"));
        entity.markUpdated(actor(actorId));
        applySchedule(entity, request, tenantId, actorId);
        SchoolScheduleEntity saved = scheduleRepository.save(entity);
        auditLogService.log(tenantId, actorId, "SchoolSchedule", saved.getId(), "UPDATE", "Updated school schedule");
        return mapper.toSchoolScheduleResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id, Long tenantId, Long actorId) {
        // Soft-delete child schedule days first
        SchoolScheduleEntity entity = scheduleRepository.findByIdWithDays(id, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "School schedule not found"));
        String actorStr = actor(actorId);
        for (SchoolScheduleDayEntity day : entity.getScheduleDays()) {
            if (!Boolean.TRUE.equals(day.getIsDeleted())) {
                day.setIsDeleted(true);
                day.markUpdated(actorStr);
            }
        }
        scheduleRepository.save(entity);
        // Soft-delete parent schedule
        softDeleteById(scheduleRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "SchoolSchedule", id, "SOFT_DELETE", "Soft deleted school schedule");
    }

    private void applySchedule(SchoolScheduleEntity entity, SchoolScheduleUpsertRequest request,
            Long tenantId, Long actorId) {
        if (request.getArrivalDeadline() == null) {
            throw new AppException(AppErrorCode.Schedule.ARRIVAL_DEADLINE_REQUIRED, messageCommon.getMessage(AppErrorCode.Schedule.ARRIVAL_DEADLINE_REQUIRED));
        }
        if (request.getDepartureTime() == null) {
            throw new AppException(AppErrorCode.Schedule.DEPARTURE_TIME_REQUIRED, messageCommon.getMessage(AppErrorCode.Schedule.DEPARTURE_TIME_REQUIRED));
        }
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new AppException(AppErrorCode.Schedule.EFFECTIVE_DATES_INVALID,
                    messageCommon.getMessage(AppErrorCode.Schedule.EFFECTIVE_DATES_INVALID));
        }
        if (request.getDaysOfWeek() == null || request.getDaysOfWeek().isEmpty()) {
            throw new AppException(AppErrorCode.Schedule.DAYS_OF_WEEK_REQUIRED, messageCommon.getMessage(AppErrorCode.Schedule.DAYS_OF_WEEK_REQUIRED));
        }

        boolean wantsDefault = request.getIsDefault() != null && request.getIsDefault();
        boolean wantsActive = request.getIsActive() == null || request.getIsActive();
        if (wantsDefault && wantsActive) {
            Long excludeId = entity.getId() != null ? entity.getId() : -1L;
            if (scheduleRepository.existsDefaultActiveBySchoolExcluding(
                    entity.getSchool().getId(), tenantId, excludeId)) {
                throw new AppException(AppErrorCode.Schedule.DEFAULT_CONFLICT,
                        messageCommon.getMessage(AppErrorCode.Schedule.DEFAULT_CONFLICT));
            }
        }

        entity.setScheduleName(request.getScheduleName());
        entity.setEducationLevel(request.getEducationLevel());
        entity.setGrade(request.getGrade());
        entity.setShiftType(request.getShiftType());
        entity.setArrivalDeadline(request.getArrivalDeadline());
        entity.setDepartureTime(request.getDepartureTime());
        entity.setEffectiveFrom(request.getEffectiveFrom());
        entity.setEffectiveTo(request.getEffectiveTo());
        entity.setIsDefaultSchedule(wantsDefault);
        entity.setIsActive(wantsActive);

        // Manage schedule days
        syncScheduleDays(entity, request.getDaysOfWeek(), tenantId, actorId);
    }

    private void syncScheduleDays(SchoolScheduleEntity schedule, List<String> daysOfWeek,
            Long tenantId, Long actorId) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            // Soft-delete all existing days
            for (SchoolScheduleDayEntity existing : schedule.getScheduleDays()) {
                if (!Boolean.TRUE.equals(existing.getIsDeleted())) {
                    existing.setIsDeleted(true);
                    existing.markUpdated(actor(actorId));
                }
            }
            return;
        }

        Set<String> requestedDays = daysOfWeek.stream()
                .map(d -> d.trim().toUpperCase())
                .collect(Collectors.toSet());

        // Validate all requested days
        for (String day : requestedDays) {
            if (!VALID_DAYS.contains(day)) {
                throw new AppException(AppErrorCode.Schedule.DAY_INVALID,
                        messageCommon.getMessage(AppErrorCode.Schedule.DAY_INVALID, day));
            }
        }

        String actorStr = actor(actorId);

        // Build a map of existing active (non-deleted) days
        Map<String, SchoolScheduleDayEntity> existingByDay = new HashMap<>();
        // Also include soft-deleted days so we can reactivate them
        Map<String, SchoolScheduleDayEntity> deletedByDay = new HashMap<>();
        for (SchoolScheduleDayEntity existing : schedule.getScheduleDays()) {
            if (Boolean.TRUE.equals(existing.getIsDeleted())) {
                deletedByDay.put(existing.getDayOfWeek(), existing);
            } else {
                existingByDay.put(existing.getDayOfWeek(), existing);
            }
        }

        // Soft-delete days that are no longer requested
        for (var entry : existingByDay.entrySet()) {
            if (!requestedDays.contains(entry.getKey())) {
                entry.getValue().setIsDeleted(true);
                entry.getValue().markUpdated(actorStr);
            }
        }

        // Add or reactivate requested days
        for (String day : requestedDays) {
            if (existingByDay.containsKey(day)) {
                // Already exists and active — no change needed
                continue;
            }
            if (deletedByDay.containsKey(day)) {
                // Reactivate soft-deleted day
                SchoolScheduleDayEntity reactivated = deletedByDay.get(day);
                reactivated.setIsDeleted(false);
                reactivated.setIsActive(true);
                reactivated.markUpdated(actorStr);
            } else {
                // Create new day
                SchoolScheduleDayEntity newDay = new SchoolScheduleDayEntity();
                newDay.markCreated(tenantId, actorStr);
                newDay.setSchedule(schedule);
                newDay.setDayOfWeek(day);
                schedule.getScheduleDays().add(newDay);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolScheduleEntity> getSchedulesForSchools(List<Long> schoolIds, Long tenantId) {
        if (schoolIds == null || schoolIds.isEmpty()) {
            return List.of();
        }
        return scheduleRepository.findBySchoolIdInWithDays(schoolIds, tenantId);
    }
}
