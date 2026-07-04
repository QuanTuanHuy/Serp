package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.dto.params.BusParamsRequest;
import serp.project.school_bus_service.dto.request.BusUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.BusResponse;
import serp.project.school_bus_service.service.IBusService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/buses")
public class BusController extends AbstractBaseController {

    private final IBusService busService;

    public BusController(IBusService busService, AuthUtils authUtils) {
        super(authUtils);
        this.busService = busService;
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.read')")
    public ResponseEntity<GeneralResponse<PageResponse<BusResponse>>> getBuses(
            @ModelAttribute BusParamsRequest params) {
        return ok("Fetched buses", busService.getBuses(params, getCurrentTenantId()));
    }

    @PostMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<BusResponse>> createBus(
            @Valid @RequestBody BusUpsertRequest request) {
        return created("Created bus",
                busService.createBus(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.read')")
    public ResponseEntity<GeneralResponse<BusResponse>> getBus(@PathVariable Long id) {
        return ok("Fetched bus", busService.getBusResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<BusResponse>> updateBus(@PathVariable Long id,
            @Valid @RequestBody BusUpsertRequest request) {
        return ok("Updated bus",
                busService.updateBus(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<Void>> deleteBus(@PathVariable Long id) {
        busService.deleteBus(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted bus");
    }
}
