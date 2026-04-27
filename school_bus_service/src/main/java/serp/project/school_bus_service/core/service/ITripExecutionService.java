package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.TripExecutionParamsRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.application.dto.response.TripStopLogResponse;
import serp.project.school_bus_service.application.dto.response.TripStudentResponse;
import serp.project.school_bus_service.infrastructure.store.model.TripExecutionEntity;

import java.util.List;

public interface ITripExecutionService {

    PageResponse<TripExecutionResponse> getTrips(TripExecutionParamsRequest params, Long tenantId);

    TripExecutionResponse getTrip(Long id, Long tenantId);

    TripExecutionEntity getTripEntity(Long id, Long tenantId);

    TripExecutionResponse createTripFromRoute(Long routeId, Long tenantId, Long actorId);

    TripExecutionResponse startTrip(Long id, Long tenantId, Long actorId);

    TripExecutionResponse arriveStop(Long id, Long routeStopId, Long tenantId, Long actorId);

    TripExecutionResponse departStop(Long id, Long routeStopId, Long tenantId, Long actorId);

    TripExecutionResponse completeTrip(Long id, Long tenantId, Long actorId);

    List<TripStopLogResponse> getTripStops(Long id, Long tenantId);

    List<TripStudentResponse> getTripStudents(Long id, Long tenantId);
}

