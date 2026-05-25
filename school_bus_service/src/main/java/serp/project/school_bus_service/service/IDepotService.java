package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.DepotParamsRequest;
import serp.project.school_bus_service.dto.request.DepotUpsertRequest;
import serp.project.school_bus_service.dto.response.DepotResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.entity.DepotEntity;

public interface IDepotService extends IBaseService<DepotEntity, Long> {

    PageResponse<DepotResponse> getDepots(DepotParamsRequest params, Long tenantId);

    DepotResponse getDepotResponse(Long id, Long tenantId);

    DepotEntity getDepot(Long id, Long tenantId);

    DepotResponse createDepot(DepotUpsertRequest request, Long tenantId, Long actorId);

    DepotResponse updateDepot(Long id, DepotUpsertRequest request, Long tenantId, Long actorId);

    void deleteDepot(Long id, Long tenantId, Long actorId);
}
