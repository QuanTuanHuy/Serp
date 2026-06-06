package serp.project.school_bus_service.service;
import serp.project.school_bus_service.entity.TripHistoryEntity;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.TripHistoryParamsRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.TripHistoryResponse;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;

public interface ITripHistoryService extends IBaseService<TripHistoryEntity, Long> {

    PageResponse<TripHistoryResponse> getTripHistory(TripHistoryParamsRequest params, Long tenantId);

    /** Record trip history when a route is completed. */
    void recordCompletedRoute(RoutePlanEntity route, RouteAssignmentEntity assignment,
                              Long tenantId, Long actorId);

    long countByTenant(Long tenantId);
}
