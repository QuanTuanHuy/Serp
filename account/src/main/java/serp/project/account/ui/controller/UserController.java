/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.AssignRoleToUserDto;
import serp.project.account.core.domain.dto.request.UpdateUserInfoRequest;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.usecase.UserUseCase;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {
    private final UserUseCase userUseCase;

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtil;

    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long organizationId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            organizationId = authUtils.getCurrentTenantId().orElse(null);
        }

        GetUserParams params = GetUserParams.builder()
                .page(page).pageSize(pageSize).sortBy(sortBy).sortDirection(sortDir)
                .search(search)
                .status(status)
                .organizationId(organizationId)
                .build();
        var response = userUseCase.getUsers(params);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/profile/me")
    public ResponseEntity<?> getMyProfile() {
        Optional<Long> userIdOpt = authUtils.getCurrentUserId();
        if (userIdOpt.isEmpty()) {
            var response = responseUtil.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }
        var response = userUseCase.getUserProfile(userIdOpt.get());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/{userId}/info")
    public ResponseEntity<?> updateUserInfo(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserInfoRequest request
    ) {
        var currentUserIdOpt = authUtils.getCurrentUserId();
        var organizationIdOpt = authUtils.getCurrentTenantId();
        if (currentUserIdOpt.isEmpty() || organizationIdOpt.isEmpty()) {
            var response = responseUtil.forbidden(Constants.ErrorMessage.FORBIDDEN);
            return ResponseEntity.status(response.getCode()).body(response);
        }
        if (!currentUserIdOpt.get().equals(userId)) {
            if (!authUtils.canAccessOrganization(organizationIdOpt.get())) {
                var response = responseUtil.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
                return ResponseEntity.status(response.getCode()).body(response);
            }
        }

        var response = userUseCase.updateUserInfo(userId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/assign-roles")
    public ResponseEntity<?> assignRoles(@Valid @RequestBody AssignRoleToUserDto request) {
        var response = userUseCase.assignRolesToUser(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

}
