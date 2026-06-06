package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.DriverProfileParamsRequest;
import serp.project.school_bus_service.dto.request.DriverProfileUpsertRequest;
import serp.project.school_bus_service.dto.response.DriverProfileResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.service.IAuditLogService;
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

import java.time.LocalDate;

import java.util.Set;

@Service
public class DriverServiceImpl extends AbstractBaseService<DriverProfileEntity, Long> implements IDriverService {

    private static final Set<String> VALID_CREW_STATUSES = Set.of(
            "AVAILABLE", "ASSIGNED", "INACTIVE", "ON_LEAVE");

    private final DriverProfileRepository driverProfileRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;
    private final MessageCommon messageCommon;


    public DriverServiceImpl(
    DriverProfileRepository driverProfileRepository,
                                 SchoolBusMapper mapper,
                                 IAuditLogService auditLogService,
                                 MessageCommon messageCommon) {
        this.driverProfileRepository = driverProfileRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
        this.messageCommon = messageCommon;
    }


    @Override
    protected BaseRepository<DriverProfileEntity, Long> getRepository() {
        return driverProfileRepository;
    }

    @Override
    public PageResponse<DriverProfileResponse> getDrivers(DriverProfileParamsRequest params, Long tenantId) {
        return PageResponse.from(driverProfileRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "fullName", "phone", "licenseNumber", "status"),
                PageableUtils.from(params,
                        Set.of("id", "fullName", "licenseNumber", "status", "createdAt", "updatedAt"),
                        "fullName")),
                mapper::toDriverProfileResponse);
    }

    @Override
    public DriverProfileResponse getDriverResponse(Long id, Long tenantId) {
        return mapper.toDriverProfileResponse(getDriver(id, tenantId));
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
        auditLogService.log(tenantId, actorId, "DriverProfile", saved.getId(), "CREATE", "Created driver profile");
        return mapper.toDriverProfileResponse(saved);
    }

    @Override
    @Transactional
    public DriverProfileResponse updateDriver(Long id, DriverProfileUpsertRequest request, Long tenantId, Long actorId) {
        DriverProfileEntity driver = getDriver(id, tenantId);
        driver.markUpdated(actor(actorId));
        applyDriver(driver, request, tenantId);
        DriverProfileEntity saved = driverProfileRepository.save(driver);
        auditLogService.log(tenantId, actorId, "DriverProfile", saved.getId(), "UPDATE", "Updated driver profile");
        return mapper.toDriverProfileResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDriver(Long id, Long tenantId, Long actorId) {
        softDeleteById(driverProfileRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "DriverProfile", id, "SOFT_DELETE", "Soft deleted driver profile");
    }

    private void applyDriver(DriverProfileEntity driver, DriverProfileUpsertRequest request, Long tenantId) {
        if (request.getStatus() != null && !VALID_CREW_STATUSES.contains(request.getStatus().toUpperCase())) {
            throw new AppException(AppErrorCode.Driver.INVALID_STATUS,
                    messageCommon.getMessage(AppErrorCode.Driver.INVALID_STATUS, request.getStatus(), VALID_CREW_STATUSES));
        }
        // License number uniqueness (exclude self on update)
        Long excludeId = driver.getId() != null ? driver.getId() : -1L;
        if (driverProfileRepository.existsByLicenseNumberAndTenantIdAndIsDeletedFalseAndIdNot(
                request.getLicenseNumber(), tenantId, excludeId)) {
            throw new AppException(AppErrorCode.Driver.LICENSE_CONFLICT,
                    messageCommon.getMessage(AppErrorCode.Driver.LICENSE_CONFLICT, request.getLicenseNumber()));
        }
        // License expiry check for operational statuses
        String normalizedStatus = request.getStatus() != null ? request.getStatus().toUpperCase() : "AVAILABLE";
        if (("AVAILABLE".equals(normalizedStatus) || "ASSIGNED".equals(normalizedStatus))
                && request.getLicenseExpiryDate() != null
                && request.getLicenseExpiryDate().isBefore(LocalDate.now())) {
            throw new AppException(AppErrorCode.Driver.LICENSE_EXPIRED,
                    messageCommon.getMessage(AppErrorCode.Driver.LICENSE_EXPIRED, normalizedStatus));
        }
        driver.setUserId(request.getUserId());
        driver.setFullName(request.getFullName());
        driver.setPhone(request.getPhone());
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setLicenseClass(request.getLicenseClass());
        driver.setLicenseExpiryDate(request.getLicenseExpiryDate());
        driver.setStatus(normalizedStatus);
        driver.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }
}
