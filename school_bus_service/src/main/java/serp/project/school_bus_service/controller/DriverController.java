package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.dto.params.DriverProfileParamsRequest;
import serp.project.school_bus_service.dto.request.DriverProfileUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.DriverProfileResponse;
import serp.project.school_bus_service.service.IDriverService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/drivers")
public class DriverController extends AbstractBaseController {

    private final IDriverService driverService;

    public DriverController(IDriverService driverService, AuthUtils authUtils) {
        super(authUtils);
        this.driverService = driverService;
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.read')")
    public ResponseEntity<GeneralResponse<PageResponse<DriverProfileResponse>>> getDrivers(
            @ModelAttribute DriverProfileParamsRequest params) {
        return ok("Fetched drivers", driverService.getDrivers(params, getCurrentTenantId()));
    }

    @PostMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<DriverProfileResponse>> createDriver(
            @Valid @RequestBody DriverProfileUpsertRequest request) {
        return created("Created driver",
                driverService.createDriver(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.read')")
    public ResponseEntity<GeneralResponse<DriverProfileResponse>> getDriver(@PathVariable Long id) {
        return ok("Fetched driver", driverService.getDriverResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<DriverProfileResponse>> updateDriver(@PathVariable Long id,
            @Valid @RequestBody DriverProfileUpsertRequest request) {
        return ok("Updated driver",
                driverService.updateDriver(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<Void>> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted driver");
    }
}
