/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.mapper;

import serp.project.second_mile.domain.Route;
import serp.project.second_mile.dto.request.CreateRouteRequest;
import serp.project.second_mile.dto.request.UpdateRouteRequest;
import serp.project.second_mile.dto.response.RouteResponse;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;

public final class RouteMapper {
    private RouteMapper() {
    }

    public static Route toEntity(CreateRouteRequest request) {
        Route route = new Route();
        route.setRouteName(request.getRouteName());
        route.setOriginType(request.getOriginType() == null ? RouteEndpointType.HUB : request.getOriginType());
        route.setOriginHubId(request.getOriginHubId());
        route.setOriginPostOfficeCode(request.getOriginPostOfficeCode());
        route.setDestinationType(request.getDestinationType());
        route.setDestinationHubId(request.getDestinationHubId());
        route.setDestinationPostOfficeCode(request.getDestinationPostOfficeCode());
        route.setVehicleId(request.getVehicleId());
        route.setEstimatedDistanceKm(request.getEstimatedDistanceKm());
        route.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        route.setFixedDepartureTime(request.getFixedDepartureTime());
        route.setStatus(request.getStatus() == null ? RouteStatus.ACTIVE : request.getStatus());
        route.setNote(request.getNote());
        return route;
    }

    public static void mapForUpdate(UpdateRouteRequest request, Route route) {
        route.setRouteName(request.getRouteName());
        route.setOriginType(request.getOriginType() == null ? RouteEndpointType.HUB : request.getOriginType());
        route.setOriginHubId(request.getOriginHubId());
        route.setOriginPostOfficeCode(request.getOriginPostOfficeCode());
        route.setDestinationType(request.getDestinationType());
        route.setDestinationHubId(request.getDestinationHubId());
        route.setDestinationPostOfficeCode(request.getDestinationPostOfficeCode());
        route.setVehicleId(request.getVehicleId());
        route.setEstimatedDistanceKm(request.getEstimatedDistanceKm());
        route.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        route.setFixedDepartureTime(request.getFixedDepartureTime());
        route.setStatus(request.getStatus());
        route.setNote(request.getNote());
    }

    public static RouteResponse toResponse(Route route) {
        return new RouteResponse(
                route.getId(),
                route.getRouteCode(),
                route.getRouteName(),
                route.getOriginType(),
                route.getOriginHubId(),
                route.getOriginPostOfficeCode(),
                route.getDestinationType(),
                route.getDestinationHubId(),
                route.getDestinationPostOfficeCode(),
                route.getVehicleId(),
                route.getEstimatedDistanceKm(),
                route.getEstimatedDurationMinutes(),
                route.getFixedDepartureTime(),
                route.getStatus(),
                route.getNote(),
                route.getCreatedAt(),
                route.getUpdatedAt(),
                route.getCreatedBy(),
                route.getUpdatedBy(),
                route.getTenantId()
        );
    }
}
