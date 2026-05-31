package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.PickupPointParamsRequest;
import serp.project.school_bus_service.dto.request.PickupPointUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.PickupPointResponse;
import serp.project.school_bus_service.entity.PickupPointEntity;

public interface IPickupPointService extends IBaseService<PickupPointEntity, Long> {

    PageResponse<PickupPointResponse> getPickupPoints(PickupPointParamsRequest params, Long tenantId);

    PickupPointResponse getPickupPointResponse(Long id, Long tenantId);

    PickupPointEntity getPickupPoint(Long id, Long tenantId);

    PickupPointResponse createPickupPoint(PickupPointUpsertRequest request, Long tenantId, Long actorId);

    PickupPointResponse updatePickupPoint(Long id, PickupPointUpsertRequest request, Long tenantId, Long actorId);

    void deletePickupPoint(Long id, Long tenantId, Long actorId);
}
