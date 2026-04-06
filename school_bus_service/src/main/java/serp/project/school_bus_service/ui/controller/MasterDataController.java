package serp.project.school_bus_service.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.application.dto.request.BusAttendantProfileUpsertRequest;
import serp.project.school_bus_service.application.dto.request.BusUpsertRequest;
import serp.project.school_bus_service.application.dto.request.DriverProfileUpsertRequest;
import serp.project.school_bus_service.application.dto.request.ParentProfileUpsertRequest;
import serp.project.school_bus_service.application.dto.request.PickupPointUpsertRequest;
import serp.project.school_bus_service.application.dto.request.SchoolUpsertRequest;
import serp.project.school_bus_service.application.dto.request.StudentUpsertRequest;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.core.service.IMasterDataService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/school-bus/api/v1")
public class MasterDataController extends AbstractBaseController {

    private final IMasterDataService masterDataService;

    public MasterDataController(IMasterDataService masterDataService, AuthUtils authUtils) {
        super(authUtils);
        this.masterDataService = masterDataService;
    }

    @GetMapping("/schools")
    public ResponseEntity<?> getSchools() {
        return ok("Fetched schools", masterDataService.getSchools(getCurrentTenantId()));
    }

    @PostMapping("/schools")
    public ResponseEntity<?> createSchool(@Valid @RequestBody SchoolUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN");
        return created("Created school", masterDataService.createSchool(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/schools/{id}")
    public ResponseEntity<?> getSchool(@PathVariable Long id) {
        return ok("Fetched school", masterDataService.getSchoolResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/schools/{id}")
    public ResponseEntity<?> updateSchool(@PathVariable Long id, @Valid @RequestBody SchoolUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN");
        return ok("Updated school", masterDataService.updateSchool(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/schools/{id}")
    public ResponseEntity<?> deleteSchool(@PathVariable Long id) {
        requireAnyRole("SCHOOL_BUS_ADMIN");
        masterDataService.deleteSchool(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted school");
    }

    @GetMapping("/parents")
    public ResponseEntity<?> getParents() {
        return ok("Fetched parents", masterDataService.getParents(getCurrentTenantId()));
    }

    @PostMapping("/parents")
    public ResponseEntity<?> createParent(@Valid @RequestBody ParentProfileUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return created("Created parent", masterDataService.createParent(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/parents/{id}")
    public ResponseEntity<?> getParent(@PathVariable Long id) {
        return ok("Fetched parent", masterDataService.getParentResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/parents/{id}")
    public ResponseEntity<?> updateParent(@PathVariable Long id,
            @Valid @RequestBody ParentProfileUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return ok("Updated parent", masterDataService.updateParent(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/parents/{id}")
    public ResponseEntity<?> deleteParent(@PathVariable Long id) {
        requireAnyRole("SCHOOL_BUS_ADMIN");
        masterDataService.deleteParent(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted parent");
    }

    @GetMapping("/students")
    public ResponseEntity<?> getStudents() {
        return ok("Fetched students", masterDataService.getStudents(getCurrentTenantId()));
    }

    @PostMapping("/students")
    public ResponseEntity<?> createStudent(@Valid @RequestBody StudentUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return created("Created student", masterDataService.createStudent(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<?> getStudent(@PathVariable Long id) {
        return ok("Fetched student", masterDataService.getStudentResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/students/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return ok("Updated student", masterDataService.updateStudent(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        requireAnyRole("SCHOOL_BUS_ADMIN");
        masterDataService.deleteStudent(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted student");
    }

    @GetMapping("/buses")
    public ResponseEntity<?> getBuses() {
        return ok("Fetched buses", masterDataService.getBuses(getCurrentTenantId()));
    }

    @PostMapping("/buses")
    public ResponseEntity<?> createBus(@Valid @RequestBody BusUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return created("Created bus", masterDataService.createBus(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/buses/{id}")
    public ResponseEntity<?> getBus(@PathVariable Long id) {
        return ok("Fetched bus", masterDataService.getBusResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/buses/{id}")
    public ResponseEntity<?> updateBus(@PathVariable Long id, @Valid @RequestBody BusUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return ok("Updated bus", masterDataService.updateBus(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/buses/{id}")
    public ResponseEntity<?> deleteBus(@PathVariable Long id) {
        requireAnyRole("SCHOOL_BUS_ADMIN");
        masterDataService.deleteBus(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted bus");
    }

    @GetMapping("/drivers")
    public ResponseEntity<?> getDrivers() {
        return ok("Fetched drivers", masterDataService.getDrivers(getCurrentTenantId()));
    }

    @PostMapping("/drivers")
    public ResponseEntity<?> createDriver(@Valid @RequestBody DriverProfileUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return created("Created driver", masterDataService.createDriver(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/drivers/{id}")
    public ResponseEntity<?> getDriver(@PathVariable Long id) {
        return ok("Fetched driver", masterDataService.getDriverResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/drivers/{id}")
    public ResponseEntity<?> updateDriver(@PathVariable Long id, @Valid @RequestBody DriverProfileUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return ok("Updated driver", masterDataService.updateDriver(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/drivers/{id}")
    public ResponseEntity<?> deleteDriver(@PathVariable Long id) {
        requireAnyRole("SCHOOL_BUS_ADMIN");
        masterDataService.deleteDriver(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted driver");
    }

    @GetMapping("/attendants")
    public ResponseEntity<?> getAttendants() {
        return ok("Fetched attendants", masterDataService.getAttendants(getCurrentTenantId()));
    }

    @PostMapping("/attendants")
    public ResponseEntity<?> createAttendant(@Valid @RequestBody BusAttendantProfileUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return created("Created attendant", masterDataService.createAttendant(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/attendants/{id}")
    public ResponseEntity<?> getAttendant(@PathVariable Long id) {
        return ok("Fetched attendant", masterDataService.getAttendantResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/attendants/{id}")
    public ResponseEntity<?> updateAttendant(@PathVariable Long id,
            @Valid @RequestBody BusAttendantProfileUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return ok("Updated attendant", masterDataService.updateAttendant(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/attendants/{id}")
    public ResponseEntity<?> deleteAttendant(@PathVariable Long id) {
        requireAnyRole("SCHOOL_BUS_ADMIN");
        masterDataService.deleteAttendant(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted attendant");
    }

    @GetMapping("/pickup-points")
    public ResponseEntity<?> getPickupPoints() {
        return ok("Fetched pickup points", masterDataService.getPickupPoints(getCurrentTenantId()));
    }

    @PostMapping("/pickup-points")
    public ResponseEntity<?> createPickupPoint(@Valid @RequestBody PickupPointUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return created("Created pickup point", masterDataService.createPickupPoint(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/pickup-points/{id}")
    public ResponseEntity<?> getPickupPoint(@PathVariable Long id) {
        return ok("Fetched pickup point", masterDataService.getPickupPointResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/pickup-points/{id}")
    public ResponseEntity<?> updatePickupPoint(@PathVariable Long id, @Valid @RequestBody PickupPointUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return ok("Updated pickup point", masterDataService.updatePickupPoint(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/pickup-points/{id}")
    public ResponseEntity<?> deletePickupPoint(@PathVariable Long id) {
        requireAnyRole("SCHOOL_BUS_ADMIN");
        masterDataService.deletePickupPoint(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted pickup point");
    }
}
