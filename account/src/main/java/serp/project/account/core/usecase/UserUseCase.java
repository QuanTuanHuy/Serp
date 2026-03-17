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
import serp.project.account.core.domain.dto.message.SendEmailRequest;
import serp.project.account.core.domain.dto.request.AssignRoleToUserDto;
import serp.project.account.core.domain.dto.request.CreateUserForOrgRequest;
import serp.project.account.core.domain.dto.request.UpdateUserInfoRequest;
import serp.project.account.core.domain.dto.request.UpdateUserTypeRequest;
import serp.project.account.core.domain.dto.request.UpdateUserRolesRequest;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.UserDetailResponse;
import serp.project.account.core.domain.dto.response.UserProfileResponse;
import serp.project.account.core.domain.entity.DepartmentEntity;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.PasswordResetRequestEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.domain.enums.UserType;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.*;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.kernel.property.PasswordResetProperties;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.PasswordResetTokenUtils;
import serp.project.account.kernel.utils.PaginationUtils;
import serp.project.account.kernel.utils.ResponseUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final IDepartmentService departmentService;
    private final IUserDepartmentService userDepartmentService;
    private final IUserModuleAccessService userModuleAccessService;
    private final IModuleService moduleService;
    private final INotificationService notificationService;

    private final RoleUseCase roleUseCase;

    private final UserMapper userMapper;
    private final PasswordResetProperties passwordResetProperties;

    private final ResponseUtils responseUtils;
    private final PaginationUtils paginationUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createUserForOrganization(Long tenantId, CreateUserForOrgRequest request) {
        String keycloakUserId = null;
        try {
            var organization = organizationService.getOrganizationById(tenantId);
            if (organization == null) {
                return responseUtils.notFound(Constants.ErrorMessage.ORGANIZATION_NOT_FOUND);
            }

            var user = userService.createUser(tenantId, request);
            final Long userId = user.getId();

            var keycloakUser = userMapper.createUserMapper(user, tenantId, request.getPassword());
            keycloakUserId = keycloakUserService.createUser(keycloakUser);

            user.setKeycloakId(keycloakUserId);
            user.setStatus(UserStatus.ACTIVE);
            userService.updateUser(userId, user);

            List<RoleEntity> roles;
            if (CollectionUtils.isEmpty(request.getRoleIds())) {
                roles = roleService.getRolesByScope(RoleScope.ORGANIZATION).stream()
                        .filter(RoleEntity::isAutoAssigned)
                        .toList();
            } else {
                var validRolesResponse = roleUseCase.getValidRolesForOrganization(tenantId);
                if (!validRolesResponse.isSuccess()) {
                    log.error("Failed to get valid roles for organization with id {}: {}", tenantId,
                            validRolesResponse.getMessage());
                    throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
                }
                roles = ((List<RoleEntity>) validRolesResponse.getData()).stream()
                        .filter(role -> request.getRoleIds().contains(role.getId()))
                        .toList();
            }
            if (CollectionUtils.isEmpty(roles)) {
                log.error("No roles found to assign to user with id {} in organization with id {}. Check why?", userId,
                        tenantId);
                throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
            }
            userService.addRolesToUser(userId, roles.stream().map(RoleEntity::getId).toList());
            keycloakUserService.assignRealmRoles(keycloakUserId, roles.stream()
                    .filter(r -> r.getKeycloakClientId() == null)
                    .map(RoleEntity::getName)
                    .toList());

            roles.stream()
                    .filter(RoleEntity::isOrganizationRole)
                    .forEach(
                            role -> organizationService.assignOrganizationToUser(tenantId, userId, role.getId(), true));

            return responseUtils.success("User created successfully");

        } catch (AppException e) {
            log.error("Error creating user for organization: {}", e.getMessage());
            if (keycloakUserId != null) {
                keycloakUserService.deleteUser(keycloakUserId);
            }
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating user for organization: {}", e.getMessage());
            if (keycloakUserId != null) {
                keycloakUserService.deleteUser(keycloakUserId);
            }
            throw e;
        }
    }

    /*
     * System admin only
     */
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
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
                var organizationMap = organizationService.getOrganizationsByIds(organizationIds).stream()
                        .collect(Collectors.toMap(OrganizationEntity::getId, Function.identity()));

                var deptIdSet = users.stream()
                        .map(UserEntity::getPrimaryDepartmentId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
                var deptMap = !deptIdSet.isEmpty()
                        ? departmentService.getDepartmentsByIds(deptIdSet).stream()
                                .collect(Collectors.toMap(DepartmentEntity::getId, Function.identity()))
                        : Map.<Long, DepartmentEntity>of();

                var userIds = users.stream().map(UserEntity::getId).toList();
                var moduleCountMap = userModuleAccessService.countActiveModulesByUserIds(userIds);

                for (int i = 0; i < userProfiles.size(); i++) {
                    var profile = userProfiles.get(i);
                    var user = users.get(i);

                    var organization = organizationMap.get(profile.getOrganizationId());
                    if (organization != null) {
                        profile.setOrganizationName(organization.getName());
                    }
                    if (user.getPrimaryDepartmentId() != null) {
                        profile.setPrimaryDepartmentId(user.getPrimaryDepartmentId());
                        var dept = deptMap.get(user.getPrimaryDepartmentId());
                        if (dept != null) {
                            profile.setPrimaryDepartmentName(dept.getName());
                        }
                    }
                    profile.setModuleAccessCount(moduleCountMap.getOrDefault(user.getId(), 0));
                }
            }

            return responseUtils.success(paginationUtils.getResponse(pairUsers.getFirst(), params.getPage(),
                    params.getPageSize(), userProfiles));
        } catch (Exception e) {
            log.error("Get users failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getUsersByIds(List<Long> ids) {
        try {
            var users = userService.getUsersByIds(ids);
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
            return responseUtils.success(userProfiles);
        } catch (Exception e) {
            log.error("Get users by ids failed: {}", e.getMessage());
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

    public GeneralResponse<?> getUserStats(Long organizationId) {
        try {
            var stats = userService.getUserStats(organizationId);
            return responseUtils.success(stats);
        } catch (Exception e) {
            log.error("Get user stats failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getUserDetail(Long organizationId, Long userId) {
        try {
            UserEntity user = userService.getUserById(userId);
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_NOT_FOUND);
            }
            if (!user.getPrimaryOrganizationId().equals(organizationId)) {
                return responseUtils.forbidden(Constants.ErrorMessage.USER_NOT_IN_ORGANIZATION);
            }

            var orgName = "";
            var org = organizationService.getOrganizationById(organizationId);
            if (org != null) {
                orgName = org.getName();
            }

            var roleDetails = user.getRoles() != null
                    ? user.getRoles().stream()
                            .map(r -> UserDetailResponse.RoleDetail.builder()
                                    .id(r.getId()).name(r.getName())
                                    .scope(r.getScope() != null ? r.getScope().name() : null)
                                    .description(r.getDescription())
                                    .moduleName(r.getModuleId() != null
                                            ? moduleService.getModuleById(r.getModuleId()).getModuleName()
                                            : null)
                                    .build())
                            .toList()
                    : List.<UserDetailResponse.RoleDetail>of();

            var userDepartments = userDepartmentService.getUserDepartmentsByUserId(userId);
            var departmentDetails = userDepartments.stream()
                    .map(ud -> {
                        DepartmentEntity dept = null;
                        try {
                            dept = departmentService.getDepartmentById(ud.getDepartmentId());
                        } catch (Exception ignored) {
                        }
                        return UserDetailResponse.DepartmentDetail.builder()
                                .id(ud.getDepartmentId())
                                .name(dept != null ? dept.getName() : null)
                                .isPrimary(ud.getIsPrimary())
                                .jobTitle(ud.getJobTitle())
                                .build();
                    })
                    .toList();

            var moduleAccesses = userModuleAccessService.getUserModuleAccesses(userId, organizationId);
            var moduleDetails = moduleAccesses.stream()
                    .map(ma -> {
                        var mod = moduleService.getModuleById(ma.getModuleId());
                        return UserDetailResponse.ModuleAccessDetail.builder()
                                .moduleId(ma.getModuleId())
                                .moduleName(mod != null ? mod.getModuleName() : null)
                                .moduleCode(mod != null ? mod.getCode() : null)
                                .isActive(ma.getIsActive())
                                .grantedAt(ma.getGrantedAt())
                                .build();
                    })
                    .toList();

            var response = UserDetailResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phoneNumber(user.getPhoneNumber())
                    .avatarUrl(user.getAvatarUrl())
                    .userType(user.getUserType())
                    .status(user.getStatus())
                    .lastLoginAt(user.getLastLoginAt())
                    .createdAt(user.getCreatedAt())
                    .timezone(user.getTimezone())
                    .preferredLanguage(user.getPreferredLanguage())
                    .organizationId(organizationId)
                    .organizationName(orgName)
                    .roles(roleDetails)
                    .departments(departmentDetails)
                    .moduleAccesses(moduleDetails)
                    .build();

            return responseUtils.success(response);
        } catch (AppException e) {
            log.error("Get user detail failed: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Get user detail failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateUserRoles(Long organizationId, Long userId, UpdateUserRolesRequest request) {
        try {
            UserEntity user = userService.getUserById(userId);
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_NOT_FOUND);
            }
            if (!user.getPrimaryOrganizationId().equals(organizationId)) {
                return responseUtils.forbidden(Constants.ErrorMessage.USER_NOT_IN_ORGANIZATION);
            }

            List<Long> newRoleIds = request.getRoleIds();
            var allRolesResponse = roleUseCase.getValidRolesForOrganization(organizationId);
            if (!allRolesResponse.isSuccess()) {
                log.error("Failed to get valid roles for organization with id {}: {}", organizationId,
                        allRolesResponse.getMessage());
                return responseUtils.internalServerError("Failed to get valid roles for organization");
            }
            var allRoles = (List<RoleEntity>) allRolesResponse.getData();
            var validRoles = allRoles.stream()
                    .filter(r -> newRoleIds.contains(r.getId()))
                    .filter(r -> !r.isSystemRole())
                    .toList();

            if (validRoles.isEmpty()) {
                return responseUtils.badRequest(Constants.ErrorMessage.ROLE_NOT_FOUND);
            }

            var currentRoleIds = user.getRoles() != null
                    ? user.getRoles().stream().map(RoleEntity::getId).toList()
                    : List.<Long>of();

            var rolesToAdd = validRoles.stream()
                    .filter(r -> !currentRoleIds.contains(r.getId()))
                    .toList();
            var rolesToRemove = user.getRoles() != null
                    ? user.getRoles().stream()
                            .filter(r -> !newRoleIds.contains(r.getId()))
                            .toList()
                    : List.<RoleEntity>of();

            if (!rolesToAdd.isEmpty()) {
                List<String> realmRoles = rolesToAdd.stream()
                        .filter(r -> r.getKeycloakClientId() == null)
                        .map(RoleEntity::getName).toList();
                var clientRoles = rolesToAdd.stream()
                        .filter(r -> r.getKeycloakClientId() != null)
                        .collect(Collectors.groupingBy(
                                RoleEntity::getKeycloakClientId,
                                Collectors.mapping(RoleEntity::getName, Collectors.toList())));

                if (!realmRoles.isEmpty()) {
                    keycloakUserService.assignRealmRoles(user.getKeycloakId(), realmRoles);
                }
                for (var entry : clientRoles.entrySet()) {
                    keycloakUserService.assignClientRoles(user.getKeycloakId(), entry.getKey(), entry.getValue());
                }
                userService.addRolesToUser(userId, rolesToAdd.stream().map(RoleEntity::getId).toList());
            }

            if (!rolesToRemove.isEmpty()) {
                List<String> realmRoles = rolesToRemove.stream()
                        .filter(r -> r.getKeycloakClientId() == null)
                        .map(RoleEntity::getName).toList();
                var clientRoles = rolesToRemove.stream()
                        .filter(r -> r.getKeycloakClientId() != null)
                        .collect(Collectors.groupingBy(
                                RoleEntity::getKeycloakClientId,
                                Collectors.mapping(RoleEntity::getName, Collectors.toList())));

                if (!realmRoles.isEmpty()) {
                    // TODO: Implement remove realm roles in Keycloak
                }
                for (var entry : clientRoles.entrySet()) {
                    keycloakUserService.revokeClientRoles(user.getKeycloakId(), entry.getKey(), entry.getValue());
                }

                userService.removeRolesFromUser(userId, rolesToRemove.stream().map(RoleEntity::getId).toList());
            }

            return responseUtils.success("User roles updated successfully");
        } catch (Exception e) {
            log.error("Update user roles failed: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateUserType(Long organizationId, Long userId, UpdateUserTypeRequest request) {
        try {
            UserEntity user = userService.getUserById(userId);
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_NOT_FOUND);
            }
            if (!user.getPrimaryOrganizationId().equals(organizationId)) {
                return responseUtils.forbidden(Constants.ErrorMessage.USER_NOT_IN_ORGANIZATION);
            }

            UserType newType = UserType.valueOf(request.getUserType().toUpperCase());
            UserEntity patch = UserEntity.builder().userType(newType).build();
            userService.updateUser(userId, patch);

            return responseUtils.success("User type updated successfully");
        } catch (IllegalArgumentException e) {
            log.error("Update user type failed: {}", e.getMessage());
            return responseUtils.badRequest("Invalid user type: " + request.getUserType());
        } catch (Exception e) {
            log.error("Update user type failed: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> resetUserPassword(Long organizationId, Long userId, Long requestedBy) {
        try {
            UserEntity user = userService.getUserById(userId);
            if (user == null) {
                throw new AppException(Constants.ErrorMessage.USER_NOT_FOUND,
                        Constants.HttpStatusCode.BAD_REQUEST);
            }
            if (!user.getPrimaryOrganizationId().equals(organizationId)) {
                throw new AppException(Constants.ErrorMessage.USER_NOT_IN_ORGANIZATION,
                        Constants.HttpStatusCode.FORBIDDEN);
            }
            if (!user.isActive()) {
                throw new AppException("Cannot reset password for inactive or suspended user",
                        Constants.HttpStatusCode.BAD_REQUEST);
            }
            if (user.getKeycloakId() == null) {
                throw new AppException("User does not have a Keycloak account",
                        Constants.HttpStatusCode.BAD_REQUEST);
            }
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                throw new AppException("User does not have an email address",
                        Constants.HttpStatusCode.BAD_REQUEST);
            }

            userService.invalidatePendingPasswordResetByUserId(userId);

            long expirationMinutes = passwordResetProperties.getExpirationMinutes();
            String rawToken = PasswordResetTokenUtils.generateToken();
            long expiresAt = Instant.now().plusSeconds(expirationMinutes * 60L).toEpochMilli();
            PasswordResetRequestEntity resetRequest = userService.createPasswordResetRequest(
                    userId,
                    organizationId,
                    user.getEmail(),
                    requestedBy,
                    PasswordResetTokenUtils.hashToken(rawToken),
                    expiresAt);

            SendEmailRequest emailRequest = SendEmailRequest.resetPasswordEmail(
                    user.getEmail(),
                    user.getFirstName(),
                    buildPasswordResetLink(rawToken),
                    expirationMinutes
            );
            notificationService.sendEmail(
                    requestedBy,
                    organizationId,
                    "PASSWORD_RESET_REQUEST",
                    resetRequest.getId(),
                    emailRequest
            );

            return responseUtils.success("Password reset initiated successfully");
        } catch (Exception e) {
            log.error("Reset user password failed: {}", e.getMessage());
            throw e;
        }
    }

    private String buildPasswordResetLink(String token) {
        String frontendResetUrl = passwordResetProperties.getFrontendResetUrl();
        if (frontendResetUrl == null || frontendResetUrl.isBlank()) {
            frontendResetUrl = "http://localhost:3000/auth/reset-password";
        }

        String separator = frontendResetUrl.contains("?") ? "&" : "?";
        return frontendResetUrl + separator + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    public GeneralResponse<?> exportUsers(Long organizationId, String format) {
        try {
            var users = userService.getUsersByOrganizationId(organizationId);
            var userProfiles = users.stream().map(userMapper::toProfileResponse).toList();

            if (!CollectionUtils.isEmpty(userProfiles)) {
                var orgIds = userProfiles.stream()
                        .map(UserProfileResponse::getOrganizationId)
                        .filter(Objects::nonNull)
                        .distinct().toList();
                var orgMap = organizationService.getOrganizationsByIds(orgIds).stream()
                        .collect(Collectors.toMap(OrganizationEntity::getId, Function.identity()));
                userProfiles.forEach(p -> {
                    var org = orgMap.get(p.getOrganizationId());
                    if (org != null)
                        p.setOrganizationName(org.getName());
                });
            }

            if ("csv".equalsIgnoreCase(format)) {
                var sb = new StringBuilder();
                sb.append("ID,Email,First Name,Last Name,User Type,Status,Roles,Last Login,Created At\n");
                for (var p : userProfiles) {
                    sb.append(p.getId()).append(",");
                    sb.append(escapeCsv(p.getEmail())).append(",");
                    sb.append(escapeCsv(p.getFirstName())).append(",");
                    sb.append(escapeCsv(p.getLastName())).append(",");
                    sb.append(p.getUserType()).append(",");
                    sb.append(p.getStatus()).append(",");
                    sb.append(escapeCsv(p.getRoles() != null ? String.join(";", p.getRoles()) : "")).append(",");
                    sb.append(p.getLastLoginAt() != null ? p.getLastLoginAt() : "").append(",");
                    sb.append(p.getCreatedAt() != null ? p.getCreatedAt() : "").append("\n");
                }
                return responseUtils.success(sb.toString());
            }

            return responseUtils.success(userProfiles);
        } catch (Exception e) {
            log.error("Export users failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

}
