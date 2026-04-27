package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.ParentProfileParamsRequest;
import serp.project.school_bus_service.application.dto.request.ParentProfileUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.ParentProfileResponse;
import serp.project.school_bus_service.infrastructure.store.model.ParentProfileEntity;

public interface IParentService {

    PageResponse<ParentProfileResponse> getParents(ParentProfileParamsRequest params, Long tenantId);

    ParentProfileResponse getParentResponse(Long id, Long tenantId);

    ParentProfileEntity getParent(Long id, Long tenantId);

    ParentProfileResponse createParent(ParentProfileUpsertRequest request, Long tenantId, Long actorId);

    ParentProfileResponse updateParent(Long id, ParentProfileUpsertRequest request, Long tenantId, Long actorId);

    void deleteParent(Long id, Long tenantId, Long actorId);
}
