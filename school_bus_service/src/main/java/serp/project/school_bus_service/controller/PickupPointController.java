package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.dto.params.PickupPointParamsRequest;
import serp.project.school_bus_service.dto.request.PickupPointUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.PickupPointResponse;
import serp.project.school_bus_service.service.IPickupPointService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/pickup-points")
public class PickupPointController extends AbstractBaseController {

    private final IPickupPointService pickupPointService;

    public PickupPointController(IPickupPointService pickupPointService, AuthUtils authUtils) {
        super(authUtils);
        this.pickupPointService = pickupPointService;
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.read')")
    public ResponseEntity<GeneralResponse<PageResponse<PickupPointResponse>>> getPickupPoints(
            @ModelAttribute PickupPointParamsRequest params) {
        return ok("Fetched pickup points", pickupPointService.getPickupPoints(params, getCurrentTenantId()));
    }

    @PostMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<PickupPointResponse>> createPickupPoint(
            @Valid @RequestBody PickupPointUpsertRequest request) {
        return created("Created pickup point",
                pickupPointService.createPickupPoint(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.read')")
    public ResponseEntity<GeneralResponse<PickupPointResponse>> getPickupPoint(@PathVariable Long id) {
        return ok("Fetched pickup point", pickupPointService.getPickupPointResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<PickupPointResponse>> updatePickupPoint(@PathVariable Long id,
            @Valid @RequestBody PickupPointUpsertRequest request) {
        return ok("Updated pickup point",
                pickupPointService.updatePickupPoint(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<Void>> deletePickupPoint(@PathVariable Long id) {
        pickupPointService.deletePickupPoint(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted pickup point");
    }
}
