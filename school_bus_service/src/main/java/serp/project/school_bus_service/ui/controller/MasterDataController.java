package serp.project.school_bus_service.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.application.dto.response.BusTypeResponse;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.enums.BusTypeEnum;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

import java.util.Arrays;
import java.util.List;

/**
 * Options and reference data controller.
 * CRUD endpoints have been moved to dedicated resource controllers
 * (SchoolController, StudentController, BusController, etc.).
 */
@RestController
public class MasterDataController extends AbstractBaseController {

    public MasterDataController(AuthUtils authUtils) {
        super(authUtils);
    }

    @GetMapping("/bus-types")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.bus.read')")
    public ResponseEntity<GeneralResponse<List<BusTypeResponse>>> getBusTypes() {
        List<BusTypeResponse> busTypes = Arrays.stream(BusTypeEnum.values())
                .map(busType -> new BusTypeResponse(
                        busType.name(),
                        busType.getValue(),
                        busType.getDescription()))
                .toList();
        return ok("Fetched bus types", busTypes);
    }
}
