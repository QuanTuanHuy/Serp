package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.response.RouteManualValidationResponse;

public interface IRouteManualValidationService {

    /**
     * Runs timeline/geometry validation on a route, updates issues in database, and returns validation response DTO.
     */
    RouteManualValidationResponse validateRoute(Long routePlanId, Long tenantId);

    /**
     * Verifies that the route has no blocking issues before assigning resources. Throws AppException if violations exist.
     */
    void validateBeforeAssignResources(Long routePlanId, Long tenantId);
}
