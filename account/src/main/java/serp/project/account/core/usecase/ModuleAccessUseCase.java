/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.message.CreateNotificationEvent;
import serp.project.account.core.domain.dto.request.AssignUserToModuleRequest;
import serp.project.account.core.domain.dto.request.BulkAssignUsersRequest;
import serp.project.account.core.domain.dto.request.BulkModuleAccessUsersRequest;
import serp.project.account.core.domain.dto.response.OrgModuleAccessResponse;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;
import serp.project.account.core.domain.entity.UserModuleAccessEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.ICombineRoleService;
import serp.project.account.core.service.IKeycloakUserService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.INotificationService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.IUserModuleAccessService;
import serp.project.account.core.service.IUserService;
import serp.project.account.core.usecase.support.UserSyncPublisher;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.ResponseUtils;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import serp.project.account.core.domain.dto.request.UpdateModuleAccessSettingsRequest;
import serp.project.account.core.domain.dto.response.BulkModuleAccessResponse;
import serp.project.account.core.domain.dto.response.BulkModuleAccessOutcome;
import serp.project.account.core.domain.dto.response.ModuleAccessSettingsResponse;
import serp.project.account.core.domain.dto.response.AutoGrantBackfillResponse;
import serp.project.account.core.domain.enums.BulkModuleAccessStatus;
import serp.project.account.core.service.IOrganizationModuleAccessSettingService;
import serp.project.account.core.usecase.support.ModuleAutoGrantService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleAccessUseCase {

    private final ISubscriptionService subscriptionService;
    private final ISubscriptionPlanService subscriptionPlanService;
    private final IUserModuleAccessService userModuleAccessService;
    private final IUserService userService;
    private final IModuleService moduleService;
    private final IRoleService roleService;
    private final ICombineRoleService combineRoleService;

    private final IKeycloakUserService keycloakUserService;
    private final INotificationService notificationService;
    private final UserSyncPublisher userSyncPublisher;

    private final ResponseUtils responseUtils;

    private final IOrganizationModuleAccessSettingService moduleAccessSettingService;
    private final ModuleAutoGrantService moduleAutoGrantService;

    public GeneralResponse<?> canOrganizationAccessModule(Long organizationId, Long moduleId) {
        try {
            log.info("[UseCase] Checking if organization {} can access module {}", organizationId, moduleId);

            var subscription = subscriptionService.getActiveOrPendingUpgrade(organizationId);
            var plan = subscriptionPlanService.getPlanById(subscription.getSubscriptionPlanId());

            var planModules = subscriptionPlanService.getPlanModules(plan.getId());
            boolean hasModule = planModules.stream()
                    .anyMatch(pm -> pm.getModuleId().equals(moduleId) && pm.getIsIncluded());

            log.info("Organization {} {} access module {}",
                    organizationId, hasModule ? "can" : "cannot", moduleId);
            return responseUtils.success(hasModule);
        } catch (AppException e) {
            log.error("Error checking module access for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when checking module access for organization {}: {}", organizationId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getAccessibleModulesForOrganization(Long organizationId) {
        try {
            log.info("[UseCase] Getting accessible modules for organization {}", organizationId);

            var subscription = subscriptionService.getActiveOrPendingUpgrade(organizationId);
            var plan = subscriptionPlanService.getPlanById(subscription.getSubscriptionPlanId());
            var planModules = subscriptionPlanService.getPlanModules(plan.getId());
            var moduleIds = planModules.stream()
                    .filter(SubscriptionPlanModuleEntity::getIsIncluded)
                    .map(SubscriptionPlanModuleEntity::getModuleId)
                    .toList();
            var allRoles = roleService.getAllRoles();
            var allModules = moduleService.getAllModules();

            int totalUsers = userService.countUsersByOrganizationId(organizationId);

            var autoGrantByModuleId = moduleAccessSettingService.getByOrganizationId(organizationId).stream()
                    .collect(Collectors.toMap(
                            setting -> setting.getModuleId(),
                            setting -> Boolean.TRUE.equals(setting.getAutoGrantToNewUsers()),
                            (left, right) -> right));

            List<OrgModuleAccessResponse> result = new ArrayList<>();
            result.addAll(allModules.stream()
                    .filter(m -> moduleIds.contains(m.getId()))
                    .map(m -> OrgModuleAccessResponse.builder()
                            .organizationId(organizationId)
                            .moduleId(m.getId())
                            .moduleName(m.getModuleName())
                            .moduleCode(m.getCode())
                            .moduleDescription(m.getDescription())
                            .isActive(true)
                            .grantedAt(subscription.getActivatedAt())
                            .activeUserCount(userModuleAccessService.countActiveUsers(m.getId(), organizationId))
                            .totalUsersCount(totalUsers)
                            .isAutoGrantToNewUsers(autoGrantByModuleId.getOrDefault(m.getId(), false))
                            .requiredRoles(allRoles.stream()
                                    .filter(r -> r.getModuleId() != null && r.getModuleId().equals(m.getId()))
                                    .map(RoleEntity::getName)
                                    .toList())
                            .build())
                    .toList());
            result.addAll(allModules.stream()
                    .filter(m -> !moduleIds.contains(m.getId()))
                    .map(m -> OrgModuleAccessResponse.builder()
                            .organizationId(organizationId)
                            .moduleId(m.getId())
                            .moduleName(m.getModuleName())
                            .moduleCode(m.getCode())
                            .moduleDescription(m.getDescription())
                            .isActive(false)
                            .isAutoGrantToNewUsers(false)
                            .build())
                    .toList());

            log.info("Organization {} has access to {} modules", organizationId, moduleIds.size());
            return responseUtils.success(result);
        } catch (AppException e) {
            log.error("Error getting accessible modules for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting accessible modules for organization {}: {}", organizationId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> assignUserToModule(Long organizationId, AssignUserToModuleRequest request,
            Long assignedBy) {
        try {
            log.info("[UseCase] Assigning user {} to module {} in organization {}",
                    request.getUserId(), request.getModuleId(), organizationId);
            var user = userService.getUserById(request.getUserId());
            if (user == null) {
                throw new AppException(Constants.ErrorMessage.USER_NOT_FOUND);
            }
            var module = moduleService.getModuleByIdFromCache(request.getModuleId());
            if (module == null) {
                throw new AppException(Constants.ErrorMessage.MODULE_NOT_FOUND);
            }
            var subscription = subscriptionService.getActiveOrPendingUpgrade(organizationId);
            var planModules = subscriptionPlanService.getPlanModules(subscription.getSubscriptionPlanId());

            var planModule = planModules.stream()
                    .filter(pm -> pm.getModuleId().equals(request.getModuleId()))
                    .filter(SubscriptionPlanModuleEntity::isAccessible)
                    .findFirst()
                    .orElseThrow(() -> new AppException(Constants.ErrorMessage.MODULE_NOT_IN_SUBSCRIPTION_PLAN));

            if (planModule.hasUserLimit()) {
                int currentUsers = userModuleAccessService.countActiveUsers(request.getModuleId(), organizationId);
                if (currentUsers >= planModule.getMaxUsersPerModule()) {
                    throw new AppException(Constants.ErrorMessage.MAX_USERS_LIMIT_REACHED);
                }
            }

            var access = userModuleAccessService.registerUserToModuleWithExpiration(
                    request.getUserId(),
                    request.getModuleId(),
                    organizationId,
                    assignedBy,
                    subscription.getEndDate());
            List<RoleEntity> moduleRoles = roleService.getRolesByModuleId(request.getModuleId());
            List<RoleEntity> assignedRoles = Collections.emptyList();
            if (request.getRoleId() != null) {
                assignedRoles = moduleRoles.stream()
                        .filter(role -> role.getId().equals(request.getRoleId()))
                        .toList();
            }
            if (assignedRoles.isEmpty()) {
                assignedRoles = moduleRoles.stream()
                        .filter(RoleEntity::isAutoAssigned)
                        .toList();
            }
            if (assignedRoles.isEmpty()) {
                log.error("No roles found to assign to user {} for module {}", request.getUserId(),
                        request.getModuleId());
                throw new AppException(Constants.ErrorMessage.NO_ROLES_FOUND_FOR_MODULE);
            }
            combineRoleService.assignRolesToUser(user, assignedRoles);
            userSyncPublisher.publishUserSync(organizationId, request.getUserId());

            if (user.getKeycloakId() != null) {
                keycloakUserService.logoutUser(user.getKeycloakId());
                log.info("Logged out user {} to refresh permissions", request.getUserId());
            }

            var notificationEvent = CreateNotificationEvent.builder()
                    .userId(request.getUserId())
                    .tenantId(organizationId)
                    .title("Module Access Granted")
                    .message("You have been assigned to the module: " + module.getModuleName())
                    .actionUrl("/" + module.getCode().toLowerCase())
                    .build();
            notificationService.sendNotification(notificationEvent);

            log.info("[UseCase] Successfully assigned user {} to module {}",
                    request.getUserId(), request.getModuleId());
            return responseUtils.success(access);
        } catch (AppException e) {
            log.error("Error assigning user to module: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when assigning user to module: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> bulkAssignUsersToModule(BulkAssignUsersRequest request, Long assignedBy) {
        try {
            List<Long> requestedUserIds = request.getUserIds() == null ? List.of() : request.getUserIds();
            List<Long> uniqueUserIds = distinctUserIds(requestedUserIds);

            log.info("[UseCase] Bulk assigning {} users to module {} in organization {}",
                    requestedUserIds.size(), request.getModuleId(), request.getOrganizationId());

            var subscription = subscriptionService.getActiveOrPendingUpgrade(request.getOrganizationId());
            var planModule = subscriptionPlanService.getPlanModules(subscription.getSubscriptionPlanId()).stream()
                    .filter(pm -> pm.getModuleId().equals(request.getModuleId()))
                    .filter(SubscriptionPlanModuleEntity::isAccessible)
                    .findFirst()
                    .orElseThrow(() -> new AppException(Constants.ErrorMessage.MODULE_NOT_IN_SUBSCRIPTION_PLAN));

            List<RoleEntity> moduleRoles = resolveBulkAssignRoles(request.getModuleId(), request.getRoleId());

            Map<Long, UserEntity> usersById = userService
                    .getUsersByOrganizationIdAndIds(request.getOrganizationId(), uniqueUserIds)
                    .stream()
                    .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (left, right) -> left));
            Map<Long, UserModuleAccessEntity> accessByUserId = userModuleAccessService
                    .getUserModuleAccessesByUserIdsAndModuleIdAndOrgId(
                            uniqueUserIds,
                            request.getModuleId(),
                            request.getOrganizationId())
                    .stream()
                    .collect(Collectors.toMap(UserModuleAccessEntity::getUserId, Function.identity(),
                            (left, right) -> left));

            int remainingSlots = resolveRemainingSlots(planModule, request.getModuleId(), request.getOrganizationId());
            List<Long> userIdsToGrant = new ArrayList<>();
            List<UserEntity> usersToGrant = new ArrayList<>();
            Map<Long, BulkModuleAccessOutcome> outcomesByUserId = new LinkedHashMap<>();

            for (Long userId : uniqueUserIds) {
                UserEntity user = usersById.get(userId);
                if (user == null) {
                    outcomesByUserId.put(userId,
                            new BulkModuleAccessOutcome(BulkModuleAccessStatus.SKIPPED, Constants.BulkAccessSkipReason.USER_NOT_FOUND));
                    continue;
                }

                UserModuleAccessEntity existingAccess = accessByUserId.get(userId);
                if (existingAccess != null && existingAccess.isActiveAccess()) {
                    outcomesByUserId.put(userId,
                            new BulkModuleAccessOutcome(BulkModuleAccessStatus.SKIPPED, Constants.BulkAccessSkipReason.ALREADY_HAS_ACCESS));
                    continue;
                }

                if (remainingSlots <= 0) {
                    outcomesByUserId.put(userId,
                            new BulkModuleAccessOutcome(BulkModuleAccessStatus.SKIPPED,
                                    Constants.BulkAccessSkipReason.MAX_USERS_LIMIT_REACHED));
                    continue;
                }

                userIdsToGrant.add(userId);
                usersToGrant.add(user);
                remainingSlots--;
                outcomesByUserId.put(userId,
                        new BulkModuleAccessOutcome(BulkModuleAccessStatus.GRANTED, null));
            }

            if (!CollectionUtils.isEmpty(userIdsToGrant)) {
                userModuleAccessService.bulkRegisterUsersToModuleWithExpiration(
                        userIdsToGrant,
                        request.getModuleId(),
                        request.getOrganizationId(),
                        assignedBy,
                        subscription.getEndDate());
                combineRoleService.assignRolesToUsers(usersToGrant, moduleRoles);
                usersToGrant.forEach(user -> {
                    userSyncPublisher.publishUserSync(request.getOrganizationId(), user.getId());
                    if (user.getKeycloakId() != null) {
                        keycloakUserService.logoutUser(user.getKeycloakId());
                    }
                });
            }

            BulkModuleAccessResponse response = buildBulkResponse(
                    request.getModuleId(),
                    requestedUserIds,
                    outcomesByUserId);

            log.info("[UseCase] Bulk assigned {} users to module {}, skipped {} users",
                    response.getGrantedCount(), request.getModuleId(), response.getSkippedCount());
            return responseUtils.success(response);
        } catch (AppException e) {
            log.error("Error bulk assigning users to module: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when bulk assigning users to module: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> bulkRevokeUsersFromModule(
            Long organizationId,
            Long moduleId,
            BulkModuleAccessUsersRequest request,
            Long revokedBy) {
        try {
            List<Long> requestedUserIds = request.getUserIds() == null ? List.of() : request.getUserIds();
            List<Long> uniqueUserIds = distinctUserIds(requestedUserIds);

            log.info("[UseCase] Bulk revoking {} users from module {} in organization {}",
                    requestedUserIds.size(), moduleId, organizationId);

            Map<Long, UserEntity> usersById = userService
                    .getUsersByOrganizationIdAndIds(organizationId, uniqueUserIds)
                    .stream()
                    .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (left, right) -> left));
            Map<Long, UserModuleAccessEntity> accessByUserId = userModuleAccessService
                    .getUserModuleAccessesByUserIdsAndModuleIdAndOrgId(
                            uniqueUserIds,
                            moduleId,
                            organizationId)
                    .stream()
                    .collect(Collectors.toMap(UserModuleAccessEntity::getUserId, Function.identity(),
                            (left, right) -> left));

            List<UserModuleAccessEntity> accessesToRevoke = new ArrayList<>();
            List<UserEntity> usersToRevoke = new ArrayList<>();
            Map<Long, BulkModuleAccessOutcome> outcomesByUserId = new LinkedHashMap<>();

            for (Long userId : uniqueUserIds) {
                UserEntity user = usersById.get(userId);
                if (user == null) {
                    outcomesByUserId.put(userId,
                            new BulkModuleAccessOutcome(BulkModuleAccessStatus.SKIPPED, Constants.BulkAccessSkipReason.USER_NOT_FOUND));
                    continue;
                }

                UserModuleAccessEntity access = accessByUserId.get(userId);
                if (access == null) {
                    outcomesByUserId.put(userId,
                            new BulkModuleAccessOutcome(BulkModuleAccessStatus.SKIPPED,
                                    Constants.BulkAccessSkipReason.USER_MODULE_ACCESS_NOT_FOUND));
                    continue;
                }

                access.deactivate();
                accessesToRevoke.add(access);
                usersToRevoke.add(user);
                outcomesByUserId.put(userId,
                        new BulkModuleAccessOutcome(BulkModuleAccessStatus.REVOKED, null));
            }

            if (!CollectionUtils.isEmpty(accessesToRevoke)) {
                userModuleAccessService.saveAll(accessesToRevoke);
                List<RoleEntity> moduleRoles = roleService.getRolesByModuleId(moduleId);
                if (!CollectionUtils.isEmpty(moduleRoles)) {
                    combineRoleService.removeRolesFromUsers(usersToRevoke, moduleRoles);
                }
                usersToRevoke.forEach(user -> {
                    userSyncPublisher.publishUserSync(organizationId, user.getId());
                    if (user.getKeycloakId() != null) {
                        keycloakUserService.logoutUser(user.getKeycloakId());
                    }
                });
            }

            BulkModuleAccessResponse response = buildBulkResponse(
                    moduleId,
                    requestedUserIds,
                    outcomesByUserId);
            log.info("[UseCase] Bulk revoked {} users from module {}, skipped {} users",
                    response.getRevokedCount(), moduleId, response.getSkippedCount());
            return responseUtils.success(response);
        } catch (AppException e) {
            log.error("Error bulk revoking users from module: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when bulk revoking users from module: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> revokeUserAccessToModule(Long organizationId, Long userId, Long moduleId) {
        try {
            log.info("[UseCase] Revoking user {} access to module {} in organization {}",
                    userId, moduleId, organizationId);

            userModuleAccessService.revokeUserModuleAccess(userId, moduleId, organizationId);

            List<RoleEntity> moduleRoles = roleService.getRolesByModuleId(moduleId);
            var user = userService.getUserById(userId);
            if (user != null && !CollectionUtils.isEmpty(moduleRoles)) {
                combineRoleService.removeRolesFromUser(user, moduleRoles);
                userSyncPublisher.publishUserSync(organizationId, userId);
                if (user.getKeycloakId() != null) {
                    keycloakUserService.logoutUser(user.getKeycloakId());
                    log.info("Logged out user {} to refresh permissions", userId);
                }
            }

            // Implement later: Send notification

            log.info("[UseCase] Successfully revoked user {} access to module {}", userId, moduleId);
            return responseUtils.success("User access revoked successfully");
        } catch (AppException e) {
            log.error("Error revoking user access: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when revoking user access: {}", e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> getUsersWithAccessToModule(Long organizationId, Long moduleId) {
        try {
            log.info("[UseCase] Getting users with access to module {} in organization {}",
                    moduleId, organizationId);

            var users = userModuleAccessService.getUsersWithModuleAccess(moduleId, organizationId);
            List<Long> userIds = users.stream()
                    .map(UserModuleAccessEntity::getUserId)
                    .toList();
            var userProfiles = userService.getUserProfilesByIds(userIds);

            log.info("[UseCase] Retrieved {} users with access to module {}", userProfiles.size(), moduleId);
            return responseUtils.success(userProfiles);
        } catch (AppException e) {
            log.error("Error getting users with module access: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting users with module access: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getModulesAccessibleByUser(Long organizationId, Long userId) {
        try {
            log.info("[UseCase] Getting modules accessible by user {} in organization {}",
                    userId, organizationId);

            var modules = userModuleAccessService.getUserModuleAccesses(userId, organizationId);
            List<Long> moduleIds = modules.stream()
                    .map(UserModuleAccessEntity::getModuleId)
                    .toList();
            var moduleEntities = moduleService.getModulesByIds(moduleIds);

            log.info("[UseCase] Retrieved {} modules accessible by user {}", moduleEntities.size(), userId);
            return responseUtils.success(moduleEntities);
        } catch (AppException e) {
            log.error("Error getting user's accessible modules: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting user's accessible modules: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> revokeAccessForSubscription(Long subscriptionId) {
        try {
            log.info("[UseCase] Revoking module access for subscription {}", subscriptionId);

            var subscription = subscriptionService.getSubscriptionById(subscriptionId);
            var plan = subscriptionPlanService.getPlanById(subscription.getSubscriptionPlanId());

            var planModules = subscriptionPlanService.getPlanModules(plan.getId());

            int usersRevoked = 0;
            for (var planModule : planModules) {
                if (planModule.getIsIncluded()) {
                    var users = userModuleAccessService.getUsersWithModuleAccess(
                            planModule.getModuleId(), subscription.getOrganizationId());

                    for (var userAccess : users) {
                        userModuleAccessService.revokeUserModuleAccess(
                                userAccess.getUserId(),
                                planModule.getModuleId(),
                                subscription.getOrganizationId());
                        usersRevoked++;
                    }
                }
            }

            log.info("[UseCase] Revoked access for {} users in subscription {}", usersRevoked, subscriptionId);
            return responseUtils.success("Module access revoked for " + usersRevoked + " users");
        } catch (AppException e) {
            log.error("Error revoking module access for subscription {}: {}", subscriptionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when revoking module access for subscription {}: {}", subscriptionId,
                    e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateModuleAccessSettings(
            Long organizationId,
            Long moduleId,
            UpdateModuleAccessSettingsRequest request,
            Long updatedBy) {
        try {
            validateModuleInSubscription(organizationId, moduleId);
            var setting = moduleAccessSettingService.upsertAutoGrantToNewUsers(
                    organizationId,
                    moduleId,
                    request.getAutoGrantToNewUsers(),
                    updatedBy);
            return responseUtils.success(ModuleAccessSettingsResponse.builder()
                    .organizationId(setting.getOrganizationId())
                    .moduleId(setting.getModuleId())
                    .autoGrantToNewUsers(setting.getAutoGrantToNewUsers())
                    .updatedBy(setting.getUpdatedBy())
                    .updatedAt(setting.getUpdatedAt())
                    .build());
        } catch (AppException e) {
            log.error("Error updating module access settings: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating module access settings: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> backfillAutoGrant(Long organizationId, Long moduleId, Long grantedBy) {
        try {
            return responseUtils.success(moduleAutoGrantService.backfillExistingUsers(
                    organizationId,
                    moduleId,
                    grantedBy));
        } catch (AppException e) {
            log.error("Error backfilling module auto-grant: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error backfilling module auto-grant: {}", e.getMessage(), e);
            throw e;
        }
    }

    private List<RoleEntity> resolveBulkAssignRoles(Long moduleId, Long roleId) {
        List<RoleEntity> moduleRoles = roleService.getRolesByModuleId(moduleId);
        if (roleId != null) {
            RoleEntity requestedRole = moduleRoles.stream()
                    .filter(role -> role.getId().equals(roleId))
                    .findFirst()
                    .orElseThrow(() -> new AppException(Constants.ErrorMessage.ROLE_NOT_FOUND));
            return List.of(requestedRole);
        }

        List<RoleEntity> defaultRoles = moduleRoles.stream()
                .filter(RoleEntity::isAutoAssigned)
                .toList();
        if (CollectionUtils.isEmpty(defaultRoles)) {
            throw new AppException(Constants.ErrorMessage.NO_ROLES_FOUND_FOR_MODULE);
        }
        return defaultRoles;
    }

    private List<Long> distinctUserIds(List<Long> requestedUserIds) {
        if (CollectionUtils.isEmpty(requestedUserIds)) {
            return List.of();
        }
        return requestedUserIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private int resolveRemainingSlots(SubscriptionPlanModuleEntity planModule, Long moduleId, Long organizationId) {
        if (!planModule.hasUserLimit()) {
            return Integer.MAX_VALUE;
        }
        int currentUsers = userModuleAccessService.countActiveUsers(moduleId, organizationId);
        return Math.max(0, planModule.getMaxUsersPerModule() - currentUsers);
    }

    private BulkModuleAccessResponse buildBulkResponse(
            Long moduleId,
            List<Long> requestedUserIds,
            Map<Long, BulkModuleAccessOutcome> outcomesByUserId) {
        BulkModuleAccessResponse response = BulkModuleAccessResponse.empty(moduleId, requestedUserIds.size());
        Set<Long> seenUserIds = new java.util.HashSet<>();
        for (Long userId : requestedUserIds) {
            if (userId == null || !seenUserIds.add(userId)) {
                continue;
            }
            BulkModuleAccessOutcome outcome = outcomesByUserId.get(userId);
            if (outcome == null) {
                response.markSkipped(userId, Constants.BulkAccessSkipReason.USER_NOT_FOUND);
                continue;
            }
            if (outcome.status() == BulkModuleAccessStatus.GRANTED) {
                response.markGranted(userId);
            } else if (outcome.status() == BulkModuleAccessStatus.REVOKED) {
                response.markRevoked(userId);
            } else {
                response.markSkipped(userId, outcome.reason());
            }
        }
        return response;
    }

    private SubscriptionPlanModuleEntity validateModuleInSubscription(Long organizationId, Long moduleId) {
        var subscription = subscriptionService.getActiveOrPendingUpgrade(organizationId);
        return subscriptionPlanService.getPlanModules(subscription.getSubscriptionPlanId()).stream()
                .filter(SubscriptionPlanModuleEntity::isAccessible)
                .filter(planModule -> planModule.getModuleId().equals(moduleId))
                .findFirst()
                .orElseThrow(() -> new AppException(Constants.ErrorMessage.MODULE_NOT_IN_SUBSCRIPTION_PLAN));
    }
}
