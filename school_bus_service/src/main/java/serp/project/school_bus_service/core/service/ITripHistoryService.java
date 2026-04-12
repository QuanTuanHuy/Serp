package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.TripHistoryParamsRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.TripHistoryResponse;

public interface ITripHistoryService {

    PageResponse<TripHistoryResponse> getTripHistory(TripHistoryParamsRequest params, Long tenantId);
}
