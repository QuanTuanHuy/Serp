package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.StudentParamsRequest;
import serp.project.school_bus_service.dto.request.StudentUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.StudentResponse;
import serp.project.school_bus_service.dto.response.StudentSummaryResponse;
import serp.project.school_bus_service.entity.StudentEntity;

public interface IStudentService extends IBaseService<StudentEntity, Long> {

    PageResponse<StudentResponse> getStudents(StudentParamsRequest params, Long tenantId);

    StudentSummaryResponse getSummary(Long tenantId);

    StudentResponse getStudentResponse(Long id, Long tenantId);

    StudentEntity getStudent(Long id, Long tenantId);

    StudentResponse createStudent(StudentUpsertRequest request, Long tenantId, Long actorId);

    StudentResponse updateStudent(Long id, StudentUpsertRequest request, Long tenantId, Long actorId);

    void deleteStudent(Long id, Long tenantId, Long actorId);

    long countByTenant(Long tenantId);
}
