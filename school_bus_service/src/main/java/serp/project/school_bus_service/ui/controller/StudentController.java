package serp.project.school_bus_service.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.application.dto.params.StudentParamsRequest;
import serp.project.school_bus_service.application.dto.request.StudentUpsertRequest;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.StudentResponse;
import serp.project.school_bus_service.core.service.IStudentService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/students")
public class StudentController extends AbstractBaseController {

    private final IStudentService studentService;

    public StudentController(IStudentService studentService, AuthUtils authUtils) {
        super(authUtils);
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageResponse<StudentResponse>>> getStudents(
            @ModelAttribute StudentParamsRequest params) {
        return ok("Fetched students", studentService.getStudents(params, getCurrentTenantId()));
    }

    @PostMapping
    public ResponseEntity<GeneralResponse<StudentResponse>> createStudent(
            @Valid @RequestBody StudentUpsertRequest request) {
        return created("Created student",
                studentService.createStudent(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<StudentResponse>> getStudent(@PathVariable Long id) {
        return ok("Fetched student", studentService.getStudentResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponse<StudentResponse>> updateStudent(@PathVariable Long id,
            @Valid @RequestBody StudentUpsertRequest request) {
        return ok("Updated student",
                studentService.updateStudent(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted student");
    }
}
