package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.DriverProfileParamsRequest;
import serp.project.school_bus_service.application.dto.request.DriverProfileUpsertRequest;
import serp.project.school_bus_service.application.dto.response.DriverProfileResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.infrastructure.store.model.DriverProfileEntity;

public interface IDriverService {

    PageResponse<DriverProfileResponse> getDrivers(DriverProfileParamsRequest params, Long tenantId);

    DriverProfileResponse getDriverResponse(Long id, Long tenantId);

    DriverProfileEntity getDriver(Long id, Long tenantId);

    DriverProfileResponse createDriver(DriverProfileUpsertRequest request, Long tenantId, Long actorId);

    DriverProfileResponse updateDriver(Long id, DriverProfileUpsertRequest request, Long tenantId, Long actorId);

    void deleteDriver(Long id, Long tenantId, Long actorId);
}
