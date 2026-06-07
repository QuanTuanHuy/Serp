package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.response.ObjectiveScoreResponse;

public interface IRouteObjectiveScoringService {

    /**
     * Calculates the objective score details for a specific route.
     *
     * @param routePlanId ID of the route
     * @param tenantId    tenant ID
     * @return objective score response DTO
     */
    ObjectiveScoreResponse calculateRouteScore(Long routePlanId, Long tenantId);

    /**
     * Calculates the overall solution objective score details for a planning session.
     *
     * @param sessionId ID of the route planning session
     * @param tenantId  tenant ID
     * @return objective score response DTO
     */
    ObjectiveScoreResponse calculateSolutionScore(Long sessionId, Long tenantId);
}
