package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.ICodeGeneratorService;
import serp.project.school_bus_service.core.service.IMasterDataService;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.BusAttendantProfileEntity;
import serp.project.school_bus_service.infrastructure.store.model.BusEntity;
import serp.project.school_bus_service.infrastructure.store.model.DepotEntity;
import serp.project.school_bus_service.infrastructure.store.model.DriverProfileEntity;
import serp.project.school_bus_service.infrastructure.store.model.ParentProfileEntity;
import serp.project.school_bus_service.infrastructure.store.model.PickupPointEntity;
import serp.project.school_bus_service.infrastructure.store.model.SchoolEntity;
import serp.project.school_bus_service.infrastructure.store.model.StudentEntity;
import serp.project.school_bus_service.infrastructure.store.repository.BusAttendantProfileRepository;
import serp.project.school_bus_service.infrastructure.store.repository.BusRepository;
import serp.project.school_bus_service.infrastructure.store.repository.DepotRepository;
import serp.project.school_bus_service.infrastructure.store.repository.DriverProfileRepository;
import serp.project.school_bus_service.infrastructure.store.repository.ParentProfileRepository;
import serp.project.school_bus_service.infrastructure.store.repository.PickupPointRepository;
import serp.project.school_bus_service.infrastructure.store.repository.SchoolRepository;
import serp.project.school_bus_service.infrastructure.store.repository.StudentRepository;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.code.SchoolBusCode;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class MasterDataServiceImpl extends AbstractBaseService<SchoolEntity, Long> implements IMasterDataService {

    private final SchoolRepository schoolRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final StudentRepository studentRepository;
    private final BusRepository busRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final BusAttendantProfileRepository busAttendantProfileRepository;
    private final PickupPointRepository pickupPointRepository;
    private final DepotRepository depotRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;
    private final ICodeGeneratorService codeGeneratorService;

    @Override
    protected BaseRepository<SchoolEntity, Long> getRepository() {
        return schoolRepository;
    }

    @Override
    public PageResponse<SchoolResponse> getSchools(SchoolParamsRequest params, Long tenantId) {
        return PageResponse.from(schoolRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "name", "code", "address", "contactPhone",
                        "contactEmail"),
                pageable(params, Set.of("id", "name", "code", "createdAt", "updatedAt"), "name")),
                mapper::toSchoolResponse);
    }

    @Override
    public SchoolResponse getSchoolResponse(Long id, Long tenantId) {
        return mapper.toSchoolResponse(getSchool(id, tenantId));
    }

    @Override
    public SchoolEntity getSchool(Long id, Long tenantId) {
        return findById(schoolRepository, id, tenantId);
    }

    @Override
    @Transactional
    public SchoolResponse createSchool(SchoolUpsertRequest request, Long tenantId, Long actorId) {
        SchoolEntity school = new SchoolEntity();
        school.markCreated(tenantId, actor(actorId));
        applySchool(school, request);
        school.setCode(generateCode(SchoolBusCode.SCHOOL, tenantId, actorId));
        SchoolEntity saved = schoolRepository.save(school);
        auditLogService.log(tenantId, actorId, "School", saved.getId(), "CREATE", "Created school master data");
        return mapper.toSchoolResponse(saved);
    }

    @Override
    @Transactional
    public SchoolResponse updateSchool(Long id, SchoolUpsertRequest request, Long tenantId, Long actorId) {
        SchoolEntity school = getSchool(id, tenantId);
        school.markUpdated(actor(actorId));
        applySchool(school, request);
        SchoolEntity saved = schoolRepository.save(school);
        auditLogService.log(tenantId, actorId, "School", saved.getId(), "UPDATE", "Updated school master data");
        return mapper.toSchoolResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSchool(Long id, Long tenantId, Long actorId) {
        softDeleteById(schoolRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "School", id, "SOFT_DELETE", "Soft deleted school");
    }

    @Override
    public PageResponse<ParentProfileResponse> getParents(ParentProfileParamsRequest params, Long tenantId) {
        return PageResponse.from(parentProfileRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "fullName", "phone", "email", "address"),
                pageable(params, Set.of("id", "fullName", "email", "createdAt", "updatedAt"), "fullName")),
                mapper::toParentProfileResponse);
    }

    @Override
    public ParentProfileResponse getParentResponse(Long id, Long tenantId) {
        return mapper.toParentProfileResponse(getParent(id, tenantId));
    }

    @Override
    public ParentProfileEntity getParent(Long id, Long tenantId) {
        return findById(parentProfileRepository, id, tenantId);
    }

    @Override
    @Transactional
    public ParentProfileResponse createParent(ParentProfileUpsertRequest request, Long tenantId, Long actorId) {
        ParentProfileEntity parent = new ParentProfileEntity();
        parent.markCreated(tenantId, actor(actorId));
        applyParent(parent, request);
        ParentProfileEntity saved = parentProfileRepository.save(parent);
        auditLogService.log(tenantId, actorId, "ParentProfile", saved.getId(), "CREATE", "Created parent profile");
        return mapper.toParentProfileResponse(saved);
    }

    @Override
    @Transactional
    public ParentProfileResponse updateParent(Long id, ParentProfileUpsertRequest request, Long tenantId, Long actorId) {
        ParentProfileEntity parent = getParent(id, tenantId);
        parent.markUpdated(actor(actorId));
        applyParent(parent, request);
        ParentProfileEntity saved = parentProfileRepository.save(parent);
        auditLogService.log(tenantId, actorId, "ParentProfile", saved.getId(), "UPDATE", "Updated parent profile");
        return mapper.toParentProfileResponse(saved);
    }

    @Override
    @Transactional
    public void deleteParent(Long id, Long tenantId, Long actorId) {
        softDeleteById(parentProfileRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "ParentProfile", id, "SOFT_DELETE", "Soft deleted parent profile");
    }

    @Override
    public PageResponse<StudentResponse> getStudents(StudentParamsRequest params, Long tenantId) {
        return PageResponse.from(studentRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "fullName", "studentCode", "grade",
                        "homeAddress", "school.name", "parentProfile.fullName", "pickupPoint.name"),
                pageable(params, Set.of("id", "fullName", "studentCode", "grade", "createdAt", "updatedAt"), "fullName")),
                mapper::toStudentResponse);
    }

    @Override
    public StudentResponse getStudentResponse(Long id, Long tenantId) {
        return mapper.toStudentResponse(getStudent(id, tenantId));
    }

    @Override
    public StudentEntity getStudent(Long id, Long tenantId) {
        return findById(studentRepository, id, tenantId);
    }

    @Override
    @Transactional
    public StudentResponse createStudent(StudentUpsertRequest request, Long tenantId, Long actorId) {
        StudentEntity student = new StudentEntity();
        student.markCreated(tenantId, actor(actorId));
        applyStudent(student, request, tenantId);
        student.setStudentCode(generateCode(SchoolBusCode.STUDENT, tenantId, actorId));
        StudentEntity saved = studentRepository.save(student);
        auditLogService.log(tenantId, actorId, "Student", saved.getId(), "CREATE", "Created student profile");
        return mapper.toStudentResponse(saved);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentUpsertRequest request, Long tenantId, Long actorId) {
        StudentEntity student = getStudent(id, tenantId);
        student.markUpdated(actor(actorId));
        applyStudent(student, request, tenantId);
        StudentEntity saved = studentRepository.save(student);
        auditLogService.log(tenantId, actorId, "Student", saved.getId(), "UPDATE", "Updated student profile");
        return mapper.toStudentResponse(saved);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id, Long tenantId, Long actorId) {
        softDeleteById(studentRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "Student", id, "SOFT_DELETE", "Soft deleted student profile");
    }

    @Override
    public PageResponse<BusResponse> getBuses(BusParamsRequest params, Long tenantId) {
        return PageResponse.from(busRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "plateNumber", "busType", "status"),
                pageable(params, Set.of("id", "plateNumber", "busType", "capacity", "status", "createdAt", "updatedAt"),
                        "plateNumber")),
                mapper::toBusResponse);
    }

    @Override
    public BusResponse getBusResponse(Long id, Long tenantId) {
        return mapper.toBusResponse(getBus(id, tenantId));
    }

    @Override
    public BusEntity getBus(Long id, Long tenantId) {
        return findById(busRepository, id, tenantId);
    }

    @Override
    @Transactional
    public BusResponse createBus(BusUpsertRequest request, Long tenantId, Long actorId) {
        BusEntity bus = new BusEntity();
        bus.markCreated(tenantId, actor(actorId));
        applyBus(bus, request);
        BusEntity saved = busRepository.save(bus);
        auditLogService.log(tenantId, actorId, "Bus", saved.getId(), "CREATE", "Created bus profile");
        return mapper.toBusResponse(saved);
    }

    @Override
    @Transactional
    public BusResponse updateBus(Long id, BusUpsertRequest request, Long tenantId, Long actorId) {
        BusEntity bus = getBus(id, tenantId);
        bus.markUpdated(actor(actorId));
        applyBus(bus, request);
        BusEntity saved = busRepository.save(bus);
        auditLogService.log(tenantId, actorId, "Bus", saved.getId(), "UPDATE", "Updated bus profile");
        return mapper.toBusResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBus(Long id, Long tenantId, Long actorId) {
        softDeleteById(busRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "Bus", id, "SOFT_DELETE", "Soft deleted bus profile");
    }

    @Override
    public PageResponse<DriverProfileResponse> getDrivers(DriverProfileParamsRequest params, Long tenantId) {
        return PageResponse.from(driverProfileRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "fullName", "phone", "licenseNumber",
                        "status"),
                pageable(params, Set.of("id", "fullName", "licenseNumber", "status", "createdAt", "updatedAt"),
                        "fullName")),
                mapper::toDriverProfileResponse);
    }

    @Override
    public DriverProfileResponse getDriverResponse(Long id, Long tenantId) {
        return mapper.toDriverProfileResponse(getDriver(id, tenantId));
    }

    @Override
    public DriverProfileEntity getDriver(Long id, Long tenantId) {
        return findById(driverProfileRepository, id, tenantId);
    }

    @Override
    @Transactional
    public DriverProfileResponse createDriver(DriverProfileUpsertRequest request, Long tenantId, Long actorId) {
        DriverProfileEntity driver = new DriverProfileEntity();
        driver.markCreated(tenantId, actor(actorId));
        applyDriver(driver, request);
        DriverProfileEntity saved = driverProfileRepository.save(driver);
        auditLogService.log(tenantId, actorId, "DriverProfile", saved.getId(), "CREATE", "Created driver profile");
        return mapper.toDriverProfileResponse(saved);
    }

    @Override
    @Transactional
    public DriverProfileResponse updateDriver(Long id, DriverProfileUpsertRequest request, Long tenantId, Long actorId) {
        DriverProfileEntity driver = getDriver(id, tenantId);
        driver.markUpdated(actor(actorId));
        applyDriver(driver, request);
        DriverProfileEntity saved = driverProfileRepository.save(driver);
        auditLogService.log(tenantId, actorId, "DriverProfile", saved.getId(), "UPDATE", "Updated driver profile");
        return mapper.toDriverProfileResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDriver(Long id, Long tenantId, Long actorId) {
        softDeleteById(driverProfileRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "DriverProfile", id, "SOFT_DELETE", "Soft deleted driver profile");
    }

    @Override
    public PageResponse<AttendantProfileResponse> getAttendants(AttendantProfileParamsRequest params, Long tenantId) {
        return PageResponse.from(busAttendantProfileRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "fullName", "phone", "status"),
                pageable(params, Set.of("id", "fullName", "status", "createdAt", "updatedAt"), "fullName")),
                mapper::toAttendantProfileResponse);
    }

    @Override
    public AttendantProfileResponse getAttendantResponse(Long id, Long tenantId) {
        return mapper.toAttendantProfileResponse(getAttendant(id, tenantId));
    }

    @Override
    public BusAttendantProfileEntity getAttendant(Long id, Long tenantId) {
        return findById(busAttendantProfileRepository, id, tenantId);
    }

    @Override
    @Transactional
    public AttendantProfileResponse createAttendant(BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId) {
        BusAttendantProfileEntity attendant = new BusAttendantProfileEntity();
        attendant.markCreated(tenantId, actor(actorId));
        applyAttendant(attendant, request);
        BusAttendantProfileEntity saved = busAttendantProfileRepository.save(attendant);
        auditLogService.log(tenantId, actorId, "AttendantProfile", saved.getId(), "CREATE", "Created attendant profile");
        return mapper.toAttendantProfileResponse(saved);
    }

    @Override
    @Transactional
    public AttendantProfileResponse updateAttendant(Long id, BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId) {
        BusAttendantProfileEntity attendant = getAttendant(id, tenantId);
        attendant.markUpdated(actor(actorId));
        applyAttendant(attendant, request);
        BusAttendantProfileEntity saved = busAttendantProfileRepository.save(attendant);
        auditLogService.log(tenantId, actorId, "AttendantProfile", saved.getId(), "UPDATE", "Updated attendant profile");
        return mapper.toAttendantProfileResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAttendant(Long id, Long tenantId, Long actorId) {
        softDeleteById(busAttendantProfileRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "AttendantProfile", id, "SOFT_DELETE", "Soft deleted attendant profile");
    }

    @Override
    public PageResponse<PickupPointResponse> getPickupPoints(PickupPointParamsRequest params, Long tenantId) {
        return PageResponse.from(pickupPointRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "name", "address", "school.name"),
                pageable(params, Set.of("id", "name", "createdAt", "updatedAt"), "name")),
                mapper::toPickupPointResponse);
    }

    @Override
    public PickupPointResponse getPickupPointResponse(Long id, Long tenantId) {
        return mapper.toPickupPointResponse(getPickupPoint(id, tenantId));
    }

    @Override
    public PickupPointEntity getPickupPoint(Long id, Long tenantId) {
        return findById(pickupPointRepository, id, tenantId);
    }

    @Override
    @Transactional
    public PickupPointResponse createPickupPoint(PickupPointUpsertRequest request, Long tenantId, Long actorId) {
        PickupPointEntity pickupPoint = new PickupPointEntity();
        pickupPoint.markCreated(tenantId, actor(actorId));
        applyPickupPoint(pickupPoint, request, tenantId);
        PickupPointEntity saved = pickupPointRepository.save(pickupPoint);
        auditLogService.log(tenantId, actorId, "PickupPoint", saved.getId(), "CREATE", "Created pickup point");
        return mapper.toPickupPointResponse(saved);
    }

    @Override
    @Transactional
    public PickupPointResponse updatePickupPoint(Long id, PickupPointUpsertRequest request, Long tenantId, Long actorId) {
        PickupPointEntity pickupPoint = getPickupPoint(id, tenantId);
        pickupPoint.markUpdated(actor(actorId));
        applyPickupPoint(pickupPoint, request, tenantId);
        PickupPointEntity saved = pickupPointRepository.save(pickupPoint);
        auditLogService.log(tenantId, actorId, "PickupPoint", saved.getId(), "UPDATE", "Updated pickup point");
        return mapper.toPickupPointResponse(saved);
    }

    @Override
    @Transactional
    public void deletePickupPoint(Long id, Long tenantId, Long actorId) {
        softDeleteById(pickupPointRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "PickupPoint", id, "SOFT_DELETE", "Soft deleted pickup point");
    }

    @Override
    public PageResponse<DepotResponse> getDepots(DepotParamsRequest params, Long tenantId) {
        return PageResponse.from(depotRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "name", "address", "contactPhone",
                        "description"),
                pageable(params, Set.of("id", "name", "createdAt", "updatedAt"), "name")),
                mapper::toDepotResponse);
    }

    @Override
    public DepotResponse getDepotResponse(Long id, Long tenantId) {
        return mapper.toDepotResponse(getDepot(id, tenantId));
    }

    @Override
    public DepotEntity getDepot(Long id, Long tenantId) {
        return findById(depotRepository, id, tenantId);
    }

    @Override
    @Transactional
    public DepotResponse createDepot(DepotUpsertRequest request, Long tenantId, Long actorId) {
        DepotEntity depot = new DepotEntity();
        depot.markCreated(tenantId, actor(actorId));
        applyDepot(depot, request);
        DepotEntity saved = depotRepository.save(depot);
        auditLogService.log(tenantId, actorId, "Depot", saved.getId(), "CREATE", "Created depot");
        return mapper.toDepotResponse(saved);
    }

    @Override
    @Transactional
    public DepotResponse updateDepot(Long id, DepotUpsertRequest request, Long tenantId, Long actorId) {
        DepotEntity depot = getDepot(id, tenantId);
        depot.markUpdated(actor(actorId));
        applyDepot(depot, request);
        DepotEntity saved = depotRepository.save(depot);
        auditLogService.log(tenantId, actorId, "Depot", saved.getId(), "UPDATE", "Updated depot");
        return mapper.toDepotResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDepot(Long id, Long tenantId, Long actorId) {
        softDeleteById(depotRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "Depot", id, "SOFT_DELETE", "Soft deleted depot");
    }

    private void applySchool(SchoolEntity school, SchoolUpsertRequest request) {
        validateCoordinatePair(request.getLatitude(), request.getLongitude(), "school");
        school.setName(request.getName());
        school.setAddress(request.getAddress());
        school.setContactPhone(request.getContactPhone());
        school.setContactEmail(request.getContactEmail());
        school.setLatitude(request.getLatitude());
        school.setLongitude(request.getLongitude());
        school.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void applyParent(ParentProfileEntity parent, ParentProfileUpsertRequest request) {
        parent.setUserId(request.getUserId());
        parent.setFullName(request.getFullName());
        parent.setPhone(request.getPhone());
        parent.setEmail(request.getEmail());
        parent.setAddress(request.getAddress());
        parent.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void applyStudent(StudentEntity student, StudentUpsertRequest request, Long tenantId) {
        student.setSchool(getSchool(request.getSchoolId(), tenantId));
        student.setParentProfile(getParent(request.getParentProfileId(), tenantId));
        student.setPickupPoint(request.getPickupPointId() == null ? null : getPickupPoint(request.getPickupPointId(), tenantId));
        student.setFullName(request.getFullName());
        student.setGrade(request.getGrade());
        student.setHomeAddress(request.getHomeAddress());
        student.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void applyBus(BusEntity bus, BusUpsertRequest request) {
        bus.setPlateNumber(request.getPlateNumber());
        bus.setBusType(request.getBusType());
        bus.setCapacity(request.getCapacity());
        bus.setStatus(request.getStatus());
        bus.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void applyDriver(DriverProfileEntity driver, DriverProfileUpsertRequest request) {
        driver.setUserId(request.getUserId());
        driver.setFullName(request.getFullName());
        driver.setPhone(request.getPhone());
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setStatus(request.getStatus());
        driver.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void applyAttendant(BusAttendantProfileEntity attendant, BusAttendantProfileUpsertRequest request) {
        attendant.setUserId(request.getUserId());
        attendant.setFullName(request.getFullName());
        attendant.setPhone(request.getPhone());
        attendant.setStatus(request.getStatus());
        attendant.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void applyPickupPoint(PickupPointEntity pickupPoint, PickupPointUpsertRequest request, Long tenantId) {
        validateCoordinatePair(request.getLatitude(), request.getLongitude(), "pickup point");
        pickupPoint.setSchool(getSchool(request.getSchoolId(), tenantId));
        pickupPoint.setName(request.getName());
        pickupPoint.setAddress(request.getAddress());
        pickupPoint.setLatitude(request.getLatitude());
        pickupPoint.setLongitude(request.getLongitude());
        pickupPoint.setPickupWindowStart(request.getPickupWindowStart());
        pickupPoint.setPickupWindowEnd(request.getPickupWindowEnd());
        pickupPoint.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void applyDepot(DepotEntity depot, DepotUpsertRequest request) {
        validateCoordinatePair(request.getLatitude(), request.getLongitude(), "depot");
        depot.setName(request.getName());
        depot.setAddress(request.getAddress());
        depot.setLatitude(request.getLatitude());
        depot.setLongitude(request.getLongitude());
        depot.setContactPhone(request.getContactPhone());
        depot.setDescription(request.getDescription());
        depot.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void validateCoordinatePair(Double latitude, Double longitude, String target) {
        if ((latitude == null) != (longitude == null)) {
            throw new AppException(
                    AppErrorCode.INVALID_REQUEST,
                    String.format("Both latitude and longitude are required when pinning a %s", target));
        }

        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new AppException(
                    AppErrorCode.INVALID_REQUEST,
                    String.format("Latitude for %s must be between -90 and 90", target));
        }

        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new AppException(
                    AppErrorCode.INVALID_REQUEST,
                    String.format("Longitude for %s must be between -180 and 180", target));
        }
    }

    private <T extends serp.project.school_bus_service.infrastructure.store.model.BaseModel> Specification<T> spec(
            Long tenantId, String keyword, String... fields) {
        return BaseSpecification.tenantActiveWithKeyword(tenantId, keyword, fields);
    }

    private Pageable pageable(
            serp.project.school_bus_service.application.dto.request.BaseParamsRequest params,
            Set<String> allowedSorts,
            String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
    }

    private String generateCode(SchoolBusCode code, Long tenantId, Long actorId) {
        return codeGeneratorService.generate(code.sequenceKey(), code.prefix(), tenantId, actorId);
    }
}
