/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.second_mile.dto.ApiResponse;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.CreateRouteRequest;
import serp.project.second_mile.dto.request.RouteFilterRequest;
import serp.project.second_mile.dto.request.UpdateRouteRequest;
import serp.project.second_mile.dto.response.RouteResponse;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;
import serp.project.second_mile.exception.MessageService;
import serp.project.second_mile.service.RouteService;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {
    private final RouteService routeService;
    private final MessageService messageService;

    @GetMapping
    public ApiResponse<PageResponse<RouteResponse>> getRoutes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "route_code", required = false) String routeCode,
            @RequestParam(name = "origin_type", required = false) RouteEndpointType originType,
            @RequestParam(name = "origin_hub_id", required = false) Long originHubId,
            @RequestParam(name = "origin_post_office_code", required = false) String originPostOfficeCode,
            @RequestParam(name = "destination_type", required = false) RouteDestinationType destinationType,
            @RequestParam(name = "destination_hub_id", required = false) Long destinationHubId,
            @RequestParam(name = "destination_post_office_code", required = false) String destinationPostOfficeCode,
            @RequestParam(name = "vehicle_id", required = false) Long vehicleId,
            @RequestParam(required = false) RouteStatus status
    ) {
        RouteFilterRequest filterRequest = RouteFilterRequest.builder()
                .keyword(keyword)
                .routeCode(routeCode)
                .originType(originType)
                .originHubId(originHubId)
                .originPostOfficeCode(originPostOfficeCode)
                .destinationType(destinationType)
                .destinationHubId(destinationHubId)
                .destinationPostOfficeCode(destinationPostOfficeCode)
                .vehicleId(vehicleId)
                .status(status)
                .build();

        return ApiResponse.<PageResponse<RouteResponse>>builder()
                .message(messageService.getMessage("success.routes.list"))
                .result(routeService.getRoutes(page, size, filterRequest))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<RouteResponse> getRouteById(@PathVariable Long id) {
        return ApiResponse.<RouteResponse>builder()
                .message(messageService.getMessage("success.routes.detail"))
                .result(routeService.getRouteById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<RouteResponse> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        return ApiResponse.<RouteResponse>builder()
                .message(messageService.getMessage("success.routes.create"))
                .result(routeService.createRoute(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<RouteResponse> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRouteRequest request
    ) {
        return ApiResponse.<RouteResponse>builder()
                .message(messageService.getMessage("success.routes.update"))
                .result(routeService.updateRoute(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ApiResponse.<Void>builder()
                .message(messageService.getMessage("success.routes.delete"))
                .build();
    }
}
