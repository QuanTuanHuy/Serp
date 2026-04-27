package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.PickupPointParamsRequest;
import serp.project.school_bus_service.application.dto.request.PickupPointUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.PickupPointResponse;
import serp.project.school_bus_service.infrastructure.store.model.PickupPointEntity;

public interface IPickupPointService {

    PageResponse<PickupPointResponse> getPickupPoints(PickupPointParamsRequest params, Long tenantId);

    PickupPointResponse getPickupPointResponse(Long id, Long tenantId);

    PickupPointEntity getPickupPoint(Long id, Long tenantId);

    PickupPointResponse createPickupPoint(PickupPointUpsertRequest request, Long tenantId, Long actorId);

    PickupPointResponse updatePickupPoint(Long id, PickupPointUpsertRequest request, Long tenantId, Long actorId);

    void deletePickupPoint(Long id, Long tenantId, Long actorId);
}
