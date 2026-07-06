package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.StudentParamsRequest;
import serp.project.school_bus_service.dto.request.StudentUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.StudentResponse;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IPickupPointService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.service.IParentService;
import serp.project.school_bus_service.service.IStudentService;
import serp.project.school_bus_service.service.ISchoolBusDataScopeService;
import serp.project.school_bus_service.shared.auth.SchoolBusSecurityService;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.StudentEntity;
import serp.project.school_bus_service.repository.StudentRepository;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;

import java.time.LocalDate;
import java.util.Set;

@Service
public class StudentServiceImpl extends AbstractBaseService<StudentEntity, Long> implements IStudentService {

    private final StudentRepository studentRepository;
    private final ISchoolService schoolService;
    private final IParentService parentService;
    private final IPickupPointService pickupPointService;
    private final SchoolBusMapper mapper;
    private final ICodeGeneratorService codeGeneratorService;
    private final MessageCommon messageCommon;
    private final ISchoolBusDataScopeService schoolBusDataScopeService;
    private final SchoolBusSecurityService securityService;


    public StudentServiceImpl(
            StudentRepository studentRepository,
            ISchoolService schoolService,
            IParentService parentService,
            IPickupPointService pickupPointService,
            SchoolBusMapper mapper,
            ICodeGeneratorService codeGeneratorService,
            MessageCommon messageCommon,
            ISchoolBusDataScopeService schoolBusDataScopeService,
            SchoolBusSecurityService securityService) {
        this.studentRepository = studentRepository;
        this.schoolService = schoolService;
        this.parentService = parentService;
        this.pickupPointService = pickupPointService;
        this.mapper = mapper;
        this.codeGeneratorService = codeGeneratorService;
        this.messageCommon = messageCommon;
        this.schoolBusDataScopeService = schoolBusDataScopeService;
        this.securityService = securityService;
    }


    @Override
    protected BaseRepository<StudentEntity, Long> getRepository() {
        return studentRepository;
    }

    @Override
    public PageResponse<StudentResponse> getStudents(StudentParamsRequest params, Long tenantId) {
        Long parentProfileId = null;
        if (securityService.isParentOnly()) {
            parentProfileId = schoolBusDataScopeService.getCurrentParentProfileIdRequired();
        } else if (params != null && params.getParentProfileId() != null) {
            parentProfileId = params.getParentProfileId();
        }
        return PageResponse.from(studentRepository.findStudentListItems(
                tenantId,
                parentProfileId,
                params == null ? null : params.getSchoolId(),
                keywordPattern(params == null ? null : params.getKeyword()),
                PageableUtils.from(params,
                        Set.of("id", "fullName", "studentCode", "grade", "className", "createdAt", "updatedAt"), "fullName")),
                response -> response);
    }

    private String keywordPattern(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return "%" + keyword.trim().toLowerCase() + "%";
    }

    @Override
    public StudentResponse getStudentResponse(Long id, Long tenantId) {
        return mapper.toStudentResponse(getStudent(id, tenantId));
    }

    @Override
    public StudentEntity getStudent(Long id, Long tenantId) {
        schoolBusDataScopeService.assertCanAccessStudent(id);
        return findById(studentRepository, id, tenantId);
    }

    @Override
    @Transactional
    public StudentResponse createStudent(StudentUpsertRequest request, Long tenantId, Long actorId) {
        StudentEntity student = new StudentEntity();
        student.markCreated(tenantId, actor(actorId));
        applyStudent(student, request, tenantId);
        student.setStudentCode(codeGeneratorService.generate(
                SchoolBusCode.STUDENT.sequenceKey(), SchoolBusCode.STUDENT.prefix(), tenantId, actorId));
        StudentEntity saved = studentRepository.save(student);
        return mapper.toStudentResponse(saved);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentUpsertRequest request, Long tenantId, Long actorId) {
        StudentEntity student = getStudent(id, tenantId);
        student.markUpdated(actor(actorId));
        // Do not allow overwriting backend-generated studentCode
        applyStudent(student, request, tenantId);
        StudentEntity saved = studentRepository.save(student);
        return mapper.toStudentResponse(saved);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanAccessStudent(id);
        softDeleteById(studentRepository, id, tenantId, actorId);
    }

    private void applyStudent(StudentEntity student, StudentUpsertRequest request, Long tenantId) {
        // Date of birth validation
        if (request.getDateOfBirth() != null && request.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new AppException(AppErrorCode.Student.DOB_FUTURE, messageCommon.getMessage(AppErrorCode.Student.DOB_FUTURE));
        }

        SchoolEntity school = schoolService.getSchool(request.getSchoolId(), tenantId);
        student.setSchool(school);

        Long parentProfileId;
        if (securityService.isParentOnly()) {
            if (student.getId() != null) {
                parentProfileId = student.getParentProfile().getId();
            } else {
                parentProfileId = schoolBusDataScopeService.getCurrentParentProfileIdRequired();
            }
        } else {
            parentProfileId = request.getParentProfileId();
        }

        if (parentProfileId == null) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    messageCommon.getMessage("student.parentProfile.required"));
        }
        student.setParentProfile(parentService.getParent(parentProfileId, tenantId));

        // Default pickup point
        if (request.getPickupPointId() != null) {
            student.setPickupPoint(pickupPointService.getPickupPoint(request.getPickupPointId(), tenantId));
        } else {
            student.setPickupPoint(null);
        }

        // Default dropoff point
        if (request.getDefaultDropoffPointId() != null) {
            student.setDefaultDropoffPoint(
                    pickupPointService.getPickupPoint(request.getDefaultDropoffPointId(), tenantId));
        } else {
            student.setDefaultDropoffPoint(null);
        }

        student.setFullName(request.getFullName());
        student.setGrade(request.getGrade());
        student.setClassName(request.getClassName());
        student.setHomeAddress(request.getHomeAddress());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setSpecialNote(request.getSpecialNote());
        student.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    @Override
    public long countByTenant(Long tenantId) {
        return studentRepository.countByTenantIdAndIsDeletedFalse(tenantId);
    }
}
