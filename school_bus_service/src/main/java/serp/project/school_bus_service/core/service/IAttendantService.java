package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.AttendantProfileParamsRequest;
import serp.project.school_bus_service.application.dto.request.BusAttendantProfileUpsertRequest;
import serp.project.school_bus_service.application.dto.response.AttendantProfileResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.infrastructure.store.model.BusAttendantProfileEntity;

public interface IAttendantService {

    PageResponse<AttendantProfileResponse> getAttendants(AttendantProfileParamsRequest params, Long tenantId);

    AttendantProfileResponse getAttendantResponse(Long id, Long tenantId);

    BusAttendantProfileEntity getAttendant(Long id, Long tenantId);

    AttendantProfileResponse createAttendant(BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId);

    AttendantProfileResponse updateAttendant(Long id, BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId);

    void deleteAttendant(Long id, Long tenantId, Long actorId);
}
