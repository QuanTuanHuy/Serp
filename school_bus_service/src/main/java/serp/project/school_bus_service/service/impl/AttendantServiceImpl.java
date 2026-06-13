package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.AttendantProfileParamsRequest;
import serp.project.school_bus_service.dto.request.BusAttendantProfileUpsertRequest;
import serp.project.school_bus_service.dto.response.AttendantProfileResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.service.IAttendantService;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;
import serp.project.school_bus_service.repository.BusAttendantProfileRepository;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;
import org.springframework.context.annotation.Lazy;
import serp.project.school_bus_service.service.ISchoolBusUserService;
import serp.project.school_bus_service.entity.SchoolBusUserEntity;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;

@Service
public class AttendantServiceImpl extends AbstractBaseService<BusAttendantProfileEntity, Long> implements IAttendantService {

    private static final Set<String> VALID_CREW_STATUSES = Set.of(
            "AVAILABLE", "ASSIGNED", "INACTIVE", "ON_LEAVE");

    private final BusAttendantProfileRepository busAttendantProfileRepository;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;
    private final ISchoolBusUserService schoolBusUserService;


    public AttendantServiceImpl(
            BusAttendantProfileRepository busAttendantProfileRepository,
            SchoolBusMapper mapper,
            MessageCommon messageCommon,
            @Lazy ISchoolBusUserService schoolBusUserService) {
        this.busAttendantProfileRepository = busAttendantProfileRepository;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
        this.schoolBusUserService = schoolBusUserService;
    }


    @Override
    protected BaseRepository<BusAttendantProfileEntity, Long> getRepository() {
        return busAttendantProfileRepository;
    }

    @Override
    public PageResponse<AttendantProfileResponse> getAttendants(AttendantProfileParamsRequest params, Long tenantId) {
        PageResponse<AttendantProfileResponse> page = PageResponse.from(busAttendantProfileRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "fullName", "phone", "status"),
                PageableUtils.from(params,
                        Set.of("id", "fullName", "status", "createdAt", "updatedAt"), "fullName")),
                mapper::toAttendantProfileResponse);

        // Enrich with SchoolBusUser details
        java.util.List<Long> schoolBusUserIds = page.getItems().stream()
                .map(AttendantProfileResponse::getUserId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        if (!schoolBusUserIds.isEmpty()) {
            Map<Long, SchoolBusUserEntity> userMap = schoolBusUserService.findAllByIds(schoolBusUserIds).stream()
                    .collect(Collectors.toMap(
                            SchoolBusUserEntity::getId,
                            java.util.function.Function.identity()
                    ));
            page.getItems().forEach(r -> {
                Long internalId = r.getUserId();
                r.setSchoolBusUserId(internalId);
                SchoolBusUserEntity u = userMap.get(internalId);
                if (u != null) {
                    r.setAccountUserId(u.getAccountUserId());
                    r.setUserId(u.getAccountUserId()); // Map to accountUserId for UI select dropdown compatibility
                    r.setUser(mapper.toSchoolBusUserResponse(u));
                }
            });
        }
        return page;
    }

    @Override
    public AttendantProfileResponse getAttendantResponse(Long id, Long tenantId) {
        BusAttendantProfileEntity entity = getAttendant(id, tenantId);
        AttendantProfileResponse response = mapper.toAttendantProfileResponse(entity);
        if (response.getUserId() != null) {
            Long internalId = response.getUserId();
            response.setSchoolBusUserId(internalId);
            schoolBusUserService.findById(internalId)
                    .ifPresent(u -> {
                        response.setAccountUserId(u.getAccountUserId());
                        response.setUserId(u.getAccountUserId()); // Map to accountUserId for UI select dropdown compatibility
                        response.setUser(mapper.toSchoolBusUserResponse(u));
                    });
        }
        return response;
    }

    @Override
    public BusAttendantProfileEntity getAttendant(Long id, Long tenantId) {
        return findById(busAttendantProfileRepository, id, tenantId);
    }

    @Override
    @Transactional
    public AttendantProfileResponse createAttendant(BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId) {
        BusAttendantProfileEntity attendant = new BusAttendantProfileEntity();
        attendant.markCreated(tenantId, actor(actorId));
        applyAttendant(attendant, request);
        BusAttendantProfileEntity saved = busAttendantProfileRepository.save(attendant);
        
        // Return enriched response
        AttendantProfileResponse response = mapper.toAttendantProfileResponse(saved);
        if (saved.getUserId() != null) {
            Long internalId = saved.getUserId();
            response.setSchoolBusUserId(internalId);
            schoolBusUserService.findById(internalId)
                    .ifPresent(u -> {
                        response.setAccountUserId(u.getAccountUserId());
                        response.setUserId(u.getAccountUserId());
                        response.setUser(mapper.toSchoolBusUserResponse(u));
                    });
        }
        return response;
    }

    @Override
    @Transactional
    public AttendantProfileResponse updateAttendant(Long id, BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId) {
        BusAttendantProfileEntity attendant = getAttendant(id, tenantId);
        attendant.markUpdated(actor(actorId));
        applyAttendant(attendant, request);
        BusAttendantProfileEntity saved = busAttendantProfileRepository.save(attendant);
        
        // Return enriched response
        AttendantProfileResponse response = mapper.toAttendantProfileResponse(saved);
        if (saved.getUserId() != null) {
            Long internalId = saved.getUserId();
            response.setSchoolBusUserId(internalId);
            schoolBusUserService.findById(internalId)
                    .ifPresent(u -> {
                        response.setAccountUserId(u.getAccountUserId());
                        response.setUserId(u.getAccountUserId());
                        response.setUser(mapper.toSchoolBusUserResponse(u));
                    });
        }
        return response;
    }

    @Override
    @Transactional
    public void deleteAttendant(Long id, Long tenantId, Long actorId) {
        softDeleteById(busAttendantProfileRepository, id, tenantId, actorId);
    }

    private void applyAttendant(BusAttendantProfileEntity attendant, BusAttendantProfileUpsertRequest request) {
        if (request.getStatus() != null && !VALID_CREW_STATUSES.contains(request.getStatus().toUpperCase())) {
            throw new AppException(AppErrorCode.Attendant.INVALID_STATUS,
                    messageCommon.getMessage(AppErrorCode.Attendant.INVALID_STATUS, request.getStatus(), VALID_CREW_STATUSES));
        }
        if (request.getAccountUserId() != null) {
            SchoolBusUserEntity schoolBusUser = schoolBusUserService.getRequiredByAccountUserId(request.getAccountUserId());
            attendant.setUserId(schoolBusUser.getId());
        } else {
            attendant.setUserId(null);
        }
        attendant.setFullName(request.getFullName());
        attendant.setPhone(request.getPhone());
        attendant.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "AVAILABLE");
        attendant.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    @Override
    @Transactional
    public void syncProfile(SchoolBusUserEntity user, boolean hasRole) {
        Optional<BusAttendantProfileEntity> existingOpt = busAttendantProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(user.getTenantId(), user.getId());
        if (hasRole) {
            BusAttendantProfileEntity profile;
            if (existingOpt.isPresent()) {
                profile = existingOpt.get();
            } else {
                profile = new BusAttendantProfileEntity();
                profile.setTenantId(user.getTenantId());
                profile.setUserId(user.getId());
                profile.setStatus("AVAILABLE");
                profile.setIsDeleted(false);
                profile.markCreated(user.getTenantId(), "SYSTEM");
            }
            profile.setFullName(user.getFullName());
            profile.setPhone(user.getPhoneNumber());
            profile.setIsActive(true);
            profile.markUpdated("SYSTEM");
            busAttendantProfileRepository.save(profile);
        } else {
            if (existingOpt.isPresent()) {
                BusAttendantProfileEntity profile = existingOpt.get();
                profile.setIsActive(false);
                profile.markUpdated("SYSTEM");
                busAttendantProfileRepository.save(profile);
            }
        }
    }
}
