package serp.project.school_bus_service.service;
import serp.project.school_bus_service.entity.SchoolPickupPointWindowEntity;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.request.SchoolPickupPointWindowUpsertRequest;
import serp.project.school_bus_service.dto.response.SchoolPickupPointWindowResponse;

import java.util.List;

public interface ISchoolPickupPointWindowService extends IBaseService<SchoolPickupPointWindowEntity, Long> {

    /** Get all active windows for a linked pickup point */
    List<SchoolPickupPointWindowResponse> getBySchoolPickupPoint(Long schoolPickupPointId, Long tenantId);

    /** Get all active windows for a school schedule (across all linked pickup points) */
    List<SchoolPickupPointWindowResponse> getBySchedule(Long schoolScheduleId, Long tenantId);

    /** Create a new window (schoolPickupPointId is in the request body) */
    SchoolPickupPointWindowResponse create(SchoolPickupPointWindowUpsertRequest request, Long tenantId, Long actorId);

    /** Update an existing window */
    SchoolPickupPointWindowResponse update(Long windowId,
            SchoolPickupPointWindowUpsertRequest request, Long tenantId, Long actorId);

    /** Soft-delete a window */
    void delete(Long windowId, Long tenantId, Long actorId);

    /** Soft-delete all active windows for a specific linked pickup point */
    void softDeleteWindowsBySchoolPickupPointId(Long schoolPickupPointId, Long tenantId, Long actorId);

    /** Soft-delete all active windows for a specific schedule */
    void softDeleteWindowsByScheduleId(Long schoolScheduleId, Long tenantId, Long actorId);

    /**
     * Check whether a time window exists for the given linked pickup point + schedule + direction.
     * Used by request validation to warn when no window is configured.
     */
    boolean hasWindow(Long schoolPickupPointId, Long schoolScheduleId, String direction, Long tenantId);

    /** Batch: find which point IDs have a valid window for the school+schedule+direction. */
    List<Long> findPointIdsWithWindow(Long schoolId, List<Long> pointIds, Long scheduleId, String direction, Long tenantId);

    List<SchoolPickupPointWindowEntity> getWindowsForLinks(List<Long> linkIds, Long tenantId);

    java.util.Optional<SchoolPickupPointWindowEntity> findWindow(Long schoolId, Long pointId, Long scheduleId, String direction, Long tenantId);
}
