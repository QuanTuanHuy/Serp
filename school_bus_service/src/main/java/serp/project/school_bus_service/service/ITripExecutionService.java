package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.TripExecutionParamsRequest;
import serp.project.school_bus_service.dto.request.CancelTripRequest;
import serp.project.school_bus_service.dto.request.CompleteTripRequest;
import serp.project.school_bus_service.dto.request.SkipStopRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.TripExecutionListItemResponse;
import serp.project.school_bus_service.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.dto.response.TripListSummaryResponse;
import serp.project.school_bus_service.dto.response.TripStopLogResponse;
import serp.project.school_bus_service.dto.response.TripStudentResponse;
import serp.project.school_bus_service.entity.TripExecutionEntity;

import serp.project.school_bus_service.enums.TripStatus;

import java.util.List;

public interface ITripExecutionService extends IBaseService<TripExecutionEntity, Long> {

    PageResponse<TripExecutionListItemResponse> getTrips(TripExecutionParamsRequest params, Long tenantId);

    TripListSummaryResponse getSummary(Long tenantId);

    TripExecutionResponse getTrip(Long id, Long tenantId);

    TripExecutionEntity getTripEntity(Long id, Long tenantId);

    TripExecutionResponse createTripFromRoute(Long routeId, Long tenantId, Long actorId);

    TripExecutionResponse startTrip(Long id, Long tenantId, Long actorId);

    TripExecutionResponse arriveStop(Long id, Long routeStopId, Long tenantId, Long actorId);

    TripExecutionResponse departStop(Long id, Long routeStopId, Long tenantId, Long actorId);

    TripExecutionResponse skipStop(Long id, Long routeStopId, SkipStopRequest request, Long tenantId, Long actorId);

    TripExecutionResponse completeTrip(Long id, CompleteTripRequest request, Long tenantId, Long actorId);

    TripExecutionResponse cancelTrip(Long id, CancelTripRequest request, Long tenantId, Long actorId);

    List<TripStopLogResponse> getTripStops(Long id, Long tenantId);

    List<TripStudentResponse> getTripStudents(Long id, Long tenantId);

    long countByTenantAndStatus(Long tenantId, TripStatus status);

    boolean existsByRoute(Long routeId, Long tenantId);

    TripExecutionEntity save(TripExecutionEntity entity);
}

