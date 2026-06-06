package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.params.AttendantProfileParamsRequest;
import serp.project.school_bus_service.dto.params.BusParamsRequest;
import serp.project.school_bus_service.dto.params.DepotParamsRequest;
import serp.project.school_bus_service.dto.params.DriverProfileParamsRequest;
import serp.project.school_bus_service.dto.params.ParentProfileParamsRequest;
import serp.project.school_bus_service.dto.params.PickupPointParamsRequest;
import serp.project.school_bus_service.dto.params.SchoolParamsRequest;
import serp.project.school_bus_service.dto.params.StudentParamsRequest;
import serp.project.school_bus_service.dto.request.BusAttendantProfileUpsertRequest;
import serp.project.school_bus_service.dto.request.BusUpsertRequest;
import serp.project.school_bus_service.dto.request.DepotUpsertRequest;
import serp.project.school_bus_service.dto.request.DriverProfileUpsertRequest;
import serp.project.school_bus_service.dto.request.ParentProfileUpsertRequest;
import serp.project.school_bus_service.dto.request.PickupPointUpsertRequest;
import serp.project.school_bus_service.dto.request.SchoolUpsertRequest;
import serp.project.school_bus_service.dto.request.StudentUpsertRequest;
import serp.project.school_bus_service.dto.response.AttendantProfileResponse;
import serp.project.school_bus_service.dto.response.BusResponse;
import serp.project.school_bus_service.dto.response.DepotResponse;
import serp.project.school_bus_service.dto.response.DriverProfileResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.ParentProfileResponse;
import serp.project.school_bus_service.dto.response.PickupPointResponse;
import serp.project.school_bus_service.dto.response.SchoolResponse;
import serp.project.school_bus_service.dto.response.StudentResponse;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;
import serp.project.school_bus_service.entity.BusEntity;
import serp.project.school_bus_service.entity.DepotEntity;
import serp.project.school_bus_service.entity.DriverProfileEntity;
import serp.project.school_bus_service.entity.ParentProfileEntity;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.StudentEntity;

/**
 * Compatibility facade interface for master data operations.
 * Delegates to dedicated resource services (ISchoolService, IStudentService, etc.).
 * Retained so that existing callers (RouteServiceImpl, TransportRequestServiceImpl, etc.)
 * do not need to change their injection in this phase.
 */
public interface IMasterDataService {

    PageResponse<SchoolResponse> getSchools(SchoolParamsRequest params, Long tenantId);

    SchoolResponse getSchoolResponse(Long id, Long tenantId);

    SchoolEntity getSchool(Long id, Long tenantId);

    SchoolResponse createSchool(SchoolUpsertRequest request, Long tenantId, Long actorId);

    SchoolResponse updateSchool(Long id, SchoolUpsertRequest request, Long tenantId, Long actorId);

    void deleteSchool(Long id, Long tenantId, Long actorId);

    PageResponse<ParentProfileResponse> getParents(ParentProfileParamsRequest params, Long tenantId);

    ParentProfileResponse getParentResponse(Long id, Long tenantId);

    ParentProfileEntity getParent(Long id, Long tenantId);

    ParentProfileResponse createParent(ParentProfileUpsertRequest request, Long tenantId, Long actorId);

    ParentProfileResponse updateParent(Long id, ParentProfileUpsertRequest request, Long tenantId, Long actorId);

    void deleteParent(Long id, Long tenantId, Long actorId);

    PageResponse<StudentResponse> getStudents(StudentParamsRequest params, Long tenantId);

    StudentResponse getStudentResponse(Long id, Long tenantId);

    StudentEntity getStudent(Long id, Long tenantId);

    StudentResponse createStudent(StudentUpsertRequest request, Long tenantId, Long actorId);

    StudentResponse updateStudent(Long id, StudentUpsertRequest request, Long tenantId, Long actorId);

    void deleteStudent(Long id, Long tenantId, Long actorId);

    PageResponse<BusResponse> getBuses(BusParamsRequest params, Long tenantId);

    BusResponse getBusResponse(Long id, Long tenantId);

    BusEntity getBus(Long id, Long tenantId);

    BusResponse createBus(BusUpsertRequest request, Long tenantId, Long actorId);

    BusResponse updateBus(Long id, BusUpsertRequest request, Long tenantId, Long actorId);

    void deleteBus(Long id, Long tenantId, Long actorId);

    PageResponse<DriverProfileResponse> getDrivers(DriverProfileParamsRequest params, Long tenantId);

    DriverProfileResponse getDriverResponse(Long id, Long tenantId);

    DriverProfileEntity getDriver(Long id, Long tenantId);

    DriverProfileResponse createDriver(DriverProfileUpsertRequest request, Long tenantId, Long actorId);

    DriverProfileResponse updateDriver(Long id, DriverProfileUpsertRequest request, Long tenantId, Long actorId);

    void deleteDriver(Long id, Long tenantId, Long actorId);

    PageResponse<AttendantProfileResponse> getAttendants(AttendantProfileParamsRequest params, Long tenantId);

    AttendantProfileResponse getAttendantResponse(Long id, Long tenantId);

    BusAttendantProfileEntity getAttendant(Long id, Long tenantId);

    AttendantProfileResponse createAttendant(BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId);

    AttendantProfileResponse updateAttendant(Long id, BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId);

    void deleteAttendant(Long id, Long tenantId, Long actorId);

    PageResponse<PickupPointResponse> getPickupPoints(PickupPointParamsRequest params, Long tenantId);

    PickupPointResponse getPickupPointResponse(Long id, Long tenantId);

    PickupPointEntity getPickupPoint(Long id, Long tenantId);

    PickupPointResponse createPickupPoint(PickupPointUpsertRequest request, Long tenantId, Long actorId);

    PickupPointResponse updatePickupPoint(Long id, PickupPointUpsertRequest request, Long tenantId, Long actorId);

    void deletePickupPoint(Long id, Long tenantId, Long actorId);

    PageResponse<DepotResponse> getDepots(DepotParamsRequest params, Long tenantId);

    DepotResponse getDepotResponse(Long id, Long tenantId);

    DepotEntity getDepot(Long id, Long tenantId);

    DepotResponse createDepot(DepotUpsertRequest request, Long tenantId, Long actorId);

    DepotResponse updateDepot(Long id, DepotUpsertRequest request, Long tenantId, Long actorId);

    void deleteDepot(Long id, Long tenantId, Long actorId);
}
