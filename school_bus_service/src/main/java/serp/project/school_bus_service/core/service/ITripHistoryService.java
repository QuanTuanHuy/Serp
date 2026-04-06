package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.response.TripHistoryResponse;

import java.util.List;

public interface ITripHistoryService {

    List<TripHistoryResponse> getTripHistory(Long tenantId);
}
