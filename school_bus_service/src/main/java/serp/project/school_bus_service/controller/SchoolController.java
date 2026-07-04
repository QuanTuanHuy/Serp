package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.dto.params.SchoolParamsRequest;
import serp.project.school_bus_service.dto.request.SchoolUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.SchoolResponse;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/schools")
public class SchoolController extends AbstractBaseController {

    private final ISchoolService schoolService;

    public SchoolController(ISchoolService schoolService, AuthUtils authUtils) {
        super(authUtils);
        this.schoolService = schoolService;
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.read')")
    public ResponseEntity<GeneralResponse<PageResponse<SchoolResponse>>> getSchools(
            @ModelAttribute SchoolParamsRequest params) {
        return ok("Fetched schools", schoolService.getSchools(params, getCurrentTenantId()));
    }

    @PostMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.write')")
    public ResponseEntity<GeneralResponse<SchoolResponse>> createSchool(
            @Valid @RequestBody SchoolUpsertRequest request) {
        return created("Created school",
                schoolService.createSchool(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.read')")
    public ResponseEntity<GeneralResponse<SchoolResponse>> getSchool(@PathVariable Long id) {
        return ok("Fetched school", schoolService.getSchoolResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.write')")
    public ResponseEntity<GeneralResponse<SchoolResponse>> updateSchool(@PathVariable Long id,
            @Valid @RequestBody SchoolUpsertRequest request) {
        return ok("Updated school",
                schoolService.updateSchool(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.write')")
    public ResponseEntity<GeneralResponse<Void>> deleteSchool(@PathVariable Long id) {
        schoolService.deleteSchool(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted school");
    }
}
