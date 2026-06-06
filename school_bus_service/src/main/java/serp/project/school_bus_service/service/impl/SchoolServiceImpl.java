package serp.project.school_bus_service.service.impl;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.SchoolParamsRequest;
import serp.project.school_bus_service.dto.request.SchoolUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.SchoolResponse;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.repository.SchoolRepository;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;
import org.springframework.context.annotation.Lazy;
import serp.project.school_bus_service.service.ISchoolScheduleService;
import serp.project.school_bus_service.service.ISchoolPickupPointService;
import serp.project.school_bus_service.service.ISchoolPickupPointWindowService;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;
import serp.project.school_bus_service.entity.SchoolPickupPointWindowEntity;
import serp.project.school_bus_service.entity.SchoolScheduleDayEntity;
import serp.project.school_bus_service.dto.response.SchoolScheduleSummaryResponse;
import serp.project.school_bus_service.dto.response.LinkedPickupPointSummaryResponse;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class SchoolServiceImpl extends AbstractBaseService<SchoolEntity, Long> implements ISchoolService {

    private final SchoolRepository schoolRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;
    private final ICodeGeneratorService codeGeneratorService;
    private final MessageCommon messageCommon;
    private final ISchoolScheduleService scheduleService;
    private final ISchoolPickupPointService pickupPointService;
    private final ISchoolPickupPointWindowService windowService;

    public SchoolServiceImpl(SchoolRepository schoolRepository,
                             SchoolBusMapper mapper,
                             IAuditLogService auditLogService,
                             ICodeGeneratorService codeGeneratorService,
                             MessageCommon messageCommon,
                             @Lazy ISchoolScheduleService scheduleService,
                             @Lazy ISchoolPickupPointService pickupPointService,
                             @Lazy ISchoolPickupPointWindowService windowService) {
        this.schoolRepository = schoolRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
        this.codeGeneratorService = codeGeneratorService;
        this.messageCommon = messageCommon;
        this.scheduleService = scheduleService;
        this.pickupPointService = pickupPointService;
        this.windowService = windowService;
    }

    @Override
    protected BaseRepository<SchoolEntity, Long> getRepository() {
        return schoolRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SchoolResponse> getSchools(SchoolParamsRequest params, Long tenantId) {
        PageResponse<SchoolResponse> pageResponse = PageResponse.from(schoolRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "name", "code", "address", "contactPhone", "contactEmail"),
                PageableUtils.from(params,
                        Set.of("id", "name", "code", "createdAt", "updatedAt"), "name")),
                mapper::toSchoolResponse);
        enrichSchoolResponses(pageResponse.getItems(), tenantId);
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolResponse getSchoolResponse(Long id, Long tenantId) {
        SchoolResponse response = mapper.toSchoolResponse(getSchool(id, tenantId));
        enrichSchoolResponses(List.of(response), tenantId);
        return response;
    }

    private void enrichSchoolResponses(List<SchoolResponse> responses, Long tenantId) {
        if (responses == null || responses.isEmpty()) {
            return;
        }

        List<Long> schoolIds = responses.stream().map(SchoolResponse::getId).toList();

        // 1. Fetch schedules for these schoolIds
        List<SchoolScheduleEntity> allSchedules = scheduleService.getSchedulesForSchools(schoolIds, tenantId);
        Map<Long, List<SchoolScheduleEntity>> schedulesBySchool = allSchedules.stream()
                .collect(Collectors.groupingBy(s -> s.getSchool().getId()));

        // 2. Fetch linked pickup points for these schoolIds
        List<SchoolPickupPointEntity> allLinks = pickupPointService.getPickupPointLinksForSchools(schoolIds, tenantId);
        Map<Long, List<SchoolPickupPointEntity>> linksBySchool = allLinks.stream()
                .collect(Collectors.groupingBy(l -> l.getSchool().getId()));

        // 3. Fetch windows for all active/inactive links in batch to avoid N+1 query
        List<Long> linkIds = allLinks.stream().map(SchoolPickupPointEntity::getId).toList();
        Map<Long, List<SchoolPickupPointWindowEntity>> windowsByLinkId = new HashMap<>();
        if (!linkIds.isEmpty()) {
            List<SchoolPickupPointWindowEntity> allWindows = windowService.getWindowsForLinks(linkIds, tenantId);
            windowsByLinkId = allWindows.stream()
                    .collect(Collectors.groupingBy(w -> w.getSchoolPickupPoint().getId()));
        }

        for (SchoolResponse resp : responses) {
            Long schoolId = resp.getId();

            // Set coordinates flags
            resp.setHasCoordinates(resp.getLatitude() != null && resp.getLongitude() != null);

            // Populate schedules
            List<SchoolScheduleEntity> schoolSchedules = schedulesBySchool.getOrDefault(schoolId, List.of());
            resp.setScheduleCount(schoolSchedules.size());
            resp.setActiveScheduleCount(schoolSchedules.stream().filter(s -> Boolean.TRUE.equals(s.getIsActive())).count());

            List<SchoolScheduleSummaryResponse> schedulesSummary = new ArrayList<>();
            for (SchoolScheduleEntity sch : schoolSchedules) {
                SchoolScheduleSummaryResponse summary = new SchoolScheduleSummaryResponse();
                summary.setId(sch.getId());
                summary.setCode(sch.getScheduleCode());
                summary.setName(sch.getScheduleName());
                summary.setShift(sch.getShiftType());
                summary.setArrivalDeadline(sch.getArrivalDeadline());
                summary.setDepartureTime(sch.getDepartureTime());
                summary.setEffectiveFrom(sch.getEffectiveFrom());
                summary.setEffectiveTo(sch.getEffectiveTo());
                summary.setIsDefault(sch.getIsDefaultSchedule());
                summary.setIsActive(sch.getIsActive());

                List<String> days = sch.getScheduleDays().stream()
                        .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                        .map(SchoolScheduleDayEntity::getDayOfWeek)
                        .toList();
                summary.setDays(days);
                schedulesSummary.add(summary);
            }
            resp.setSchedules(schedulesSummary);

            // Populate pickup points
            List<SchoolPickupPointEntity> schoolLinks = linksBySchool.getOrDefault(schoolId, List.of());
            resp.setPickupPointCount(schoolLinks.size());

            boolean anyMissingCoordinates = false;
            long windowCount = 0;
            List<LinkedPickupPointSummaryResponse> pickupPointsSummary = new ArrayList<>();
            for (SchoolPickupPointEntity link : schoolLinks) {
                LinkedPickupPointSummaryResponse summary = new LinkedPickupPointSummaryResponse();
                summary.setLinkId(link.getId());
                summary.setIsDefault(link.getIsDefaultPoint());

                var pt = link.getPickupPoint();
                if (pt != null) {
                    summary.setId(pt.getId());
                    summary.setCode(pt.getCode());
                    summary.setName(pt.getName());
                    summary.setAddress(pt.getAddress());
                    summary.setUsageType(pt.getUsageType());
                    summary.setLatitude(pt.getLatitude());
                    summary.setLongitude(pt.getLongitude());
                    boolean hasCoords = pt.getLatitude() != null && pt.getLongitude() != null;
                    summary.setHasCoordinates(hasCoords);
                    if (!hasCoords) {
                        anyMissingCoordinates = true;
                    }
                } else {
                    anyMissingCoordinates = true;
                }

                pickupPointsSummary.add(summary);

                // Count windows for this link
                List<SchoolPickupPointWindowEntity> linkWindows = windowsByLinkId.getOrDefault(link.getId(), List.of());
                windowCount += linkWindows.size();
            }
            resp.setPickupPoints(pickupPointsSummary);
            resp.setAnyLinkedPointMissingCoordinates(anyMissingCoordinates);
            resp.setPickupWindowCount(windowCount);
        }
    }

    @Override
    public SchoolEntity getSchool(Long id, Long tenantId) {
        return findById(schoolRepository, id, tenantId);
    }

    @Override
    @Transactional
    public SchoolResponse createSchool(SchoolUpsertRequest request, Long tenantId, Long actorId) {
        SchoolEntity school = new SchoolEntity();
        school.markCreated(tenantId, actor(actorId));
        applySchool(school, request);
        school.setCode(codeGeneratorService.generate(
                SchoolBusCode.SCHOOL.sequenceKey(), SchoolBusCode.SCHOOL.prefix(), tenantId, actorId));
        SchoolEntity saved = schoolRepository.save(school);
        auditLogService.log(tenantId, actorId, "School", saved.getId(), "CREATE", "Created school master data");
        return mapper.toSchoolResponse(saved);
    }

    @Override
    @Transactional
    public SchoolResponse updateSchool(Long id, SchoolUpsertRequest request, Long tenantId, Long actorId) {
        SchoolEntity school = getSchool(id, tenantId);
        school.markUpdated(actor(actorId));
        applySchool(school, request);
        SchoolEntity saved = schoolRepository.save(school);
        auditLogService.log(tenantId, actorId, "School", saved.getId(), "UPDATE", "Updated school master data");
        return mapper.toSchoolResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSchool(Long id, Long tenantId, Long actorId) {
        softDeleteById(schoolRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "School", id, "SOFT_DELETE", "Soft deleted school");
    }

    private void applySchool(SchoolEntity school, SchoolUpsertRequest request) {
        validateCoordinatePair(request.getLatitude(), request.getLongitude(), "school");
        if (request.getContactEmail() != null && !request.getContactEmail().isBlank()
                && !request.getContactEmail().matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new AppException(AppErrorCode.School.EMAIL_INVALID, messageCommon.getMessage(AppErrorCode.School.EMAIL_INVALID));
        }
        school.setName(request.getName());
        school.setAddress(request.getAddress());
        school.setContactPhone(request.getContactPhone());
        school.setContactEmail(request.getContactEmail());
        school.setLatitude(request.getLatitude());
        school.setLongitude(request.getLongitude());
        school.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void validateCoordinatePair(Double latitude, Double longitude, String target) {
        if ((latitude == null) != (longitude == null)) {
            throw new AppException(
                    AppErrorCode.Coordinate.BOTH_REQUIRED,
                    messageCommon.getMessage(AppErrorCode.Coordinate.BOTH_REQUIRED, target));
        }

        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new AppException(
                    AppErrorCode.Coordinate.LATITUDE_RANGE,
                    messageCommon.getMessage(AppErrorCode.Coordinate.LATITUDE_RANGE, target));
        }

        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new AppException(
                    AppErrorCode.Coordinate.LONGITUDE_RANGE,
                    messageCommon.getMessage(AppErrorCode.Coordinate.LONGITUDE_RANGE, target));
        }
    }

    @Override
    public long countByTenant(Long tenantId) {
        return schoolRepository.countByTenantIdAndIsDeletedFalse(tenantId);
    }
}
