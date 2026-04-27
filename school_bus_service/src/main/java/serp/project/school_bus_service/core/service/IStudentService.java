package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.StudentParamsRequest;
import serp.project.school_bus_service.application.dto.request.StudentUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.StudentResponse;
import serp.project.school_bus_service.infrastructure.store.model.StudentEntity;

public interface IStudentService {

    PageResponse<StudentResponse> getStudents(StudentParamsRequest params, Long tenantId);

    StudentResponse getStudentResponse(Long id, Long tenantId);

    StudentEntity getStudent(Long id, Long tenantId);

    StudentResponse createStudent(StudentUpsertRequest request, Long tenantId, Long actorId);

    StudentResponse updateStudent(Long id, StudentUpsertRequest request, Long tenantId, Long actorId);

    void deleteStudent(Long id, Long tenantId, Long actorId);
}
