package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.request.SchoolScheduleUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.SchoolScheduleResponse;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;

import java.util.List;

public interface ISchoolScheduleService extends IBaseService<SchoolScheduleEntity, Long> {

    PageResponse<SchoolScheduleResponse> getSchedulesBySchool(Long schoolId, int page, int size, Long tenantId);

    List<SchoolScheduleResponse> getActiveSchedulesBySchool(Long schoolId, Long tenantId);

    SchoolScheduleResponse getScheduleResponse(Long id, Long tenantId);

    SchoolScheduleEntity getSchedule(Long id, Long tenantId);

    /** Load schedule with its days populated (for validation purposes). */
    SchoolScheduleEntity getScheduleWithDays(Long id, Long tenantId);

    SchoolScheduleResponse createSchedule(Long schoolId, SchoolScheduleUpsertRequest request, Long tenantId, Long actorId);

    SchoolScheduleResponse updateSchedule(Long id, SchoolScheduleUpsertRequest request, Long tenantId, Long actorId);

    void deleteSchedule(Long id, Long tenantId, Long actorId);
}
