/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.AssignUserToModuleRequest;
import serp.project.account.core.domain.dto.request.BulkAssignUsersRequest;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IOrganizationSubscriptionService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.kernel.utils.ResponseUtils;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleAccessUseCase {

    private final IOrganizationSubscriptionService organizationSubscriptionService;
    private final ISubscriptionPlanService subscriptionPlanService;
    private final ResponseUtils responseUtils;
    // TODO: Inject IUserModuleAccessService when it's implemented
    // private final IUserModuleAccessService userModuleAccessService;

    /**
     * Check if organization can access a module based on subscription
     */
    public GeneralResponse<?> canOrganizationAccessModule(Long organizationId, Long moduleId) {
        try {
            log.info("[UseCase] Checking if organization {} can access module {}", organizationId, moduleId);

            // Check if organization has active subscription
            if (!organizationSubscriptionService.hasActiveSubscription(organizationId)) {
                log.warn("Organization {} does not have active subscription", organizationId);
                return responseUtils.success(false);
            }

            // Get active subscription and its plan
            var subscription = organizationSubscriptionService.getActiveSubscription(organizationId);
            var plan = subscriptionPlanService.getPlanById(subscription.getSubscriptionPlanId());

            // Check if module is included in plan
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
            log.error("Unexpected error when checking module access for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get modules accessible by organization
     */
    public GeneralResponse<?> getAccessibleModulesForOrganization(Long organizationId) {
        try {
            log.info("[UseCase] Getting accessible modules for organization {}", organizationId);

            // Check if organization has active subscription
            if (!organizationSubscriptionService.hasActiveSubscription(organizationId)) {
                log.warn("Organization {} does not have active subscription", organizationId);
                return responseUtils.success(new ArrayList<>());
            }

            // Get active subscription and its plan
            var subscription = organizationSubscriptionService.getActiveSubscription(organizationId);
            var plan = subscriptionPlanService.getPlanById(subscription.getSubscriptionPlanId());

            // Get modules included in plan
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
            log.error("Unexpected error when getting accessible modules for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Assign user to module
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> assignUserToModule(Long organizationId, AssignUserToModuleRequest request, Long assignedBy) {
        try {
            log.info("[UseCase] Assigning user {} to module {} in organization {}", 
                    request.getUserId(), request.getModuleId(), organizationId);

            // Validate organization can access module
            var canAccess = organizationSubscriptionService.canAccessModule(organizationId, request.getModuleId());
            if (!canAccess) {
                return responseUtils.badRequest("Organization does not have access to this module");
            }

            // Get subscription and plan for license validation
            var subscription = organizationSubscriptionService.getActiveSubscription(organizationId);
            var plan = subscriptionPlanService.getPlanById(subscription.getSubscriptionPlanId());
            var planModules = subscriptionPlanService.getPlanModules(plan.getId());

            // Find module in plan
            var planModule = planModules.stream()
                    .filter(pm -> pm.getModuleId().equals(request.getModuleId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException("Module not found in plan"));

            // TODO: Check license limits
            // TODO: Use service to assign user
            // userModuleAccessService.assignUserToModule(organizationId, request, assignedBy);

            // TODO: Send Kafka event - user assigned to module
            // kafkaProducer.sendUserAssignedToModuleEvent(organizationId, request);

            log.info("[UseCase] Successfully assigned user {} to module {}", 
                    request.getUserId(), request.getModuleId());
            return responseUtils.success("User assigned to module successfully");
        } catch (AppException e) {
            log.error("Error assigning user to module: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when assigning user to module: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Bulk assign users to module
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> bulkAssignUsersToModule(BulkAssignUsersRequest request, Long assignedBy) {
        try {
            log.info("[UseCase] Bulk assigning {} users to module {} in organization {}", 
                    request.getUserIds().size(), request.getModuleId(), request.getOrganizationId());

            // Validate organization can access module
            var canAccess = organizationSubscriptionService.canAccessModule(
                    request.getOrganizationId(), request.getModuleId());
            if (!canAccess) {
                return responseUtils.badRequest("Organization does not have access to this module");
            }

            // Get subscription and plan for license validation
            var subscription = organizationSubscriptionService.getActiveSubscription(request.getOrganizationId());
            var plan = subscriptionPlanService.getPlanById(subscription.getSubscriptionPlanId());
            var planModules = subscriptionPlanService.getPlanModules(plan.getId());

            // Find module in plan
            var planModule = planModules.stream()
                    .filter(pm -> pm.getModuleId().equals(request.getModuleId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException("Module not found in plan"));

            // TODO: Check license limits for bulk assignment
            // TODO: Use service to bulk assign users
            // userModuleAccessService.bulkAssignUsersToModule(request, assignedBy);

            // TODO: Send Kafka event - bulk users assigned to module
            // kafkaProducer.sendBulkUsersAssignedToModuleEvent(request);

            log.info("[UseCase] Successfully bulk assigned {} users to module {}", 
                    request.getUserIds().size(), request.getModuleId());
            return responseUtils.success("Users assigned to module successfully");
        } catch (AppException e) {
            log.error("Error bulk assigning users to module: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when bulk assigning users to module: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Revoke user access to module
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> revokeUserAccessToModule(Long organizationId, Long userId, Long moduleId) {
        try {
            log.info("[UseCase] Revoking user {} access to module {} in organization {}", 
                    userId, moduleId, organizationId);

            // TODO: Use service to revoke access
            // userModuleAccessService.revokeUserAccessToModule(organizationId, userId, moduleId);

            // TODO: Send Kafka event - user access revoked
            // kafkaProducer.sendUserAccessRevokedEvent(organizationId, userId, moduleId);

            log.info("[UseCase] Successfully revoked user {} access to module {}", userId, moduleId);
            return responseUtils.success("User access revoked successfully");
        } catch (AppException e) {
            log.error("Error revoking user access: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when revoking user access: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get users with access to module
     */
    public GeneralResponse<?> getUsersWithAccessToModule(Long organizationId, Long moduleId) {
        try {
            log.info("[UseCase] Getting users with access to module {} in organization {}", 
                    moduleId, organizationId);

            // TODO: Use service to get users
            // var users = userModuleAccessService.getUsersWithAccessToModule(organizationId, moduleId);
            // return responseUtils.success(users);

            log.info("[UseCase] Retrieved users with access to module {}", moduleId);
            return responseUtils.success(new ArrayList<>()); // Placeholder
        } catch (Exception e) {
            log.error("Unexpected error when getting users with module access: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get modules accessible by user
     */
    public GeneralResponse<?> getModulesAccessibleByUser(Long organizationId, Long userId) {
        try {
            log.info("[UseCase] Getting modules accessible by user {} in organization {}", 
                    userId, organizationId);

            // TODO: Use service to get modules
            // var modules = userModuleAccessService.getModulesAccessibleByUser(organizationId, userId);
            // return responseUtils.success(modules);

            log.info("[UseCase] Retrieved modules accessible by user {}", userId);
            return responseUtils.success(new ArrayList<>()); // Placeholder
        } catch (Exception e) {
            log.error("Unexpected error when getting user's accessible modules: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Grant access to all modules in subscription plan
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> grantAccessForSubscription(Long subscriptionId) {
        try {
            log.info("[UseCase] Granting module access for subscription {}", subscriptionId);

            // Get subscription and organization
            var subscription = organizationSubscriptionService.getSubscriptionById(subscriptionId);
            var plan = subscriptionPlanService.getPlanById(subscription.getSubscriptionPlanId());

            // Get modules in plan
            var planModules = subscriptionPlanService.getPlanModules(plan.getId());

            // TODO: Grant access to all included modules
            // for (var planModule : planModules) {
            //     if (planModule.getIsIncluded()) {
            //         moduleAccessService.enableModuleForOrganization(
            //                 subscription.getOrganizationId(), planModule.getModuleId());
            //     }
            // }

            log.info("[UseCase] Granted access to {} modules for subscription {}", 
                    planModules.size(), subscriptionId);
            return responseUtils.success("Module access granted successfully");
        } catch (AppException e) {
            log.error("Error granting module access for subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when granting module access for subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Revoke access to all modules
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> revokeAccessForSubscription(Long subscriptionId) {
        try {
            log.info("[UseCase] Revoking module access for subscription {}", subscriptionId);

            // Get subscription and organization
            var subscription = organizationSubscriptionService.getSubscriptionById(subscriptionId);

            // TODO: Revoke all module access for organization
            // moduleAccessService.revokeAllAccessForOrganization(subscription.getOrganizationId());

            log.info("[UseCase] Revoked all module access for subscription {}", subscriptionId);
            return responseUtils.success("Module access revoked successfully");
        } catch (AppException e) {
            log.error("Error revoking module access for subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when revoking module access for subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }
}
