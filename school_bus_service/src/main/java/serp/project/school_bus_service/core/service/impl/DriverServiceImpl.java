package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.DriverProfileParamsRequest;
import serp.project.school_bus_service.application.dto.request.DriverProfileUpsertRequest;
import serp.project.school_bus_service.application.dto.response.DriverProfileResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.IDriverService;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.DriverProfileEntity;
import serp.project.school_bus_service.infrastructure.store.repository.DriverProfileRepository;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl extends AbstractBaseService<DriverProfileEntity, Long> implements IDriverService {

    private final DriverProfileRepository driverProfileRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;

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
        applyDriver(driver, request);
        DriverProfileEntity saved = driverProfileRepository.save(driver);
        auditLogService.log(tenantId, actorId, "DriverProfile", saved.getId(), "CREATE", "Created driver profile");
        return mapper.toDriverProfileResponse(saved);
    }

    @Override
    @Transactional
    public DriverProfileResponse updateDriver(Long id, DriverProfileUpsertRequest request, Long tenantId, Long actorId) {
        DriverProfileEntity driver = getDriver(id, tenantId);
        driver.markUpdated(actor(actorId));
        applyDriver(driver, request);
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

    private void applyDriver(DriverProfileEntity driver, DriverProfileUpsertRequest request) {
        driver.setUserId(request.getUserId());
        driver.setFullName(request.getFullName());
        driver.setPhone(request.getPhone());
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setStatus(request.getStatus());
        driver.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }
}
