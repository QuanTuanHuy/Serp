package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import serp.project.school_bus_service.application.dto.response.DepotResponse;
import serp.project.school_bus_service.application.dto.response.DriverProfileResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.ParentProfileResponse;
import serp.project.school_bus_service.application.dto.response.PickupPointResponse;
import serp.project.school_bus_service.application.dto.response.SchoolResponse;
import serp.project.school_bus_service.application.dto.response.StudentResponse;
import serp.project.school_bus_service.core.service.IAttendantService;
import serp.project.school_bus_service.core.service.IBusService;
import serp.project.school_bus_service.core.service.IDepotService;
import serp.project.school_bus_service.core.service.IDriverService;
import serp.project.school_bus_service.core.service.IMasterDataService;
import serp.project.school_bus_service.core.service.IParentService;
import serp.project.school_bus_service.core.service.IPickupPointService;
import serp.project.school_bus_service.core.service.ISchoolService;
import serp.project.school_bus_service.core.service.IStudentService;
import serp.project.school_bus_service.infrastructure.store.model.BusAttendantProfileEntity;
import serp.project.school_bus_service.infrastructure.store.model.BusEntity;
import serp.project.school_bus_service.infrastructure.store.model.DepotEntity;
import serp.project.school_bus_service.infrastructure.store.model.DriverProfileEntity;
import serp.project.school_bus_service.infrastructure.store.model.ParentProfileEntity;
import serp.project.school_bus_service.infrastructure.store.model.PickupPointEntity;
import serp.project.school_bus_service.infrastructure.store.model.SchoolEntity;
import serp.project.school_bus_service.infrastructure.store.model.StudentEntity;

/**
 * Compatibility facade that delegates to dedicated resource services.
 * Exists so that callers injecting IMasterDataService continue to work without modification.
 * No longer extends AbstractBaseService or owns any business logic directly.
 */
@Service
@RequiredArgsConstructor
public class MasterDataServiceImpl implements IMasterDataService {

    private final ISchoolService schoolService;
    private final IParentService parentService;
    private final IStudentService studentService;
    private final IBusService busService;
    private final IDriverService driverService;
    private final IAttendantService attendantService;
    private final IPickupPointService pickupPointService;
    private final IDepotService depotService;

    // --- School ---

    @Override
    public PageResponse<SchoolResponse> getSchools(SchoolParamsRequest params, Long tenantId) {
        return schoolService.getSchools(params, tenantId);
    }

    @Override
    public SchoolResponse getSchoolResponse(Long id, Long tenantId) {
        return schoolService.getSchoolResponse(id, tenantId);
    }

    @Override
    public SchoolEntity getSchool(Long id, Long tenantId) {
        return schoolService.getSchool(id, tenantId);
    }

    @Override
    public SchoolResponse createSchool(SchoolUpsertRequest request, Long tenantId, Long actorId) {
        return schoolService.createSchool(request, tenantId, actorId);
    }

    @Override
    public SchoolResponse updateSchool(Long id, SchoolUpsertRequest request, Long tenantId, Long actorId) {
        return schoolService.updateSchool(id, request, tenantId, actorId);
    }

    @Override
    public void deleteSchool(Long id, Long tenantId, Long actorId) {
        schoolService.deleteSchool(id, tenantId, actorId);
    }

    // --- Parent ---

    @Override
    public PageResponse<ParentProfileResponse> getParents(ParentProfileParamsRequest params, Long tenantId) {
        return parentService.getParents(params, tenantId);
    }

    @Override
    public ParentProfileResponse getParentResponse(Long id, Long tenantId) {
        return parentService.getParentResponse(id, tenantId);
    }

    @Override
    public ParentProfileEntity getParent(Long id, Long tenantId) {
        return parentService.getParent(id, tenantId);
    }

    @Override
    public ParentProfileResponse createParent(ParentProfileUpsertRequest request, Long tenantId, Long actorId) {
        return parentService.createParent(request, tenantId, actorId);
    }

    @Override
    public ParentProfileResponse updateParent(Long id, ParentProfileUpsertRequest request, Long tenantId, Long actorId) {
        return parentService.updateParent(id, request, tenantId, actorId);
    }

    @Override
    public void deleteParent(Long id, Long tenantId, Long actorId) {
        parentService.deleteParent(id, tenantId, actorId);
    }

    // --- Student ---

    @Override
    public PageResponse<StudentResponse> getStudents(StudentParamsRequest params, Long tenantId) {
        return studentService.getStudents(params, tenantId);
    }

    @Override
    public StudentResponse getStudentResponse(Long id, Long tenantId) {
        return studentService.getStudentResponse(id, tenantId);
    }

    @Override
    public StudentEntity getStudent(Long id, Long tenantId) {
        return studentService.getStudent(id, tenantId);
    }

    @Override
    public StudentResponse createStudent(StudentUpsertRequest request, Long tenantId, Long actorId) {
        return studentService.createStudent(request, tenantId, actorId);
    }

    @Override
    public StudentResponse updateStudent(Long id, StudentUpsertRequest request, Long tenantId, Long actorId) {
        return studentService.updateStudent(id, request, tenantId, actorId);
    }

    @Override
    public void deleteStudent(Long id, Long tenantId, Long actorId) {
        studentService.deleteStudent(id, tenantId, actorId);
    }

    // --- Bus ---

    @Override
    public PageResponse<BusResponse> getBuses(BusParamsRequest params, Long tenantId) {
        return busService.getBuses(params, tenantId);
    }

    @Override
    public BusResponse getBusResponse(Long id, Long tenantId) {
        return busService.getBusResponse(id, tenantId);
    }

    @Override
    public BusEntity getBus(Long id, Long tenantId) {
        return busService.getBus(id, tenantId);
    }

    @Override
    public BusResponse createBus(BusUpsertRequest request, Long tenantId, Long actorId) {
        return busService.createBus(request, tenantId, actorId);
    }

    @Override
    public BusResponse updateBus(Long id, BusUpsertRequest request, Long tenantId, Long actorId) {
        return busService.updateBus(id, request, tenantId, actorId);
    }

    @Override
    public void deleteBus(Long id, Long tenantId, Long actorId) {
        busService.deleteBus(id, tenantId, actorId);
    }

    // --- Driver ---

    @Override
    public PageResponse<DriverProfileResponse> getDrivers(DriverProfileParamsRequest params, Long tenantId) {
        return driverService.getDrivers(params, tenantId);
    }

    @Override
    public DriverProfileResponse getDriverResponse(Long id, Long tenantId) {
        return driverService.getDriverResponse(id, tenantId);
    }

    @Override
    public DriverProfileEntity getDriver(Long id, Long tenantId) {
        return driverService.getDriver(id, tenantId);
    }

    @Override
    public DriverProfileResponse createDriver(DriverProfileUpsertRequest request, Long tenantId, Long actorId) {
        return driverService.createDriver(request, tenantId, actorId);
    }

    @Override
    public DriverProfileResponse updateDriver(Long id, DriverProfileUpsertRequest request, Long tenantId, Long actorId) {
        return driverService.updateDriver(id, request, tenantId, actorId);
    }

    @Override
    public void deleteDriver(Long id, Long tenantId, Long actorId) {
        driverService.deleteDriver(id, tenantId, actorId);
    }

    // --- Attendant ---

    @Override
    public PageResponse<AttendantProfileResponse> getAttendants(AttendantProfileParamsRequest params, Long tenantId) {
        return attendantService.getAttendants(params, tenantId);
    }

    @Override
    public AttendantProfileResponse getAttendantResponse(Long id, Long tenantId) {
        return attendantService.getAttendantResponse(id, tenantId);
    }

    @Override
    public BusAttendantProfileEntity getAttendant(Long id, Long tenantId) {
        return attendantService.getAttendant(id, tenantId);
    }

    @Override
    public AttendantProfileResponse createAttendant(BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId) {
        return attendantService.createAttendant(request, tenantId, actorId);
    }

    @Override
    public AttendantProfileResponse updateAttendant(Long id, BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId) {
        return attendantService.updateAttendant(id, request, tenantId, actorId);
    }

    @Override
    public void deleteAttendant(Long id, Long tenantId, Long actorId) {
        attendantService.deleteAttendant(id, tenantId, actorId);
    }

    // --- PickupPoint ---

    @Override
    public PageResponse<PickupPointResponse> getPickupPoints(PickupPointParamsRequest params, Long tenantId) {
        return pickupPointService.getPickupPoints(params, tenantId);
    }

    @Override
    public PickupPointResponse getPickupPointResponse(Long id, Long tenantId) {
        return pickupPointService.getPickupPointResponse(id, tenantId);
    }

    @Override
    public PickupPointEntity getPickupPoint(Long id, Long tenantId) {
        return pickupPointService.getPickupPoint(id, tenantId);
    }

    @Override
    public PickupPointResponse createPickupPoint(PickupPointUpsertRequest request, Long tenantId, Long actorId) {
        return pickupPointService.createPickupPoint(request, tenantId, actorId);
    }

    @Override
    public PickupPointResponse updatePickupPoint(Long id, PickupPointUpsertRequest request, Long tenantId, Long actorId) {
        return pickupPointService.updatePickupPoint(id, request, tenantId, actorId);
    }

    @Override
    public void deletePickupPoint(Long id, Long tenantId, Long actorId) {
        pickupPointService.deletePickupPoint(id, tenantId, actorId);
    }

    // --- Depot ---

    @Override
    public PageResponse<DepotResponse> getDepots(DepotParamsRequest params, Long tenantId) {
        return depotService.getDepots(params, tenantId);
    }

    @Override
    public DepotResponse getDepotResponse(Long id, Long tenantId) {
        return depotService.getDepotResponse(id, tenantId);
    }

    @Override
    public DepotEntity getDepot(Long id, Long tenantId) {
        return depotService.getDepot(id, tenantId);
    }

    @Override
    public DepotResponse createDepot(DepotUpsertRequest request, Long tenantId, Long actorId) {
        return depotService.createDepot(request, tenantId, actorId);
    }

    @Override
    public DepotResponse updateDepot(Long id, DepotUpsertRequest request, Long tenantId, Long actorId) {
        return depotService.updateDepot(id, request, tenantId, actorId);
    }

    @Override
    public void deleteDepot(Long id, Long tenantId, Long actorId) {
        depotService.deleteDepot(id, tenantId, actorId);
    }
}
