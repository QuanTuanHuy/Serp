package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.BusParamsRequest;
import serp.project.school_bus_service.dto.request.BusUpsertRequest;
import serp.project.school_bus_service.dto.response.BusResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.entity.BusEntity;

public interface IBusService extends IBaseService<BusEntity, Long> {

    PageResponse<BusResponse> getBuses(BusParamsRequest params, Long tenantId);

    BusResponse getBusResponse(Long id, Long tenantId);

    BusEntity getBus(Long id, Long tenantId);

    BusResponse createBus(BusUpsertRequest request, Long tenantId, Long actorId);

    BusResponse updateBus(Long id, BusUpsertRequest request, Long tenantId, Long actorId);

    void deleteBus(Long id, Long tenantId, Long actorId);

    long countByTenant(Long tenantId);
}
