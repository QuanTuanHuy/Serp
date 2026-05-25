package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.ParentProfileParamsRequest;
import serp.project.school_bus_service.dto.request.ParentProfileUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.ParentProfileResponse;
import serp.project.school_bus_service.entity.ParentProfileEntity;

public interface IParentService extends IBaseService<ParentProfileEntity, Long> {

    PageResponse<ParentProfileResponse> getParents(ParentProfileParamsRequest params, Long tenantId);

    ParentProfileResponse getParentResponse(Long id, Long tenantId);

    ParentProfileEntity getParent(Long id, Long tenantId);

    ParentProfileResponse createParent(ParentProfileUpsertRequest request, Long tenantId, Long actorId);

    ParentProfileResponse updateParent(Long id, ParentProfileUpsertRequest request, Long tenantId, Long actorId);

    void deleteParent(Long id, Long tenantId, Long actorId);

    long countByTenant(Long tenantId);
}
