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
import serp.project.account.core.domain.dto.request.AssignRoleToUserDto;
import serp.project.account.core.domain.dto.request.UpdateUserInfoRequest;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.UserProfileResponse;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.service.IKeycloakUserService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.IUserService;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.PaginationUtils;
import serp.project.account.kernel.utils.ResponseUtils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUseCase {
    private final IUserService userService;
    private final IKeycloakUserService keycloakUserService;
    private final IRoleService roleService;
    private final IOrganizationService organizationService;

    private final UserMapper userMapper;

    private final ResponseUtils responseUtils;
    private final PaginationUtils paginationUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> assignRolesToUser(AssignRoleToUserDto request) {
        try {
            UserEntity user = userService.getUserById(request.getUserId());
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_NOT_FOUND);
            }

            List<Long> roleIds = request.getRoleIds();
            var roles = roleService.getAllRoles().stream()
                    .filter(r -> roleIds.contains(r.getId()))
                    .toList();
            if (CollectionUtils.isEmpty(roles)) {
                return responseUtils.badRequest(Constants.ErrorMessage.ROLE_NOT_FOUND);
            }

            List<String> realmRoles = roles.stream()
                    .filter(r -> r.getKeycloakClientId() == null)
                    .map(RoleEntity::getName)
                    .toList();
            var clientRoles = roles.stream()
                    .filter(r -> r.getKeycloakClientId() != null)
                    .collect(Collectors.groupingBy(
                            RoleEntity::getKeycloakClientId,
                            Collectors.mapping(RoleEntity::getName, Collectors.toList())));
            keycloakUserService.assignRealmRoles(user.getKeycloakId(), realmRoles);
            for (var entry : clientRoles.entrySet()) {
                keycloakUserService.assignClientRoles(user.getKeycloakId(), entry.getKey(), entry.getValue());
            }
            userService.addRolesToUser(user.getId(), roles.stream().map(RoleEntity::getId).toList());
            return responseUtils.success("Roles assigned successfully");
        } catch (Exception e) {
            log.error("Assign roles to user failed: {}", e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> getUserProfile(Long userId) {
        try {
            var user = userService.getUserProfile(userId);
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_NOT_FOUND);
            }
            return responseUtils.success(user);
        } catch (Exception e) {
            log.error("Get user profile failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getUsers(GetUserParams params) {
        try {
            var pairUsers = userService.getUsers(params);
            var users = pairUsers.getSecond();
            var userProfiles = users.stream().map(userMapper::toProfileResponse).toList();
            if (!CollectionUtils.isEmpty(userProfiles)) {
                var organizationIds = userProfiles.stream()
                        .map(UserProfileResponse::getOrganizationId)
                        .distinct()
                        .toList();
                var organizationMap = organizationService.getOrganizationsByIds(organizationIds).stream()
                        .collect(Collectors.toMap(OrganizationEntity::getId, Function.identity()));
                userProfiles.forEach(profile -> {
                    var organization = organizationMap.get(profile.getOrganizationId());
                    if (organization != null) {
                        profile.setOrganizationName(organization.getName());
                    }
                });
            }

            return responseUtils.success(paginationUtils.getResponse(pairUsers.getFirst(), params.getPage(),
                    params.getPageSize(), userProfiles));
        } catch (Exception e) {
            log.error("Get users failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        try {
            UserEntity user = userService.getUserById(userId);
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_NOT_FOUND);
            }

            UserEntity patch = request.toEntity();

            UserEntity updated = userService.updateUser(userId, patch);
            UserProfileResponse profile = userMapper.toProfileResponse(updated);

            if (profile.getOrganizationId() != null) {
                var orgs = organizationService.getOrganizationsByIds(List.of(profile.getOrganizationId()));
                if (!orgs.isEmpty()) {
                    profile.setOrganizationName(orgs.get(0).getName());
                }
            }

            return responseUtils.success(profile);
        } catch (Exception e) {
            log.error("Update user info failed: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateUserStatus(Long organizationId, Long updatedBy, Long userId, String status,
            Boolean isSerpAdmin) {
        try {
            UserEntity user = userService.getUserById(userId);
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_NOT_FOUND);
            }
            if (!isSerpAdmin) {
                var organization = organizationService.getOrganizationById(user.getPrimaryOrganizationId());
                if (organization == null || !organization.getOwnerId().equals(organizationId)) {
                    return responseUtils.forbidden(Constants.ErrorMessage.FORBIDDEN);
                }
            }
            switch (status.toUpperCase()) {
                case "ACTIVE":
                    user.activate();
                    break;
                case "INACTIVE":
                    user.deactivate();
                    break;
                case "SUSPENDED":
                    user.suspend();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid status: " + status);
            }
            userService.updateUser(userId, user);
            return responseUtils.success("User status updated successfully");
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Update user status failed: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Update user status failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }
}
