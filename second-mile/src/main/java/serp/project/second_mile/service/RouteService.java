/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.CreateRouteRequest;
import serp.project.second_mile.dto.request.RouteFilterRequest;
import serp.project.second_mile.dto.request.UpdateRouteRequest;
import serp.project.second_mile.dto.response.RouteResponse;

public interface RouteService {
    PageResponse<RouteResponse> getRoutes(int page, int size, RouteFilterRequest filterRequest);

    RouteResponse getRouteById(Long id);

    RouteResponse createRoute(CreateRouteRequest request);

    RouteResponse updateRoute(Long id, UpdateRouteRequest request);

    void deleteRoute(Long id);
}
