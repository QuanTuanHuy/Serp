package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.DepotParamsRequest;
import serp.project.school_bus_service.application.dto.request.DepotUpsertRequest;
import serp.project.school_bus_service.application.dto.response.DepotResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.infrastructure.store.model.DepotEntity;

public interface IDepotService {

    PageResponse<DepotResponse> getDepots(DepotParamsRequest params, Long tenantId);

    DepotResponse getDepotResponse(Long id, Long tenantId);

    DepotEntity getDepot(Long id, Long tenantId);

    DepotResponse createDepot(DepotUpsertRequest request, Long tenantId, Long actorId);

    DepotResponse updateDepot(Long id, DepotUpsertRequest request, Long tenantId, Long actorId);

    void deleteDepot(Long id, Long tenantId, Long actorId);
}
