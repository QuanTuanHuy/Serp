package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.BusParamsRequest;
import serp.project.school_bus_service.application.dto.request.BusUpsertRequest;
import serp.project.school_bus_service.application.dto.response.BusResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.infrastructure.store.model.BusEntity;

public interface IBusService {

    PageResponse<BusResponse> getBuses(BusParamsRequest params, Long tenantId);

    BusResponse getBusResponse(Long id, Long tenantId);

    BusEntity getBus(Long id, Long tenantId);

    BusResponse createBus(BusUpsertRequest request, Long tenantId, Long actorId);

    BusResponse updateBus(Long id, BusUpsertRequest request, Long tenantId, Long actorId);

    void deleteBus(Long id, Long tenantId, Long actorId);
}
