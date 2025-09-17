/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.ui.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import serp.project.ptm_optimization.core.domain.dto.request.CreateTaskRegistrationDto;
import serp.project.ptm_optimization.core.usecase.TaskRegistrationUseCase;
import serp.project.ptm_optimization.kernel.utils.AuthUtils;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/task-registrations")
public class TaskRegistrationController {
    private final TaskRegistrationUseCase taskRegistrationUseCase;

    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> createTaskRegistration(
            @Valid @RequestBody CreateTaskRegistrationDto request) {
        Long userId = authUtils.getCurrentUserId().get();
        var response = taskRegistrationUseCase.createTaskRegistration(userId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

}
