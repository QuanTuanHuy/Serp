package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.response.FleetSummaryResponse;

public interface IFleetSummaryService {

    FleetSummaryResponse getSummary(Long tenantId);
}
