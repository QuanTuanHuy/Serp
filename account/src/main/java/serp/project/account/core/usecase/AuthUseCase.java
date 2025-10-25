/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.CreateOrganizationDto;
import serp.project.account.core.domain.dto.request.CreateUserDto;
import serp.project.account.core.domain.dto.request.LoginRequest;
import serp.project.account.core.domain.dto.request.RefreshTokenRequest;
import serp.project.account.core.domain.dto.request.RevokeTokenRequest;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.domain.enums.UserType;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.*;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.ResponseUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUseCase {
    private final IUserService userService;
    private final IOrganizationService organizationService;
    private final IKeycloakUserService keycloakUserService;
    private final IRoleService roleService;
    private final ITokenService tokenService;

    private final ResponseUtils responseUtils;

    private final UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> registerUser(CreateUserDto request) {
        String userKeycloakId = null;
        try {
            var orgRoles = roleService.getRolesByScope(RoleScope.ORGANIZATION);
            if (CollectionUtils.isEmpty(orgRoles)) {
                log.error("No organization roles found. Check why?");
                return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
            }

            request.setRoleIds(Collections.emptyList());
            UserEntity user = userService.createUser(request);

            OrganizationEntity organization = organizationService.createOrganization(user.getId(),
                    request.getOrganization());
            log.info("Organization id: {}", organization.getId());

            var keycloakUser = userMapper.createUserMapper(user, organization.getId(), request.getPassword());
            userKeycloakId = keycloakUserService.createUser(keycloakUser);
            log.info("Keycloak user id: {}", userKeycloakId);

            user.setKeycloakId(userKeycloakId);
            user.setPrimaryOrganizationId(organization.getId());
            user.setStatus(UserStatus.ACTIVE);
            user.setUserType(UserType.OWNER);

            user = userService.updateUser(user.getId(), user);
            final Long userId = user.getId();

            keycloakUserService.assignRealmRoles(userKeycloakId, orgRoles.stream()
                    .filter(r -> r.getKeycloakClientId() == null)
                    .map(RoleEntity::getName)
                    .toList());
            userService.addRolesToUser(user.getId(), orgRoles.stream().map(RoleEntity::getId).toList());

            orgRoles.forEach(role -> {
                organizationService.assignOrganizationToUser(organization.getId(), userId, role.getId(), true);
            });

            return responseUtils.success(user);
        } catch (AppException e) {
            log.error("Register user failed: {}", e.getMessage());
            if (userKeycloakId != null) {
                keycloakUserService.deleteUser(userKeycloakId);
            }
            throw e;
        } catch (Exception e) {
            log.error("Unexpected register user error: {}", e.getMessage());
            if (userKeycloakId != null) {
                keycloakUserService.deleteUser(userKeycloakId);
            }
            throw e;
        }
    }

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
            user.setLastLoginAt(Instant.now().toEpochMilli());
            userService.updateUser(user.getId(), user);
            return responseUtils.success(loginResponse);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getUserToken(String username, String password) {
        try {
            var tokenResponse = tokenService.getUserToken(username, password);
            return responseUtils.success(tokenResponse);
        } catch (AppException e) {
            log.error("Error getting user token: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting user token: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
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
            log.error("Unexpected error when refreshing token: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
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
            log.error("Unexpected error when revoking token: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createSuperAdmin(String email, String password) {
        String userKeycloakId = null;
        try {
            List<RoleEntity> systemRoles = roleService.getRolesByScope(RoleScope.SYSTEM);
            if (CollectionUtils.isEmpty(systemRoles)) {
                log.error("No system roles found, check why?");
                return null;
            }
            List<RoleEntity> orgRoles = roleService.getRolesByScope(RoleScope.ORGANIZATION);
            if (CollectionUtils.isEmpty(orgRoles)) {
                log.error("No organization roles found, check why?");
                return null;
            }

            var createUserDto = CreateUserDto.builder()
                    .firstName("Super Admin")
                    .lastName("Serp")
                    .email(email)
                    .password(password)
                    .organization(CreateOrganizationDto.builder()
                            .name("Serp Organization")
                            .build())
                    .build();

            UserEntity user = userService.createUser(createUserDto);

            OrganizationEntity organization = organizationService.createOrganization(user.getId(),
                    createUserDto.getOrganization());
            log.info("Organization id: {}", organization.getId());

            var keycloakUser = userMapper.createUserMapper(user, organization.getId(), createUserDto.getPassword());
            userKeycloakId = keycloakUserService.createUser(keycloakUser);

            user.setKeycloakId(userKeycloakId);
            user.setIsSuperAdmin(true);
            user.setPrimaryOrganizationId(organization.getId());
            user.setStatus(UserStatus.ACTIVE);
            user.setUserType(UserType.OWNER);

            user = userService.updateUser(user.getId(), user);
            final Long userId = user.getId();

            List<RoleEntity> combinedRoles = Stream.concat(
                    systemRoles.stream(),
                    orgRoles.stream()).collect(Collectors.toList());
            keycloakUserService.assignRealmRoles(userKeycloakId, combinedRoles.stream()
                    .filter(r -> r.getKeycloakClientId() == null)
                    .map(RoleEntity::getName)
                    .toList());
            userService.addRolesToUser(user.getId(), combinedRoles.stream().map(RoleEntity::getId).toList());

            orgRoles.forEach(role -> {
                organizationService.assignOrganizationToUser(organization.getId(), userId, role.getId(), true);
            });

            return responseUtils.success("Super Admin created successfully");
        } catch (AppException e) {
            log.error("Error creating super admin: {}", e.getMessage());
            if (userKeycloakId != null) {
                keycloakUserService.deleteUser(userKeycloakId);
            }
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when creating super admin: {}", e.getMessage());
            if (userKeycloakId != null) {
                keycloakUserService.deleteUser(userKeycloakId);
            }
            throw e;
        }
    }
}
