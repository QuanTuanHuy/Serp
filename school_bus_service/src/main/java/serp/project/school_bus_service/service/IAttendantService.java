package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.AttendantProfileParamsRequest;
import serp.project.school_bus_service.dto.request.BusAttendantProfileUpsertRequest;
import serp.project.school_bus_service.dto.response.AttendantProfileResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;

public interface IAttendantService extends IBaseService<BusAttendantProfileEntity, Long> {

    PageResponse<AttendantProfileResponse> getAttendants(AttendantProfileParamsRequest params, Long tenantId);

    AttendantProfileResponse getAttendantResponse(Long id, Long tenantId);

    BusAttendantProfileEntity getAttendant(Long id, Long tenantId);

    AttendantProfileResponse createAttendant(BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId);

    AttendantProfileResponse updateAttendant(Long id, BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId);

    void deleteAttendant(Long id, Long tenantId, Long actorId);
}
