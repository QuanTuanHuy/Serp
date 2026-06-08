package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.request.SchoolPickupPointUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.SchoolPickupPointResponse;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;

import java.util.List;
import java.util.Optional;

public interface ISchoolPickupPointService extends IBaseService<SchoolPickupPointEntity, Long> {

    SchoolPickupPointEntity getSchoolPickupPoint(Long id, Long tenantId);

    PageResponse<SchoolPickupPointResponse> getBySchool(Long schoolId, int page, int size, Long tenantId);

    List<SchoolPickupPointResponse> getActiveBySchool(Long schoolId, Long tenantId);

    List<SchoolPickupPointResponse> getAllActiveLinks(Long tenantId);

    SchoolPickupPointResponse link(Long schoolId, SchoolPickupPointUpsertRequest request, Long tenantId, Long actorId);

    SchoolPickupPointResponse update(Long id, SchoolPickupPointUpsertRequest request, Long tenantId, Long actorId);

    void unlink(Long schoolId, Long pickupPointId, Long tenantId, Long actorId);

    boolean isPickupPointLinkedToSchool(Long schoolId, Long pickupPointId, Long tenantId);

    Optional<SchoolPickupPointEntity> findLinkBySchoolAndPickupPoint(Long schoolId, Long pickupPointId, Long tenantId);

    List<SchoolPickupPointEntity> getPickupPointLinksForSchools(List<Long> schoolIds, Long tenantId);
}
