package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.DriverProfileParamsRequest;
import serp.project.school_bus_service.dto.request.DriverProfileUpsertRequest;
import serp.project.school_bus_service.dto.response.DriverProfileResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.entity.DriverProfileEntity;

public interface IDriverService extends IBaseService<DriverProfileEntity, Long> {

    PageResponse<DriverProfileResponse> getDrivers(DriverProfileParamsRequest params, Long tenantId);

    DriverProfileResponse getDriverResponse(Long id, Long tenantId);

    DriverProfileEntity getDriver(Long id, Long tenantId);

    DriverProfileResponse createDriver(DriverProfileUpsertRequest request, Long tenantId, Long actorId);

    DriverProfileResponse updateDriver(Long id, DriverProfileUpsertRequest request, Long tenantId, Long actorId);

    void deleteDriver(Long id, Long tenantId, Long actorId);
}
