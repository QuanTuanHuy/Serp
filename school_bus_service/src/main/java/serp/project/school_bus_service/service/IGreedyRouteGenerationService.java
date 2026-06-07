package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.request.GreedyGenerateRequest;
import serp.project.school_bus_service.dto.response.GreedyGenerateResponse;

public interface IGreedyRouteGenerationService {

    /**
     * Generates a set of initial routes for a planning session using a greedy insertion heuristic.
     *
     * @param sessionId ID of the route planning session
     * @param request   greedy parameters (capacity, depot)
     * @param tenantId  tenant ID
     * @param actorId   actor ID
     * @return summary response containing generated routes, unassigned students, and issues
     */
    GreedyGenerateResponse generateRoutes(Long sessionId, GreedyGenerateRequest request, Long tenantId, Long actorId);
}
