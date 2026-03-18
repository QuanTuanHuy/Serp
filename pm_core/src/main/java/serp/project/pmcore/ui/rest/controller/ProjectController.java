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
import serp.project.pmcore.application.query.project.GetProjectQuery;
import serp.project.pmcore.domain.constant.RestControllerConstants;
import serp.project.pmcore.domain.dto.request.project.CreateProjectRequest;
import serp.project.pmcore.domain.dto.response.GeneralResponse;
import serp.project.pmcore.domain.dto.response.project.ProjectDetailResponse;
import serp.project.pmcore.domain.dto.response.project.ProjectResponse;
import serp.project.pmcore.domain.exception.AccessDeniedException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
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

    private final GetProjectQuery getProjectQuery;

    @PostMapping
    public ResponseEntity<GeneralResponse<?>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        ProjectResponse response = createProjectCommand.execute(request, tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<ProjectDetailResponse>> getProjectById(
            @PathVariable("id") Long projectId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));
        var response = getProjectQuery.executeById(projectId, tenantId);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<GeneralResponse<ProjectDetailResponse>> getProjectByKey(
            @PathVariable String key) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));
        var response = getProjectQuery.executeByKey(key, tenantId);
        return ResponseEntity.ok(responseUtils.success(response));
    }

}
