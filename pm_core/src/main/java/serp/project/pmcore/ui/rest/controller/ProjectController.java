/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import serp.project.pmcore.application.command.project.CreateProjectCommand;
import serp.project.pmcore.domain.constant.RestControllerConstants;
import serp.project.pmcore.domain.dto.request.CreateProjectRequest;
import serp.project.pmcore.domain.dto.response.GeneralResponse;
import serp.project.pmcore.domain.dto.response.ProjectResponse;
import serp.project.pmcore.domain.exception.AppException;
import serp.project.pmcore.domain.exception.ErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.kernel.utils.ResponseUtils;

@RestController
@RequestMapping(RestControllerConstants.PROJECTS)
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    private final CreateProjectCommand createProjectCommand;

    @PostMapping
    public ResponseEntity<GeneralResponse<?>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        ProjectResponse response = createProjectCommand.execute(request, tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

}
