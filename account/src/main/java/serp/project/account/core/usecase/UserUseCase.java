/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.AssignRoleToUserDto;
import serp.project.account.core.domain.dto.request.CreateUserForOrgRequest;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.request.UpdateUserInfoRequest;
import serp.project.account.core.domain.dto.request.UpdateUserRolesRequest;
import serp.project.account.core.domain.dto.request.UpdateUserTypeRequest;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.usecase.user.command.UserAccountCommandService;
import serp.project.account.core.usecase.user.command.UserProvisioningCoordinator;
import serp.project.account.core.usecase.user.command.UserRoleCoordinator;
import serp.project.account.core.usecase.user.export.UserExportService;
import serp.project.account.core.usecase.user.password.UserPasswordResetCoordinator;
import serp.project.account.core.usecase.user.query.UserQueryService;
import serp.project.account.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUseCase {
    private final IOrganizationService organizationService;
    private final UserProvisioningCoordinator userProvisioningCoordinator;
    private final UserRoleCoordinator userRoleCoordinator;
    private final UserAccountCommandService userAccountCommandService;
    private final UserQueryService userQueryService;
    private final UserPasswordResetCoordinator userPasswordResetCoordinator;
    private final UserExportService userExportService;
    private final ResponseUtils responseUtils;

    public GeneralResponse<?> createUserForOrganization(Long organizationId, CreateUserForOrgRequest request) {
        try {
            var organization = organizationService.getOrganizationById(organizationId);
            if (organization == null) {
                return responseUtils.notFound(Constants.ErrorMessage.ORGANIZATION_NOT_FOUND);
            }

            userProvisioningCoordinator.createOrganizationUser(organization, request);
            return responseUtils.success("User created successfully");
        } catch (AppException e) {
            log.error("Error creating user for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating user for organization {}", organizationId, e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public GeneralResponse<?> assignRolesToUser(AssignRoleToUserDto request) {
        try {
            userRoleCoordinator.assignRolesAsSystemAdmin(request.getUserId(), request.getRoleIds());
            return responseUtils.success("Roles assigned successfully");
        } catch (AppException e) {
            log.error("Assign roles to user {} failed: {}", request.getUserId(), e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error assigning roles to user {}", request.getUserId(), e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public GeneralResponse<?> getUserProfile(Long userId) {
        try {
            var userProfile = userQueryService.getUserProfile(userId);
            if (userProfile == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_NOT_FOUND);
            }
            return responseUtils.success(userProfile);
        } catch (AppException e) {
            log.error("Get user profile {} failed: {}", userId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting user profile {}", userId, e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public GeneralResponse<?> getUsers(GetUserParams params) {
        try {
            return responseUtils.success(userQueryService.getUsers(params));
        } catch (AppException e) {
            log.error("Get users failed: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting users", e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public GeneralResponse<?> getUsersByIds(List<Long> ids) {
        try {
            return responseUtils.success(userQueryService.getUsersByIds(ids));
        } catch (AppException e) {
            log.error("Get users by ids failed: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting users by ids", e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public GeneralResponse<?> updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        try {
            return responseUtils.success(userAccountCommandService.updateUserInfo(userId, request));
        } catch (AppException e) {
            log.error("Update user info {} failed: {}", userId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating user info {}", userId, e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public GeneralResponse<?> updateUserStatus(Long organizationId, Long updatedBy, Long userId, String status,
            Boolean isSerpAdmin) {
        try {
            userAccountCommandService.updateStatus(organizationId, updatedBy, userId, status,
                    Boolean.TRUE.equals(isSerpAdmin));
            return responseUtils.success("User status updated successfully");
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Update user status {} failed: {}", userId, e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (AppException e) {
            log.error("Update user status {} failed: {}", userId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating user status {}", userId, e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public GeneralResponse<?> getUserStats(Long organizationId) {
        try {
            return responseUtils.success(userQueryService.getUserStats(organizationId));
        } catch (AppException e) {
            log.error("Get user stats failed: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting user stats for organization {}", organizationId, e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public GeneralResponse<?> getUserDetail(Long organizationId, Long userId) {
        try {
            return responseUtils.success(userQueryService.getUserDetail(organizationId, userId));
        } catch (AppException e) {
            log.error("Get user detail failed: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting user detail {} in organization {}", userId, organizationId, e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public GeneralResponse<?> updateUserRoles(Long organizationId, Long userId, UpdateUserRolesRequest request) {
        try {
            userRoleCoordinator.updateOrganizationRoles(organizationId, userId, request.getRoleIds());
            return responseUtils.success("User roles updated successfully");
        } catch (AppException e) {
            log.error("Update user roles failed: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating user roles for user {}", userId, e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public GeneralResponse<?> updateUserType(Long organizationId, Long userId, UpdateUserTypeRequest request) {
        try {
            userAccountCommandService.updateType(organizationId, userId, request.getUserType());
            return responseUtils.success("User type updated successfully");
        } catch (IllegalArgumentException e) {
            log.error("Update user type {} failed: {}", userId, e.getMessage());
            return responseUtils.badRequest("Invalid user type: " + request.getUserType());
        } catch (AppException e) {
            log.error("Update user type {} failed: {}", userId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating user type {}", userId, e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public GeneralResponse<?> resetUserPassword(Long organizationId, Long userId, Long requestedBy) {
        try {
            userPasswordResetCoordinator.initiateReset(organizationId, userId, requestedBy);
            return responseUtils.success("Password reset initiated successfully");
        } catch (AppException e) {
            log.error("Reset user password {} failed: {}", userId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error resetting password for user {}", userId, e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public GeneralResponse<?> exportUsers(Long organizationId, String format) {
        try {
            return responseUtils.success(userExportService.exportUsers(organizationId, format));
        } catch (AppException e) {
            log.error("Export users failed: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error exporting users for organization {}", organizationId, e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
