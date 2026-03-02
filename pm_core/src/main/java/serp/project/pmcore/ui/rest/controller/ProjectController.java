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
import serp.project.pmcore.core.domain.dto.request.CreateProjectRequest;
import serp.project.pmcore.core.domain.dto.request.GetProjectParams;
import serp.project.pmcore.core.domain.dto.request.UpdateProjectRequest;
import serp.project.pmcore.core.domain.dto.response.GeneralResponse;
import serp.project.pmcore.core.domain.dto.response.ProjectResponse;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.core.usecase.ProjectUseCase;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.kernel.utils.ResponseUtils;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectUseCase projectUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @PostMapping
    public ResponseEntity<GeneralResponse<?>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        ProjectResponse response = projectUseCase.createProject(request, tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<?>> getProjectById(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        ProjectResponse response = projectUseCase.getProjectById(id, tenantId);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<GeneralResponse<?>> getProjectByKey(@PathVariable String key) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        ProjectResponse response = projectUseCase.getProjectByKey(key, tenantId);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<?>> getProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String projectTypeKey,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        GetProjectParams params = GetProjectParams.builder()
                .search(search)
                .categoryId(categoryId)
                .projectTypeKey(projectTypeKey)
                .archived(archived)
                .build();
        params.setPage(page);
        params.setPageSize(pageSize);
        params.setSortBy(sortBy);
        params.setSortDirection(sortDirection);

        Map<String, Object> response = projectUseCase.getProjects(tenantId, params);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<?>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        ProjectResponse response = projectUseCase.updateProject(id, request, tenantId, userId);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<?>> deleteProject(@PathVariable Long id) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        projectUseCase.deleteProject(id, tenantId, userId);
        return ResponseEntity.ok(responseUtils.status("Project deleted successfully"));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<GeneralResponse<?>> archiveProject(@PathVariable Long id) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        ProjectResponse response = projectUseCase.archiveProject(id, tenantId, userId);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PostMapping("/{id}/unarchive")
    public ResponseEntity<GeneralResponse<?>> unarchiveProject(@PathVariable Long id) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        ProjectResponse response = projectUseCase.unarchiveProject(id, tenantId, userId);
        return ResponseEntity.ok(responseUtils.success(response));
    }
}
