/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.second_mile.dto.request.InternalRoutePlanRequest;
import serp.project.second_mile.dto.response.InternalRoutePlanResponse;
import serp.project.second_mile.service.RoutePlanningService;

@RestController
@RequestMapping("/api/v1/internal/route-plans")
@RequiredArgsConstructor
public class InternalRoutePlanningController {
    private final RoutePlanningService routePlanningService;

    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER', 'TMS_POSTOFFICER_MANAGER')")
    public InternalRoutePlanResponse planOrderRoute(@Valid @RequestBody InternalRoutePlanRequest request) {
        return routePlanningService.planOrderRoute(request);
    }
}
