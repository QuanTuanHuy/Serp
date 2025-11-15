/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.AssignUserToDepartmentRequest;
import serp.project.account.core.domain.dto.request.CreateDepartmentRequest;
import serp.project.account.core.domain.dto.request.GetDepartmentParams;
import serp.project.account.core.domain.dto.request.UpdateDepartmentRequest;
import serp.project.account.core.usecase.DepartmentUseCase;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/departments")
@Slf4j
public class DepartmentController {
    private final DepartmentUseCase departmentUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtil;

    @PostMapping
    public ResponseEntity<?> createDepartment(
            @PathVariable Long organizationId,
            @Valid @RequestBody CreateDepartmentRequest request) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var resp = responseUtil.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(resp.getCode()).body(resp);
        }

        var response = departmentUseCase.createDepartment(organizationId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/{departmentId}")
    public ResponseEntity<?> updateDepartment(
            @PathVariable Long organizationId,
            @PathVariable Long departmentId,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var resp = responseUtil.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(resp.getCode()).body(resp);
        }
        var response = departmentUseCase.updateDepartment(departmentId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{departmentId}")
    public ResponseEntity<?> deleteDepartment(
            @PathVariable Long organizationId,
            @PathVariable Long departmentId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var resp = responseUtil.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(resp.getCode()).body(resp);
        }
        var response = departmentUseCase.deleteDepartment(departmentId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<?> getDepartmentById(
            @PathVariable Long organizationId,
            @PathVariable Long departmentId) {

        if (!authUtils.canAccessOrganization(organizationId)) {
            var resp = responseUtil.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(resp.getCode()).body(resp);
        }

        var response = departmentUseCase.getDepartmentById(departmentId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getDepartments(
            @PathVariable Long organizationId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long parentDepartmentId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long managerId) {

        if (!authUtils.canAccessOrganization(organizationId)) {
            var resp = responseUtil.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(resp.getCode()).body(resp);
        }

        GetDepartmentParams params = GetDepartmentParams.builder()
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDir)
                .search(search)
                .organizationId(organizationId)
                .parentDepartmentId(parentDepartmentId)
                .isActive(isActive)
                .managerId(managerId)
                .build();

        var response = departmentUseCase.getDepartmentsByOrganization(params);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{departmentId}/users")
    public ResponseEntity<?> assignUserToDepartment(
            @PathVariable Long organizationId,
            @PathVariable Long departmentId,
            @Valid @RequestBody AssignUserToDepartmentRequest request) {

        if (!authUtils.canAccessOrganization(organizationId)) {
            var resp = responseUtil.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(resp.getCode()).body(resp);
        }

        request.setDepartmentId(departmentId);

        var response = departmentUseCase.assignUserToDepartment(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{departmentId}/members")
    public ResponseEntity<?> getDepartmentMembers(
            @PathVariable Long organizationId,
            @PathVariable Long departmentId) {

        if (!authUtils.canAccessOrganization(organizationId)) {
            var resp = responseUtil.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(resp.getCode()).body(resp);
        }

        var response = departmentUseCase.getMembersByDepartmentId(departmentId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

}
