/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.AssignUserToModuleRequest;
import serp.project.account.core.domain.dto.request.BulkAssignUsersRequest;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserModuleAccessEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IKeycloakUserService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.IOrganizationSubscriptionService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.IUserModuleAccessService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleAccessUseCase {

    private final IOrganizationSubscriptionService organizationSubscriptionService;
    private final ISubscriptionPlanService subscriptionPlanService;
    private final IUserModuleAccessService userModuleAccessService;
    private final IUserService userService;
    private final IModuleService moduleService;
    private final IRoleService roleService;
    private final IKeycloakUserService keycloakUserService;

    private final ResponseUtils responseUtils;

    public GeneralResponse<?> canOrganizationAccessModule(Long organizationId, Long moduleId) {
        try {
            log.info("[UseCase] Checking if organization {} can access module {}", organizationId, moduleId);

            if (!organizationSubscriptionService.hasActiveSubscription(organizationId)) {
                log.warn("Organization {} does not have active subscription", organizationId);
                return responseUtils.success(false);
            }

            var subscription = organizationSubscriptionService.getActiveSubscription(organizationId);
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

            if (!organizationSubscriptionService.hasActiveSubscription(organizationId)) {
                log.warn("Organization {} does not have active subscription", organizationId);
                return responseUtils.success(Collections.emptyList());
            }

            var subscription = organizationSubscriptionService.getActiveSubscription(organizationId);
            var plan = subscriptionPlanService.getPlanById(subscription.getSubscriptionPlanId());

            var planModules = subscriptionPlanService.getPlanModules(plan.getId());
            var moduleIds = planModules.stream()
                    .filter(pm -> pm.getIsIncluded())
                    .map(pm -> pm.getModuleId())
                    .toList();

            log.info("Organization {} has access to {} modules", organizationId, moduleIds.size());
            return responseUtils.success(moduleIds);
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
            var subscription = organizationSubscriptionService.getActiveSubscription(organizationId);
            var planModules = subscriptionPlanService.getPlanModules(subscription.getSubscriptionPlanId());

            var planModule = planModules.stream()
                    .filter(pm -> pm.getModuleId().equals(request.getModuleId()))
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
            userService.addRolesToUser(request.getUserId(), assignedRoles.stream()
                    .map(RoleEntity::getId).toList());
            List<String> roleNames = assignedRoles.stream()
                    .map(RoleEntity::getName)
                    .toList();
            keycloakUserService.assignClientRoles(user.getKeycloakId(), module.getKeycloakClientId(), roleNames);

            // Implement later: Send notification

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
            log.info("[UseCase] Bulk assigning {} users to module {} in organization {}",
                    request.getUserIds().size(), request.getModuleId(), request.getOrganizationId());

            var subscription = organizationSubscriptionService.getActiveSubscription(request.getOrganizationId());
            var planModules = subscriptionPlanService.getPlanModules(subscription.getSubscriptionPlanId());

            var planModule = planModules.stream()
                    .filter(pm -> pm.getModuleId().equals(request.getModuleId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(Constants.ErrorMessage.MODULE_NOT_IN_SUBSCRIPTION_PLAN));

            if (planModule.hasUserLimit()) {
                int currentUsers = userModuleAccessService.countActiveUsers(
                        request.getModuleId(), request.getOrganizationId());
                int availableSlots = planModule.getMaxUsersPerModule() - currentUsers;

                if (request.getUserIds().size() > availableSlots) {
                    throw new AppException(Constants.ErrorMessage.MAX_USERS_LIMIT_REACHED);
                }
            }

            var accesses = userModuleAccessService.bulkRegisterUsersToModule(
                    request.getUserIds(),
                    request.getModuleId(),
                    request.getOrganizationId(),
                    assignedBy);

            // Implement later: Send notification

            log.info("[UseCase] Successfully bulk assigned {} users to module {}",
                    accesses.size(), request.getModuleId());
            return responseUtils.success(accesses);
        } catch (AppException e) {
            log.error("Error bulk assigning users to module: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when bulk assigning users to module: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> revokeUserAccessToModule(Long organizationId, Long userId, Long moduleId) {
        try {
            log.info("[UseCase] Revoking user {} access to module {} in organization {}",
                    userId, moduleId, organizationId);

            userModuleAccessService.revokeUserModuleAccess(userId, moduleId, organizationId);

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

            var subscription = organizationSubscriptionService.getSubscriptionById(subscriptionId);
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

}
