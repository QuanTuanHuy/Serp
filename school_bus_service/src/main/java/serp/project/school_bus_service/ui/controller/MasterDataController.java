package serp.project.school_bus_service.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.application.dto.params.AttendantProfileParamsRequest;
import serp.project.school_bus_service.application.dto.params.BusParamsRequest;
import serp.project.school_bus_service.application.dto.params.DepotParamsRequest;
import serp.project.school_bus_service.application.dto.params.DriverProfileParamsRequest;
import serp.project.school_bus_service.application.dto.params.ParentProfileParamsRequest;
import serp.project.school_bus_service.application.dto.params.PickupPointParamsRequest;
import serp.project.school_bus_service.application.dto.params.SchoolParamsRequest;
import serp.project.school_bus_service.application.dto.params.StudentParamsRequest;
import serp.project.school_bus_service.application.dto.request.BusAttendantProfileUpsertRequest;
import serp.project.school_bus_service.application.dto.request.BusUpsertRequest;
import serp.project.school_bus_service.application.dto.request.DepotUpsertRequest;
import serp.project.school_bus_service.application.dto.request.DriverProfileUpsertRequest;
import serp.project.school_bus_service.application.dto.request.ParentProfileUpsertRequest;
import serp.project.school_bus_service.application.dto.request.PickupPointUpsertRequest;
import serp.project.school_bus_service.application.dto.request.SchoolUpsertRequest;
import serp.project.school_bus_service.application.dto.request.StudentUpsertRequest;
import serp.project.school_bus_service.application.dto.response.AttendantProfileResponse;
import serp.project.school_bus_service.application.dto.response.BusResponse;
import serp.project.school_bus_service.application.dto.response.BusTypeResponse;
import serp.project.school_bus_service.application.dto.response.DepotResponse;
import serp.project.school_bus_service.application.dto.response.DriverProfileResponse;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.ParentProfileResponse;
import serp.project.school_bus_service.application.dto.response.PickupPointResponse;
import serp.project.school_bus_service.application.dto.response.SchoolResponse;
import serp.project.school_bus_service.application.dto.response.StudentResponse;
import serp.project.school_bus_service.core.service.IMasterDataService;
import serp.project.school_bus_service.enums.BusTypeEnum;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping
public class MasterDataController extends AbstractBaseController {

    private final IMasterDataService masterDataService;

    public MasterDataController(IMasterDataService masterDataService, AuthUtils authUtils) {
        super(authUtils);
        this.masterDataService = masterDataService;
    }

    @GetMapping("/schools")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.read')")
    public ResponseEntity<GeneralResponse<PageResponse<SchoolResponse>>> getSchools(
            @ModelAttribute SchoolParamsRequest params) {
        return ok("Fetched schools", masterDataService.getSchools(params, getCurrentTenantId()));
    }

    @PostMapping("/schools")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.write')")
    public ResponseEntity<GeneralResponse<SchoolResponse>> createSchool(@Valid @RequestBody SchoolUpsertRequest request) {
        return created("Created school", masterDataService.createSchool(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/schools/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.read')")
    public ResponseEntity<GeneralResponse<SchoolResponse>> getSchool(@PathVariable Long id) {
        return ok("Fetched school", masterDataService.getSchoolResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/schools/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.write')")
    public ResponseEntity<GeneralResponse<SchoolResponse>> updateSchool(@PathVariable Long id, @Valid @RequestBody SchoolUpsertRequest request) {
        return ok("Updated school", masterDataService.updateSchool(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/schools/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.delete')")
    public ResponseEntity<GeneralResponse<Void>> deleteSchool(@PathVariable Long id) {
        masterDataService.deleteSchool(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted school");
    }

    @GetMapping("/parents")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.parent.read')")
    public ResponseEntity<GeneralResponse<PageResponse<ParentProfileResponse>>> getParents(
            @ModelAttribute ParentProfileParamsRequest params) {
        return ok("Fetched parents", masterDataService.getParents(params, getCurrentTenantId()));
    }

    @PostMapping("/parents")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.parent.write')")
    public ResponseEntity<GeneralResponse<ParentProfileResponse>> createParent(@Valid @RequestBody ParentProfileUpsertRequest request) {
        return created("Created parent", masterDataService.createParent(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/parents/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.parent.read')")
    public ResponseEntity<GeneralResponse<ParentProfileResponse>> getParent(@PathVariable Long id) {
        return ok("Fetched parent", masterDataService.getParentResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/parents/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.parent.write')")
    public ResponseEntity<GeneralResponse<ParentProfileResponse>> updateParent(@PathVariable Long id,
            @Valid @RequestBody ParentProfileUpsertRequest request) {
        return ok("Updated parent", masterDataService.updateParent(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/parents/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.parent.delete')")
    public ResponseEntity<GeneralResponse<Void>> deleteParent(@PathVariable Long id) {
        masterDataService.deleteParent(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted parent");
    }

    @GetMapping("/students")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.student.read')")
    public ResponseEntity<GeneralResponse<PageResponse<StudentResponse>>> getStudents(
            @ModelAttribute StudentParamsRequest params) {
        return ok("Fetched students", masterDataService.getStudents(params, getCurrentTenantId()));
    }

    @PostMapping("/students")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.student.write')")
    public ResponseEntity<GeneralResponse<StudentResponse>> createStudent(@Valid @RequestBody StudentUpsertRequest request) {
        return created("Created student", masterDataService.createStudent(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/students/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.student.read')")
    public ResponseEntity<GeneralResponse<StudentResponse>> getStudent(@PathVariable Long id) {
        return ok("Fetched student", masterDataService.getStudentResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/students/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.student.write')")
    public ResponseEntity<GeneralResponse<StudentResponse>> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUpsertRequest request) {
        return ok("Updated student", masterDataService.updateStudent(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/students/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.student.delete')")
    public ResponseEntity<GeneralResponse<Void>> deleteStudent(@PathVariable Long id) {
        masterDataService.deleteStudent(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted student");
    }

    @GetMapping("/buses")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.bus.read')")
    public ResponseEntity<GeneralResponse<PageResponse<BusResponse>>> getBuses(
            @ModelAttribute BusParamsRequest params) {
        return ok("Fetched buses", masterDataService.getBuses(params, getCurrentTenantId()));
    }

    @PostMapping("/buses")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.bus.write')")
    public ResponseEntity<GeneralResponse<BusResponse>> createBus(@Valid @RequestBody BusUpsertRequest request) {
        return created("Created bus", masterDataService.createBus(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/buses/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.bus.read')")
    public ResponseEntity<GeneralResponse<BusResponse>> getBus(@PathVariable Long id) {
        return ok("Fetched bus", masterDataService.getBusResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/buses/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.bus.write')")
    public ResponseEntity<GeneralResponse<BusResponse>> updateBus(@PathVariable Long id, @Valid @RequestBody BusUpsertRequest request) {
        return ok("Updated bus", masterDataService.updateBus(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/buses/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.bus.delete')")
    public ResponseEntity<GeneralResponse<Void>> deleteBus(@PathVariable Long id) {
        masterDataService.deleteBus(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted bus");
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

    @GetMapping("/drivers")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.driver.read')")
    public ResponseEntity<GeneralResponse<PageResponse<DriverProfileResponse>>> getDrivers(
            @ModelAttribute DriverProfileParamsRequest params) {
        return ok("Fetched drivers", masterDataService.getDrivers(params, getCurrentTenantId()));
    }

    @PostMapping("/drivers")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.driver.write')")
    public ResponseEntity<GeneralResponse<DriverProfileResponse>> createDriver(@Valid @RequestBody DriverProfileUpsertRequest request) {
        return created("Created driver", masterDataService.createDriver(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/drivers/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.driver.read')")
    public ResponseEntity<GeneralResponse<DriverProfileResponse>> getDriver(@PathVariable Long id) {
        return ok("Fetched driver", masterDataService.getDriverResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/drivers/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.driver.write')")
    public ResponseEntity<GeneralResponse<DriverProfileResponse>> updateDriver(@PathVariable Long id, @Valid @RequestBody DriverProfileUpsertRequest request) {
        return ok("Updated driver", masterDataService.updateDriver(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/drivers/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.driver.delete')")
    public ResponseEntity<GeneralResponse<Void>> deleteDriver(@PathVariable Long id) {
        masterDataService.deleteDriver(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted driver");
    }

    @GetMapping("/attendants")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendant.read')")
    public ResponseEntity<GeneralResponse<PageResponse<AttendantProfileResponse>>> getAttendants(
            @ModelAttribute AttendantProfileParamsRequest params) {
        return ok("Fetched attendants", masterDataService.getAttendants(params, getCurrentTenantId()));
    }

    @PostMapping("/attendants")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendant.write')")
    public ResponseEntity<GeneralResponse<AttendantProfileResponse>> createAttendant(@Valid @RequestBody BusAttendantProfileUpsertRequest request) {
        return created("Created attendant", masterDataService.createAttendant(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/attendants/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendant.read')")
    public ResponseEntity<GeneralResponse<AttendantProfileResponse>> getAttendant(@PathVariable Long id) {
        return ok("Fetched attendant", masterDataService.getAttendantResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/attendants/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendant.write')")
    public ResponseEntity<GeneralResponse<AttendantProfileResponse>> updateAttendant(@PathVariable Long id,
            @Valid @RequestBody BusAttendantProfileUpsertRequest request) {
        return ok("Updated attendant", masterDataService.updateAttendant(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/attendants/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendant.delete')")
    public ResponseEntity<GeneralResponse<Void>> deleteAttendant(@PathVariable Long id) {
        masterDataService.deleteAttendant(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted attendant");
    }

    @GetMapping("/pickup-points")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.pickup-point.read')")
    public ResponseEntity<GeneralResponse<PageResponse<PickupPointResponse>>> getPickupPoints(
            @ModelAttribute PickupPointParamsRequest params) {
        return ok("Fetched pickup points", masterDataService.getPickupPoints(params, getCurrentTenantId()));
    }

    @PostMapping("/pickup-points")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.pickup-point.write')")
    public ResponseEntity<GeneralResponse<PickupPointResponse>> createPickupPoint(@Valid @RequestBody PickupPointUpsertRequest request) {
        return created("Created pickup point", masterDataService.createPickupPoint(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/pickup-points/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.pickup-point.read')")
    public ResponseEntity<GeneralResponse<PickupPointResponse>> getPickupPoint(@PathVariable Long id) {
        return ok("Fetched pickup point", masterDataService.getPickupPointResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/pickup-points/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.pickup-point.write')")
    public ResponseEntity<GeneralResponse<PickupPointResponse>> updatePickupPoint(@PathVariable Long id, @Valid @RequestBody PickupPointUpsertRequest request) {
        return ok("Updated pickup point", masterDataService.updatePickupPoint(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/pickup-points/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.pickup-point.delete')")
    public ResponseEntity<GeneralResponse<Void>> deletePickupPoint(@PathVariable Long id) {
        masterDataService.deletePickupPoint(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted pickup point");
    }

    @GetMapping("/depots")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.depot.read')")
    public ResponseEntity<GeneralResponse<PageResponse<DepotResponse>>> getDepots(
            @ModelAttribute DepotParamsRequest params) {
        return ok("Fetched depots", masterDataService.getDepots(params, getCurrentTenantId()));
    }

    @PostMapping("/depots")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.depot.write')")
    public ResponseEntity<GeneralResponse<DepotResponse>> createDepot(@Valid @RequestBody DepotUpsertRequest request) {
        return created("Created depot", masterDataService.createDepot(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/depots/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.depot.read')")
    public ResponseEntity<GeneralResponse<DepotResponse>> getDepot(@PathVariable Long id) {
        return ok("Fetched depot", masterDataService.getDepotResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/depots/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.depot.write')")
    public ResponseEntity<GeneralResponse<DepotResponse>> updateDepot(@PathVariable Long id,
            @Valid @RequestBody DepotUpsertRequest request) {
        return ok("Updated depot", masterDataService.updateDepot(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/depots/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.depot.delete')")
    public ResponseEntity<GeneralResponse<Void>> deleteDepot(@PathVariable Long id) {
        masterDataService.deleteDepot(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted depot");
    }
}
