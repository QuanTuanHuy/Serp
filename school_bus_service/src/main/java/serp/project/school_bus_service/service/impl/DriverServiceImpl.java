package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.DriverProfileParamsRequest;
import serp.project.school_bus_service.dto.request.DriverProfileUpsertRequest;
import serp.project.school_bus_service.dto.response.DriverProfileResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.service.IDriverService;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.DriverProfileEntity;
import serp.project.school_bus_service.repository.DriverProfileRepository;
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
public class DriverServiceImpl extends AbstractBaseService<DriverProfileEntity, Long> implements IDriverService {

    private static final Set<String> VALID_CREW_STATUSES = Set.of(
            "AVAILABLE", "ASSIGNED", "INACTIVE", "ON_LEAVE");

    private final DriverProfileRepository driverProfileRepository;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;
    private final ISchoolBusUserService schoolBusUserService;


    public DriverServiceImpl(
            DriverProfileRepository driverProfileRepository,
            SchoolBusMapper mapper,
            MessageCommon messageCommon,
            @Lazy ISchoolBusUserService schoolBusUserService) {
        this.driverProfileRepository = driverProfileRepository;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
        this.schoolBusUserService = schoolBusUserService;
    }


    @Override
    protected BaseRepository<DriverProfileEntity, Long> getRepository() {
        return driverProfileRepository;
    }

    @Override
    public PageResponse<DriverProfileResponse> getDrivers(DriverProfileParamsRequest params, Long tenantId) {
        PageResponse<DriverProfileResponse> page = PageResponse.from(driverProfileRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "fullName", "phone", "status"),
                PageableUtils.from(params,
                        Set.of("id", "fullName", "status", "createdAt", "updatedAt"),
                        "fullName")),
                mapper::toDriverProfileResponse);

        // Enrich with SchoolBusUser details
        java.util.List<Long> schoolBusUserIds = page.getItems().stream()
                .map(DriverProfileResponse::getUserId)
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
    public DriverProfileResponse getDriverResponse(Long id, Long tenantId) {
        DriverProfileEntity entity = getDriver(id, tenantId);
        DriverProfileResponse response = mapper.toDriverProfileResponse(entity);
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
    public DriverProfileEntity getDriver(Long id, Long tenantId) {
        return findById(driverProfileRepository, id, tenantId);
    }

    @Override
    @Transactional
    public DriverProfileResponse createDriver(DriverProfileUpsertRequest request, Long tenantId, Long actorId) {
        DriverProfileEntity driver = new DriverProfileEntity();
        driver.markCreated(tenantId, actor(actorId));
        applyDriver(driver, request, tenantId);
        DriverProfileEntity saved = driverProfileRepository.save(driver);
        
        // Return enriched response
        DriverProfileResponse response = mapper.toDriverProfileResponse(saved);
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
    public DriverProfileResponse updateDriver(Long id, DriverProfileUpsertRequest request, Long tenantId, Long actorId) {
        DriverProfileEntity driver = getDriver(id, tenantId);
        driver.markUpdated(actor(actorId));
        applyDriver(driver, request, tenantId);
        DriverProfileEntity saved = driverProfileRepository.save(driver);
        
        // Return enriched response
        DriverProfileResponse response = mapper.toDriverProfileResponse(saved);
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
    public void deleteDriver(Long id, Long tenantId, Long actorId) {
        softDeleteById(driverProfileRepository, id, tenantId, actorId);
    }

    private void applyDriver(DriverProfileEntity driver, DriverProfileUpsertRequest request, Long tenantId) {
        if (request.getStatus() != null && !VALID_CREW_STATUSES.contains(request.getStatus().toUpperCase())) {
            throw new AppException(AppErrorCode.Driver.INVALID_STATUS,
                    messageCommon.getMessage(AppErrorCode.Driver.INVALID_STATUS, request.getStatus(), VALID_CREW_STATUSES));
        }
        String normalizedStatus = request.getStatus() != null ? request.getStatus().toUpperCase() : "AVAILABLE";
        if (request.getAccountUserId() != null) {
            SchoolBusUserEntity schoolBusUser = schoolBusUserService.getRequiredByAccountUserId(request.getAccountUserId());
            driver.setUserId(schoolBusUser.getId());
        } else {
            driver.setUserId(null);
        }
        driver.setFullName(request.getFullName());
        driver.setPhone(request.getPhone());
        driver.setStatus(normalizedStatus);
        driver.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    @Override
    @Transactional
    public void syncProfile(SchoolBusUserEntity user, boolean hasRole) {
        Optional<DriverProfileEntity> existingOpt = driverProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(user.getTenantId(), user.getId());
        if (hasRole) {
            DriverProfileEntity profile;
            if (existingOpt.isPresent()) {
                profile = existingOpt.get();
            } else {
                profile = new DriverProfileEntity();
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
            driverProfileRepository.save(profile);
        } else {
            if (existingOpt.isPresent()) {
                DriverProfileEntity profile = existingOpt.get();
                profile.setIsActive(false);
                profile.markUpdated("SYSTEM");
                driverProfileRepository.save(profile);
            }
        }
    }
}
