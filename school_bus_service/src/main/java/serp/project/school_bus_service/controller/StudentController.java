package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.dto.params.StudentParamsRequest;
import serp.project.school_bus_service.dto.request.StudentUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.StudentResponse;
import serp.project.school_bus_service.dto.response.StudentSummaryResponse;
import serp.project.school_bus_service.service.IStudentService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/students")
public class StudentController extends AbstractBaseController {

    private final IStudentService studentService;

    public StudentController(IStudentService studentService, AuthUtils authUtils) {
        super(authUtils);
        this.studentService = studentService;
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.student.read')")
    public ResponseEntity<GeneralResponse<PageResponse<StudentResponse>>> getStudents(
            @ModelAttribute StudentParamsRequest params) {
        return ok("Fetched students", studentService.getStudents(params, getCurrentTenantId()));
    }

    @GetMapping("/summary")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.student.read')")
    public ResponseEntity<GeneralResponse<StudentSummaryResponse>> getSummary() {
        return ok("Fetched student summary", studentService.getSummary(getCurrentTenantId()));
    }

    @PostMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.student.write')")
    public ResponseEntity<GeneralResponse<StudentResponse>> createStudent(
            @Valid @RequestBody StudentUpsertRequest request) {
        return created("Created student",
                studentService.createStudent(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.student.read')")
    public ResponseEntity<GeneralResponse<StudentResponse>> getStudent(@PathVariable Long id) {
        return ok("Fetched student", studentService.getStudentResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.student.write')")
    public ResponseEntity<GeneralResponse<StudentResponse>> updateStudent(@PathVariable Long id,
            @Valid @RequestBody StudentUpsertRequest request) {
        return ok("Updated student",
                studentService.updateStudent(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.student.write')")
    public ResponseEntity<GeneralResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted student");
    }
}
