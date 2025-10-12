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
import serp.project.account.core.domain.dto.request.CreateUserDto;
import serp.project.account.core.domain.dto.request.LoginRequest;
import serp.project.account.core.domain.dto.request.RefreshTokenRequest;
import serp.project.account.core.domain.dto.request.RevokeTokenRequest;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.enums.GroupEnum;
import serp.project.account.core.domain.enums.RoleEnum;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.*;
import serp.project.account.core.service.impl.GroupService;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.ResponseUtils;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUseCase {
    private final IUserService userService;
    private final IOrganizationService organizationService;
    private final IKeycloakUserService keycloakUserService;
    private final IRoleService roleService;
    private final ITokenService tokenService;
    private final GroupService groupService;
    private final IKeycloakGroupService keycloakGroupService;

    private final ResponseUtils responseUtils;

    private final UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> registerUser(CreateUserDto request) {
        try {
            var group = groupService.getGroupByName(GroupEnum.DEFAULT_SERP_GROUP.getName());
            if (group == null) {
                return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
            }

            request.setRoleIds(Collections.emptyList());
            var user = userService.createUser(request);

            var organization = organizationService.createOrganization(request.getOrganization());
            var ownerRole = roleService.getOrCreateOrganizationRole(RoleEnum.ORG_OWNER.getRoleName());
            var adminRole = roleService.getOrCreateOrganizationRole(RoleEnum.ORG_ADMIN.getRoleName());
            var memberRole = roleService.getOrCreateOrganizationRole(RoleEnum.ORG_USER.getRoleName());
            organizationService.assignOrganizationToUser(organization.getId(), user.getId(), ownerRole.getId());
            organizationService.assignOrganizationToUser(organization.getId(), user.getId(), memberRole.getId());
            organizationService.assignOrganizationToUser(organization.getId(), user.getId(), adminRole.getId());

            var createUserKeycloak = userMapper.createUserMapper(user, organization.getId(), request);
            String userKeycloakId = keycloakUserService.createUser(createUserKeycloak);
            userService.updateKeycloakUser(user.getId(), userKeycloakId);

            keycloakGroupService.addUserToGroup(userKeycloakId, group.getKeycloakGroupId());
            groupService.addUserToGroup(user.getId(), group.getId());

            var roles = roleService.getRolesByGroupId(group.getId());
            if (!CollectionUtils.isEmpty(roles)) {
                userService.addRolesToUser(user.getId(), roles.stream().map(RoleEntity::getId).toList());
            }

            return responseUtils.success(user);
        } catch (AppException e) {
            log.error("Register user failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected register user error: {}", e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> login(LoginRequest request) {
        try {
            var user = userService.getUserByEmail(request.getEmail());
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.WRONG_EMAIL_OR_PASSWORD);
            }
            var loginResponse = tokenService.getUserToken(user.getEmail(), request.getPassword());
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
}
