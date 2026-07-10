/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.dto.request.InternalRoutePlanRequest;
import serp.project.second_mile.dto.response.InternalRoutePlanResponse;

public interface RoutePlanningService {
    InternalRoutePlanResponse planOrderRoute(InternalRoutePlanRequest request);
}
