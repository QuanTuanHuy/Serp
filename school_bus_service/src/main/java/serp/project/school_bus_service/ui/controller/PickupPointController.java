package serp.project.school_bus_service.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.application.dto.params.PickupPointParamsRequest;
import serp.project.school_bus_service.application.dto.request.PickupPointUpsertRequest;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.PickupPointResponse;
import serp.project.school_bus_service.core.service.IPickupPointService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/pickup-points")
public class PickupPointController extends AbstractBaseController {

    private final IPickupPointService pickupPointService;

    public PickupPointController(IPickupPointService pickupPointService, AuthUtils authUtils) {
        super(authUtils);
        this.pickupPointService = pickupPointService;
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageResponse<PickupPointResponse>>> getPickupPoints(
            @ModelAttribute PickupPointParamsRequest params) {
        return ok("Fetched pickup points", pickupPointService.getPickupPoints(params, getCurrentTenantId()));
    }

    @PostMapping
    public ResponseEntity<GeneralResponse<PickupPointResponse>> createPickupPoint(
            @Valid @RequestBody PickupPointUpsertRequest request) {
        return created("Created pickup point",
                pickupPointService.createPickupPoint(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<PickupPointResponse>> getPickupPoint(@PathVariable Long id) {
        return ok("Fetched pickup point", pickupPointService.getPickupPointResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponse<PickupPointResponse>> updatePickupPoint(@PathVariable Long id,
            @Valid @RequestBody PickupPointUpsertRequest request) {
        return ok("Updated pickup point",
                pickupPointService.updatePickupPoint(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<Void>> deletePickupPoint(@PathVariable Long id) {
        pickupPointService.deletePickupPoint(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted pickup point");
    }
}
