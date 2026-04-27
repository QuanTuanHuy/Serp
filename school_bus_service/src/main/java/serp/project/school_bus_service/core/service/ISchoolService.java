package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.SchoolParamsRequest;
import serp.project.school_bus_service.application.dto.request.SchoolUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.SchoolResponse;
import serp.project.school_bus_service.infrastructure.store.model.SchoolEntity;

public interface ISchoolService {

    PageResponse<SchoolResponse> getSchools(SchoolParamsRequest params, Long tenantId);

    SchoolResponse getSchoolResponse(Long id, Long tenantId);

    SchoolEntity getSchool(Long id, Long tenantId);

    SchoolResponse createSchool(SchoolUpsertRequest request, Long tenantId, Long actorId);

    SchoolResponse updateSchool(Long id, SchoolUpsertRequest request, Long tenantId, Long actorId);

    void deleteSchool(Long id, Long tenantId, Long actorId);
}
