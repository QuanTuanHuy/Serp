package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.SchoolParamsRequest;
import serp.project.school_bus_service.dto.request.SchoolUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.SchoolResponse;
import serp.project.school_bus_service.entity.SchoolEntity;

public interface ISchoolService extends IBaseService<SchoolEntity, Long> {

    PageResponse<SchoolResponse> getSchools(SchoolParamsRequest params, Long tenantId);

    SchoolResponse getSchoolResponse(Long id, Long tenantId);

    SchoolEntity getSchool(Long id, Long tenantId);

    SchoolResponse createSchool(SchoolUpsertRequest request, Long tenantId, Long actorId);

    SchoolResponse updateSchool(Long id, SchoolUpsertRequest request, Long tenantId, Long actorId);

    void deleteSchool(Long id, Long tenantId, Long actorId);

    long countByTenant(Long tenantId);
}
