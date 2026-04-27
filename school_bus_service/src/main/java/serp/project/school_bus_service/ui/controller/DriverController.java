package serp.project.school_bus_service.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.application.dto.params.DriverProfileParamsRequest;
import serp.project.school_bus_service.application.dto.request.DriverProfileUpsertRequest;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.DriverProfileResponse;
import serp.project.school_bus_service.core.service.IDriverService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/drivers")
public class DriverController extends AbstractBaseController {

    private final IDriverService driverService;

    public DriverController(IDriverService driverService, AuthUtils authUtils) {
        super(authUtils);
        this.driverService = driverService;
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageResponse<DriverProfileResponse>>> getDrivers(
            @ModelAttribute DriverProfileParamsRequest params) {
        return ok("Fetched drivers", driverService.getDrivers(params, getCurrentTenantId()));
    }

    @PostMapping
    public ResponseEntity<GeneralResponse<DriverProfileResponse>> createDriver(
            @Valid @RequestBody DriverProfileUpsertRequest request) {
        return created("Created driver",
                driverService.createDriver(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<DriverProfileResponse>> getDriver(@PathVariable Long id) {
        return ok("Fetched driver", driverService.getDriverResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponse<DriverProfileResponse>> updateDriver(@PathVariable Long id,
            @Valid @RequestBody DriverProfileUpsertRequest request) {
        return ok("Updated driver",
                driverService.updateDriver(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<Void>> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted driver");
    }
}
