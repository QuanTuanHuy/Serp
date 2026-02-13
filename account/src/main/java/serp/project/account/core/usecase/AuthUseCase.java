/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.ChangePasswordRequest;
import serp.project.account.core.domain.dto.request.CreateOrganizationDto;
import serp.project.account.core.domain.dto.request.CreateUserDto;
import serp.project.account.core.domain.dto.request.LoginRequest;
import serp.project.account.core.domain.dto.request.RefreshTokenRequest;
import serp.project.account.core.domain.dto.request.RevokeTokenRequest;
import serp.project.account.core.domain.dto.response.RegisterUserResponse;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.domain.enums.UserType;
import serp.project.account.core.domain.event.UserOnlineInternalEvent;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.ICombineRoleService;
import serp.project.account.core.service.IKeycloakUserService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.ITokenService;
import serp.project.account.core.service.IUserService;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.ResponseUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUseCase {
    private final IUserService userService;
    private final IOrganizationService organizationService;
    private final IKeycloakUserService keycloakUserService;
    private final IRoleService roleService;
    private final ITokenService tokenService;
    private final ICombineRoleService combineRoleService;

    private final ResponseUtils responseUtils;

    private final UserMapper userMapper;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> registerUser(CreateUserDto request) {
        try {
            var orgRoles = roleService.getRolesByScope(RoleScope.ORGANIZATION);
            if (CollectionUtils.isEmpty(orgRoles)) {
                log.error("No organization roles found. Check why?");
                return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
            }

            var createUserRequest = buildCreateUserRequestWithoutRoles(request);
            UserEntity user = provisionUserWithOrganization(createUserRequest, orgRoles, orgRoles, false);
            return responseUtils.success(buildRegisterUserResponse(user));
        } catch (AppException e) {
            log.error("Register user failed", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected register user error", e);
            throw e;
        }
    }

    @Transactional
    public GeneralResponse<?> login(LoginRequest request) {
        try {
            var user = userService.getUserByEmail(request.getEmail());
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.WRONG_EMAIL_OR_PASSWORD);
            }
            if (!user.isActive()) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_INACTIVE);
            }
            var loginResponse = tokenService.getUserToken(user.getEmail(), request.getPassword());
            eventPublisher.publishEvent(new UserOnlineInternalEvent(user.getId(), user.getEmail(), Instant.now()));
            return responseUtils.success(loginResponse);
        } catch (AppException e) {
            log.warn("Login failed for user {}: {}", request.getEmail(), e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected login failure for user {}", request.getEmail(), e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getUserToken(String username, String password) {
        try {
            var tokenResponse = tokenService.getUserToken(username, password);
            return responseUtils.success(tokenResponse);
        } catch (AppException e) {
            log.error("Error getting user token: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting user token", e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
    
    public GeneralResponse<?> refreshToken(RefreshTokenRequest request) {
        try {
            var tokenResponse = tokenService.refreshToken(request.getRefreshToken());
            return responseUtils.success(tokenResponse);
        } catch (AppException e) {
            log.error("Error refreshing token: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when refreshing token", e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public GeneralResponse<?> revokeToken(RevokeTokenRequest request) {
        try {
            tokenService.revokeToken(request.getRefreshToken());
            return responseUtils.success("Token revoked successfully");
        } catch (AppException e) {
            log.error("Error revoking token: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when revoking token", e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createSuperAdmin(String email, String password) {
        try {
            List<RoleEntity> systemRoles = roleService.getRolesByScope(RoleScope.SYSTEM);
            if (CollectionUtils.isEmpty(systemRoles)) {
                log.error("No system roles found, check why?");
                return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
            }
            List<RoleEntity> orgRoles = roleService.getRolesByScope(RoleScope.ORGANIZATION);
            if (CollectionUtils.isEmpty(orgRoles)) {
                log.error("No organization roles found, check why?");
                return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
            }

            var createUserDto = CreateUserDto.builder()
                    .firstName("Super Admin")
                    .lastName("Serp")
                    .email(email)
                    .password(password)
                    .organization(CreateOrganizationDto.builder()
                            .name("Serp Organization")
                            .build())
                    .roleIds(Collections.emptyList())
                    .build();

            List<RoleEntity> allRoles = new ArrayList<>(systemRoles);
            allRoles.addAll(orgRoles);
            provisionUserWithOrganization(createUserDto, allRoles, orgRoles, true);

            return responseUtils.success("Super Admin created successfully");
        } catch (AppException e) {
            log.error("Error creating super admin", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when creating super admin", e);
            throw e;
        }
    }

    public GeneralResponse<?> changePassword(Long userId,
            ChangePasswordRequest request) {
        try {
            UserEntity user = userService.getUserById(userId);
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_NOT_FOUND);
            }
            try {
                tokenService.getUserToken(user.getEmail(), request.getOldPassword());
            } catch (AppException e) {
                if (isAuthenticationFailure(e)) {
                    return responseUtils.badRequest(Constants.ErrorMessage.INVALID_PASSWORD);
                }
                throw e;
            } catch (Exception e) {
                log.error("Error validating old password for user {}", userId, e);
                return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
            }
            if (request.getOldPassword().equals(request.getNewPassword())) {
                return responseUtils.badRequest(Constants.ErrorMessage.PASSWORD_CANNOT_BE_OLD_PASSWORD);
            }

            keycloakUserService.resetPassword(user.getKeycloakId(), request.getNewPassword());

            return responseUtils.success("Password changed successfully");
        } catch (AppException e) {
            log.error("Change password failed: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when changing password", e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    private CreateUserDto buildCreateUserRequestWithoutRoles(CreateUserDto request) {
        return CreateUserDto.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .organization(request.getOrganization())
                .roleIds(Collections.emptyList())
                .build();
    }

    private UserEntity provisionUserWithOrganization(
            CreateUserDto createUserRequest,
            List<RoleEntity> rolesToAssign,
            List<RoleEntity> organizationRoles,
            boolean isSuperAdmin) {
        String userKeycloakId = null;
        try {
            UserEntity user = userService.createUser(createUserRequest);
            OrganizationEntity organization = organizationService.createOrganization(
                    user.getId(), createUserRequest.getOrganization());

            var keycloakUser = userMapper.createUserMapper(
                    user,
                    organization.getId(),
                    createUserRequest.getPassword());
            userKeycloakId = keycloakUserService.createUser(keycloakUser);

            user = updateProvisionedUser(user, organization.getId(), userKeycloakId, isSuperAdmin);
            combineRoleService.assignRolesToUser(user, rolesToAssign);
            assignOrganizationRoles(organization.getId(), user.getId(), organizationRoles);
            return user;
        } catch (Exception e) {
            cleanupKeycloakUser(userKeycloakId);
            throw e;
        }
    }

    private UserEntity updateProvisionedUser(
            UserEntity user,
            Long organizationId,
            String keycloakUserId,
            boolean isSuperAdmin) {
        user.setKeycloakId(keycloakUserId);
        user.setPrimaryOrganizationId(organizationId);
        user.setStatus(UserStatus.ACTIVE);
        user.setUserType(UserType.OWNER);
        if (isSuperAdmin) {
            user.setIsSuperAdmin(true);
        }
        return userService.updateUser(user.getId(), user);
    }

    private void assignOrganizationRoles(Long organizationId, Long userId, List<RoleEntity> roles) {
        roles.forEach(role -> organizationService.assignOrganizationToUser(
                organizationId,
                userId,
                role.getId(),
                true));
    }

    private void cleanupKeycloakUser(String userKeycloakId) {
        if (userKeycloakId == null) {
            return;
        }
        try {
            keycloakUserService.deleteUser(userKeycloakId);
        } catch (Exception cleanupException) {
            log.error("Failed to cleanup keycloak user {}", userKeycloakId, cleanupException);
        }
    }

    private RegisterUserResponse buildRegisterUserResponse(UserEntity user) {
        return RegisterUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .primaryOrganizationId(user.getPrimaryOrganizationId())
                .status(user.getStatus())
                .userType(user.getUserType())
                .build();
    }

    private boolean isAuthenticationFailure(AppException e) {
        if (e.getCode() == null) {
            return false;
        }
        return e.getCode().equals(Constants.HttpStatusCode.UNAUTHORIZED)
                || e.getCode().equals(Constants.HttpStatusCode.BAD_REQUEST);
    }
}
