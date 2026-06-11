package serp.project.school_bus_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.dto.response.DropdownOptionResponse;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.service.ISchoolBusDropdownService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

import java.util.List;

@RestController
@RequestMapping("/dropdowns")
public class SchoolBusDropdownController extends AbstractBaseController {

    private final ISchoolBusDropdownService dropdownService;

    public SchoolBusDropdownController(ISchoolBusDropdownService dropdownService, AuthUtils authUtils) {
        super(authUtils);
        this.dropdownService = dropdownService;
    }

    @GetMapping("/schools")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dropdown.school')")
    public ResponseEntity<GeneralResponse<List<DropdownOptionResponse>>> getSchoolsDropdown() {
        return ok("Fetched schools dropdown", dropdownService.getSchoolsDropdown(getCurrentTenantId()));
    }

    @GetMapping("/school-pickup-points")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dropdown.school-pickup-point')")
    public ResponseEntity<GeneralResponse<List<DropdownOptionResponse>>> getSchoolPickupPointsDropdown(
            @RequestParam Long schoolId) {
        return ok("Fetched school pickup points dropdown", dropdownService.getSchoolPickupPointsDropdown(schoolId, getCurrentTenantId()));
    }

    @GetMapping("/parents")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dropdown.parent')")
    public ResponseEntity<GeneralResponse<List<DropdownOptionResponse>>> getParentsDropdown() {
        return ok("Fetched parents dropdown", dropdownService.getParentsDropdown(getCurrentTenantId()));
    }

    @GetMapping("/drivers")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dropdown.driver')")
    public ResponseEntity<GeneralResponse<List<DropdownOptionResponse>>> getDriversDropdown() {
        return ok("Fetched drivers dropdown", dropdownService.getDriversDropdown(getCurrentTenantId()));
    }

    @GetMapping("/attendants")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dropdown.attendant')")
    public ResponseEntity<GeneralResponse<List<DropdownOptionResponse>>> getAttendantsDropdown() {
        return ok("Fetched attendants dropdown", dropdownService.getAttendantsDropdown(getCurrentTenantId()));
    }

    @GetMapping("/buses")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dropdown.bus')")
    public ResponseEntity<GeneralResponse<List<DropdownOptionResponse>>> getBusesDropdown(
            @RequestParam(required = false) Long depotId) {
        return ok("Fetched buses dropdown", dropdownService.getBusesDropdown(depotId, getCurrentTenantId()));
    }
}
