package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.StudentParamsRequest;
import serp.project.school_bus_service.application.dto.request.StudentUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.StudentResponse;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.ICodeGeneratorService;
import serp.project.school_bus_service.core.service.IPickupPointService;
import serp.project.school_bus_service.core.service.ISchoolService;
import serp.project.school_bus_service.core.service.IParentService;
import serp.project.school_bus_service.core.service.IStudentService;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.StudentEntity;
import serp.project.school_bus_service.infrastructure.store.repository.StudentRepository;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.code.SchoolBusCode;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends AbstractBaseService<StudentEntity, Long> implements IStudentService {

    private final StudentRepository studentRepository;
    private final ISchoolService schoolService;
    private final IParentService parentService;
    private final IPickupPointService pickupPointService;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;
    private final ICodeGeneratorService codeGeneratorService;

    @Override
    protected BaseRepository<StudentEntity, Long> getRepository() {
        return studentRepository;
    }

    @Override
    public PageResponse<StudentResponse> getStudents(StudentParamsRequest params, Long tenantId) {
        return PageResponse.from(studentRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "fullName", "studentCode", "grade", "homeAddress",
                        "school.name", "parentProfile.fullName", "pickupPoint.name"),
                PageableUtils.from(params,
                        Set.of("id", "fullName", "studentCode", "grade", "createdAt", "updatedAt"), "fullName")),
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
        student.setStudentCode(codeGeneratorService.generate(
                SchoolBusCode.STUDENT.sequenceKey(), SchoolBusCode.STUDENT.prefix(), tenantId, actorId));
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

    private void applyStudent(StudentEntity student, StudentUpsertRequest request, Long tenantId) {
        student.setSchool(schoolService.getSchool(request.getSchoolId(), tenantId));
        student.setParentProfile(parentService.getParent(request.getParentProfileId(), tenantId));
        student.setPickupPoint(request.getPickupPointId() == null ? null
                : pickupPointService.getPickupPoint(request.getPickupPointId(), tenantId));
        student.setFullName(request.getFullName());
        student.setGrade(request.getGrade());
        student.setHomeAddress(request.getHomeAddress());
        student.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }
}
