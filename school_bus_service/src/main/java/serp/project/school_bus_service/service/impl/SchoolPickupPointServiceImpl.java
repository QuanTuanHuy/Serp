package serp.project.school_bus_service.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.SchoolPickupPointUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.SchoolPickupPointResponse;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.IPickupPointService;
import serp.project.school_bus_service.service.ISchoolPickupPointService;
import serp.project.school_bus_service.service.ISchoolPickupPointWindowService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;
import serp.project.school_bus_service.repository.SchoolPickupPointRepository;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import serp.project.school_bus_service.dto.response.SchoolPickupPointWindowResponse;
import serp.project.school_bus_service.dto.response.SchoolPickupPointCompatibilityResponse;
import serp.project.school_bus_service.entity.SchoolPickupPointWindowEntity;

@Service
public class SchoolPickupPointServiceImpl extends AbstractBaseService<SchoolPickupPointEntity, Long>
        implements ISchoolPickupPointService {

    private final SchoolPickupPointRepository repository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;
    private final ISchoolService schoolService;
    private final IPickupPointService pickupPointService;
    private final ISchoolPickupPointWindowService windowService;
    private final MessageCommon messageCommon;

    public SchoolPickupPointServiceImpl(
            SchoolPickupPointRepository repository,
            @Lazy SchoolBusMapper mapper,
            IAuditLogService auditLogService,
            ISchoolService schoolService,
            IPickupPointService pickupPointService,
            @Lazy ISchoolPickupPointWindowService windowService,
            MessageCommon messageCommon) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
        this.schoolService = schoolService;
        this.pickupPointService = pickupPointService;
        this.windowService = windowService;
        this.messageCommon = messageCommon;
    }

    @Override
    protected BaseRepository<SchoolPickupPointEntity, Long> getRepository() {
        return repository;
    }

    @Override
    public SchoolPickupPointEntity getSchoolPickupPoint(Long id, Long tenantId) {
        return findById(repository, id, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SchoolPickupPointResponse> getBySchool(Long schoolId, int page, int size, Long tenantId) {
        PageResponse<SchoolPickupPointResponse> pageResponse = PageResponse.from(
                repository.findBySchoolIdAndTenantIdAndIsDeletedFalse(
                        schoolId, tenantId, PageRequest.of(page, size, Sort.by("pickupPoint.name"))),
                mapper::toSchoolPickupPointResponse);
        enrichLinksWithWindows(pageResponse.getItems(), tenantId);
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolPickupPointResponse> getActiveBySchool(Long schoolId, Long tenantId) {
        List<SchoolPickupPointResponse> list = repository.findBySchoolIdAndTenantIdAndIsDeletedFalseAndIsActiveTrue(schoolId, tenantId)
                .stream().map(mapper::toSchoolPickupPointResponse).toList();
        enrichLinksWithWindows(list, tenantId);
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolPickupPointResponse> getAllActiveLinks(Long tenantId) {
        List<SchoolPickupPointResponse> list = repository.findByTenantIdAndIsDeletedFalseAndIsActiveTrue(tenantId)
                .stream().map(mapper::toSchoolPickupPointResponse).toList();
        enrichLinksWithWindows(list, tenantId);
        return list;
    }

    private void enrichLinksWithWindows(List<SchoolPickupPointResponse> linkResponses, Long tenantId) {
        if (linkResponses == null || linkResponses.isEmpty()) {
            return;
        }
        List<Long> linkIds = linkResponses.stream().map(SchoolPickupPointResponse::getId).toList();
        List<SchoolPickupPointWindowEntity> windowEntities = windowService.getWindowsForLinks(linkIds, tenantId);
        
        Map<Long, List<SchoolPickupPointWindowResponse>> windowsByLinkId = windowEntities.stream()
                .map(mapper::toSchoolPickupPointWindowResponse)
                .collect(Collectors.groupingBy(SchoolPickupPointWindowResponse::getSchoolPickupPointId));

        for (SchoolPickupPointResponse linkResponse : linkResponses) {
            List<SchoolPickupPointWindowResponse> windows = windowsByLinkId.get(linkResponse.getId());
            linkResponse.setWindows(windows != null ? windows : List.of());
        }
    }

    @Override
    @Transactional
    public SchoolPickupPointResponse link(Long schoolId, SchoolPickupPointUpsertRequest request,
            Long tenantId, Long actorId) {
        SchoolEntity school = schoolService.getSchool(schoolId, tenantId);
        PickupPointEntity pickupPoint = pickupPointService.getPickupPoint(request.getPickupPointId(), tenantId);

        // Check if already linked
        repository.findBySchoolIdAndPickupPointIdAndTenantIdAndIsDeletedFalse(
                schoolId, request.getPickupPointId(), tenantId)
                .ifPresent(existing -> {
                    throw new AppException(AppErrorCode.SchoolPickupPoint.CONFLICT,
                            messageCommon.getMessage(AppErrorCode.SchoolPickupPoint.CONFLICT));
                });

        SchoolPickupPointEntity entity = new SchoolPickupPointEntity();
        entity.markCreated(tenantId, actor(actorId));
        entity.setSchool(school);
        entity.setPickupPoint(pickupPoint);
        applyFields(entity, request);
        SchoolPickupPointEntity saved = repository.save(entity);
        auditLogService.log(tenantId, actorId, "SchoolPickupPoint", saved.getId(), "CREATE",
                "Linked pickup point " + pickupPoint.getName() + " to school " + school.getName());
        return mapper.toSchoolPickupPointResponse(saved);
    }

    @Override
    @Transactional
    public SchoolPickupPointResponse update(Long id, SchoolPickupPointUpsertRequest request,
            Long tenantId, Long actorId) {
        SchoolPickupPointEntity entity = repository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, messageCommon.getMessage("schoolPickupPoint.notFound")));
        entity.markUpdated(actor(actorId));
        applyFields(entity, request);
        SchoolPickupPointEntity saved = repository.save(entity);
        auditLogService.log(tenantId, actorId, "SchoolPickupPoint", saved.getId(), "UPDATE",
                "Updated school-pickup-point link config");
        return mapper.toSchoolPickupPointResponse(saved);
    }

    @Override
    @Transactional
    public void unlink(Long schoolId, Long pickupPointId, Long tenantId, Long actorId) {
        SchoolPickupPointEntity entity = repository.findBySchoolIdAndPickupPointIdAndTenantIdAndIsDeletedFalse(
                schoolId, pickupPointId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND,
                        messageCommon.getMessage("schoolPickupPoint.linkNotFound")));
        entity.setIsDeleted(true);
        entity.setIsActive(false);
        entity.markUpdated(actor(actorId));
        repository.save(entity);
        // Cascade soft-delete all active windows for this linked pickup point
        windowService.softDeleteWindowsBySchoolPickupPointId(entity.getId(), tenantId, actorId);
        auditLogService.log(tenantId, actorId, "SchoolPickupPoint", entity.getId(), "SOFT_DELETE",
                "Unlinked pickup point from school");
    }

    @Override
    public boolean isPickupPointLinkedToSchool(Long schoolId, Long pickupPointId, Long tenantId) {
        return repository.existsBySchoolIdAndPickupPointIdAndTenantIdAndIsDeletedFalseAndIsActiveTrue(
                schoolId, pickupPointId, tenantId);
    }

    @Override
    public Optional<SchoolPickupPointEntity> findLinkBySchoolAndPickupPoint(
            Long schoolId, Long pickupPointId, Long tenantId) {
        return repository.findBySchoolIdAndPickupPointIdAndTenantIdAndIsDeletedFalse(
                schoolId, pickupPointId, tenantId);
    }

    @Override
    public boolean isLinkedAndActive(Long schoolId, Long pickupPointId, Long tenantId) {
        return repository.existsBySchoolIdAndPickupPointIdAndTenantIdAndIsDeletedFalseAndIsActiveTrue(
                schoolId, pickupPointId, tenantId);
    }

    private void applyFields(SchoolPickupPointEntity entity, SchoolPickupPointUpsertRequest request) {
        // After V12, windows are managed via school_bus_school_pickup_point_window table.
        // This link entity only manages the school<->pickup_point relationship and default flag.
        entity.setIsDefaultPoint(request.getIsDefault() != null ? request.getIsDefault() : false);
        entity.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolPickupPointEntity> getPickupPointLinksForSchools(List<Long> schoolIds, Long tenantId) {
        if (schoolIds == null || schoolIds.isEmpty()) {
            return List.of();
        }
        return repository.findBySchoolIdInAndTenantIdAndIsDeletedFalse(schoolIds, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolPickupPointCompatibilityResponse> getCompatibility(Long schoolId, Long schoolScheduleId, Long tenantId) {
        List<SchoolPickupPointEntity> links = repository.findBySchoolIdAndTenantIdAndIsDeletedFalseAndIsActiveTrue(schoolId, tenantId);
        List<SchoolPickupPointCompatibilityResponse> result = new ArrayList<>();
        if (links.isEmpty()) {
            return result;
        }

        List<Long> linkIds = links.stream().map(SchoolPickupPointEntity::getId).toList();
        List<SchoolPickupPointWindowEntity> windowEntities = windowService.getWindowsForLinks(linkIds, tenantId);

        Map<Long, Map<String, SchoolPickupPointWindowEntity>> windowsByLinkId = new HashMap<>();
        for (SchoolPickupPointWindowEntity win : windowEntities) {
            if (schoolScheduleId != null) {
                if (win.getSchoolSchedule() == null || !schoolScheduleId.equals(win.getSchoolSchedule().getId())) {
                    continue;
                }
            }
            if (win.getSchoolPickupPoint() != null) {
                windowsByLinkId.computeIfAbsent(win.getSchoolPickupPoint().getId(), k -> new HashMap<>())
                        .put(win.getDirection(), win);
            }
        }

        for (SchoolPickupPointEntity link : links) {
            PickupPointEntity pp = link.getPickupPoint();
            if (pp == null || Boolean.TRUE.equals(pp.getIsDeleted())) {
                continue;
            }

            SchoolPickupPointCompatibilityResponse resp = new SchoolPickupPointCompatibilityResponse();
            resp.setPickupPointId(pp.getId());
            resp.setPickupPointCode(pp.getCode());
            resp.setPickupPointName(pp.getName());

            boolean hasCoords = pp.getLatitude() != null && pp.getLongitude() != null;
            String usage = pp.getUsageType() != null ? pp.getUsageType().toUpperCase() : "PICKUP_DROPOFF";
            boolean supportsPickup = usage.equals("PICKUP_ONLY") || usage.equals("PICKUP_DROPOFF");
            boolean supportsDropoff = usage.equals("DROPOFF_ONLY") || usage.equals("PICKUP_DROPOFF");

            resp.setUsageType(usage);
            resp.setHasCoordinates(hasCoords);

            Map<String, SchoolPickupPointWindowEntity> directionsMap = windowsByLinkId.get(link.getId());

            // ── Pickup Compatibility ─────────────────────────────────────────
            if (!supportsPickup) {
                resp.setPickupReadinessCode("UNSUPPORTED_USAGE_TYPE");
                resp.setPickupReadinessLabel("Unsupported usage type");
                resp.setPickupReadinessStatus("Unsupported usage");
                resp.setPickupMissingConfigReason("Usage type " + usage + " does not support PICKUP");
                resp.setCompatibleForPickup(false);
            } else if (!hasCoords) {
                resp.setPickupReadinessCode("MISSING_COORDINATES");
                resp.setPickupReadinessLabel("Missing coordinates");
                resp.setPickupReadinessStatus("Missing coordinates");
                resp.setPickupMissingConfigReason("Missing coordinates");
                resp.setCompatibleForPickup(false);
            } else if (schoolScheduleId != null) {
                SchoolPickupPointWindowEntity pickupWin = directionsMap != null ? directionsMap.get("PICKUP_TO_SCHOOL") : null;
                if (pickupWin != null) {
                    resp.setPickupReadinessCode("READY");
                    resp.setPickupReadinessLabel("Ready");
                    resp.setPickupReadinessStatus("Ready");
                    resp.setPickupWindowStart(pickupWin.getWindowStart());
                    resp.setPickupWindowEnd(pickupWin.getWindowEnd());
                    resp.setCompatibleForPickup(true);
                } else {
                    resp.setPickupReadinessCode("MISSING_PICKUP_WINDOW");
                    resp.setPickupReadinessLabel("Missing pickup window");
                    resp.setPickupReadinessStatus("Missing window");
                    resp.setPickupMissingConfigReason("Missing PICKUP_TO_SCHOOL window");
                    resp.setCompatibleForPickup(false);
                }
            } else {
                // No schedule selected — window cannot be checked
                resp.setPickupReadinessCode("NOT_CHECKED");
                resp.setPickupReadinessLabel("Select schedule to check window");
                resp.setPickupReadinessStatus("Ready");
                resp.setCompatibleForPickup(true);
            }

            // ── Dropoff Compatibility ────────────────────────────────────────
            if (!supportsDropoff) {
                resp.setDropoffReadinessCode("UNSUPPORTED_USAGE_TYPE");
                resp.setDropoffReadinessLabel("Unsupported usage type");
                resp.setDropoffReadinessStatus("Unsupported usage");
                resp.setDropoffMissingConfigReason("Usage type " + usage + " does not support DROPOFF");
                resp.setCompatibleForDropoff(false);
            } else if (!hasCoords) {
                resp.setDropoffReadinessCode("MISSING_COORDINATES");
                resp.setDropoffReadinessLabel("Missing coordinates");
                resp.setDropoffReadinessStatus("Missing coordinates");
                resp.setDropoffMissingConfigReason("Missing coordinates");
                resp.setCompatibleForDropoff(false);
            } else if (schoolScheduleId != null) {
                SchoolPickupPointWindowEntity dropoffWin = directionsMap != null ? directionsMap.get("DROPOFF_FROM_SCHOOL") : null;
                if (dropoffWin != null) {
                    resp.setDropoffReadinessCode("READY");
                    resp.setDropoffReadinessLabel("Ready");
                    resp.setDropoffReadinessStatus("Ready");
                    resp.setDropoffWindowStart(dropoffWin.getWindowStart());
                    resp.setDropoffWindowEnd(dropoffWin.getWindowEnd());
                    resp.setCompatibleForDropoff(true);
                } else {
                    resp.setDropoffReadinessCode("MISSING_DROPOFF_WINDOW");
                    resp.setDropoffReadinessLabel("Missing drop-off window");
                    resp.setDropoffReadinessStatus("Missing window");
                    resp.setDropoffMissingConfigReason("Missing DROPOFF_FROM_SCHOOL window");
                    resp.setCompatibleForDropoff(false);
                }
            } else {
                // No schedule selected — window cannot be checked
                resp.setDropoffReadinessCode("NOT_CHECKED");
                resp.setDropoffReadinessLabel("Select schedule to check window");
                resp.setDropoffReadinessStatus("Ready");
                resp.setCompatibleForDropoff(true);
            }

            result.add(resp);
        }

        return result;
    }
}
