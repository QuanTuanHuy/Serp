/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.AddModuleToPlanRequest;
import serp.project.account.core.domain.dto.request.CreateSubscriptionPlanRequest;
import serp.project.account.core.domain.dto.request.UpdateSubscriptionPlanRequest;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.ICombineRoleService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IUserModuleAccessService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanUseCase {

    private final ISubscriptionPlanService subscriptionPlanService;
    private final ISubscriptionService subscriptionService;
    private final ICombineRoleService combineRoleService;
    private final IRoleService roleService;
    private final IUserService userService;
    private final IUserModuleAccessService userModuleAccessService;
    private final IOrganizationService organizationService;
    private final IModuleService moduleService;

    private final ResponseUtils responseUtils;

    private final AsyncTaskExecutor asyncTaskExecutor;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createPlan(CreateSubscriptionPlanRequest request, Long createdBy) {
        try {
            var plan = subscriptionPlanService.createPlan(request, createdBy);
            log.info("[UseCase] Successfully created subscription plan with ID: {}", plan.getId());
            return responseUtils.success(plan);
        } catch (AppException e) {
            log.error("Error creating subscription plan: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when creating subscription plan: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updatePlan(Long planId, UpdateSubscriptionPlanRequest request, Long updatedBy) {
        try {
            var plan = subscriptionPlanService.updatePlan(planId, request, updatedBy);

            log.info("[UseCase] Successfully updated subscription plan: {}", planId);
            return responseUtils.success(plan);
        } catch (AppException e) {
            log.error("Error updating subscription plan {}: {}", planId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when updating subscription plan {}: {}", planId, e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> deletePlan(Long planId) {
        try {
            subscriptionPlanService.deletePlan(planId);

            log.info("[UseCase] Successfully deleted subscription plan: {}", planId);
            return responseUtils.success("Subscription plan deleted successfully");
        } catch (AppException e) {
            log.error("Error deleting subscription plan {}: {}", planId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when deleting subscription plan {}: {}", planId, e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> getPlanById(Long planId) {
        try {
            var plan = subscriptionPlanService.getPlanById(planId);
            return responseUtils.success(plan);
        } catch (AppException e) {
            log.error("Error getting subscription plan {}: {}", planId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting subscription plan {}: {}", planId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getPlanByCode(String planCode) {
        try {
            var plan = subscriptionPlanService.getPlanByCode(planCode);
            return responseUtils.success(plan);
        } catch (AppException e) {
            log.error("Error getting subscription plan by code {}: {}", planCode, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting subscription plan by code {}: {}", planCode, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getAllPlans() {
        try {
            var plans = subscriptionPlanService.getAllPlans();
            return responseUtils.success(plans);
        } catch (Exception e) {
            log.error("Unexpected error when getting all subscription plans: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getAllActivePlans() {
        try {
            var plans = subscriptionPlanService.getAllActivePlans();
            return responseUtils.success(plans);
        } catch (Exception e) {
            log.error("Unexpected error when getting all active subscription plans: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getCustomPlanByOrganizationId(Long organizationId) {
        try {
            var plan = subscriptionPlanService.getCustomPlanByOrganizationId(organizationId);
            return responseUtils.success(plan);
        } catch (Exception e) {
            log.error("Unexpected error when getting custom plan for organization {}: {}", organizationId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getStandardPlans() {
        try {
            var plans = subscriptionPlanService.getStandardPlans();
            return responseUtils.success(plans);
        } catch (Exception e) {
            log.error("Unexpected error when getting standard subscription plans: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> calculatePlanPrice(Long planId, String billingCycle) {
        try {
            var plan = subscriptionPlanService.getPlanById(planId);
            var price = plan.getPriceByBillingCycle(billingCycle);
            return responseUtils.success(price);
        } catch (AppException e) {
            log.error("Error calculating plan price for {}: {}", planId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when calculating plan price for {}: {}", planId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> calculateYearlySavings(Long planId) {
        try {
            var plan = subscriptionPlanService.getPlanById(planId);
            var savings = plan.getYearlySavings();
            return responseUtils.success(savings);
        } catch (AppException e) {
            log.error("Error calculating yearly savings for plan {}: {}", planId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when calculating yearly savings for plan {}: {}", planId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> addModuleToPlan(Long planId, AddModuleToPlanRequest request, Long createdBy) {
        try {
            log.info("[UseCase] Adding module {} to plan {}", request.getModuleId(), planId);

            var module = moduleService.getModuleByIdFromCache(request.getModuleId());
            if (module == null || !module.isAvailable()) {
                return responseUtils.badRequest(Constants.ErrorMessage.MODULE_NOT_FOUND);
            }

            var planModule = subscriptionPlanService.addModuleToPlan(
                    planId,
                    request.getModuleId(),
                    request.getLicenseType().name(),
                    request.getIsIncluded(),
                    request.getMaxUsersPerModule(),
                    createdBy);

            addModuleAccessForExistedSubscriptions(planId, request.getModuleId());

            log.info("[UseCase] Successfully added module {} to plan {}", request.getModuleId(), planId);
            return responseUtils.success(planModule);
        } catch (AppException e) {
            log.error("Error adding module to plan {}: {}", planId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when adding module to plan {}: {}", planId, e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> removeModuleFromPlan(Long planId, Long moduleId) {
        try {
            log.info("[UseCase] Removing module {} from plan {}", moduleId, planId);

            subscriptionPlanService.removeModuleFromPlan(planId, moduleId);

            removeModuleAccessFromExistedSubscriptions(planId, moduleId);

            log.info("[UseCase] Successfully removed module {} from plan {}", moduleId, planId);
            return responseUtils.success("Module removed from plan successfully");
        } catch (AppException e) {
            log.error("Error removing module from plan {}: {}", planId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when removing module from plan {}: {}", planId, e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> getPlanModules(Long planId) {
        try {
            var modules = subscriptionPlanService.getPlanModules(planId);
            return responseUtils.success(modules);
        } catch (Exception e) {
            log.error("Unexpected error when getting modules for plan {}: {}", planId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> isModuleInPlan(Long planId, Long moduleId) {
        try {
            var exists = subscriptionPlanService.isModuleInPlan(planId, moduleId);
            return responseUtils.success(exists);
        } catch (Exception e) {
            log.error("Unexpected error when checking module in plan: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addModuleAccessForExistedSubscriptions(Long planId, Long moduleId) {
        asyncTaskExecutor.execute(() -> {
            var subscriptions = subscriptionService.getSubscriptionsByPlanId(planId).stream()
                    .filter(sub -> sub.isActive())
                    .toList();
            if (CollectionUtils.isEmpty(subscriptions)) {
                return;
            }
            List<RoleEntity> rolesInModule = roleService.getRolesByModuleId(moduleId);
            if (CollectionUtils.isEmpty(rolesInModule)) {
                log.error("No roles found in module {} when adding module access for existing subscriptions", moduleId);
                return;
            }

            subscriptions.forEach(subscription -> {
                try {
                    var organization = organizationService.getOrganizationById(subscription.getOrganizationId());
                    if (organization == null) {
                        log.error("Organization with id {} not found. Skipping adding module access",
                                subscription.getOrganizationId());
                        return;
                    }

                    var usersInOrganization = userService.getUsersByOrganizationId(subscription.getOrganizationId());
                    usersInOrganization.forEach(user -> {
                        if (!user.isActive()) {
                            return;
                        }
                        userModuleAccessService.registerUserToModuleWithExpiration(
                                user.getId(),
                                moduleId,
                                organization.getId(),
                                organization.getOwnerId(),
                                subscription.getEndDate());
                        combineRoleService.assignRolesToUser(user, rolesInModule);
                    });
                } catch (Exception e) {
                    log.error("Error adding module access for subscription {}: {}", subscription.getId(),
                            e.getMessage());
                }
            });

            log.info("Completed adding module access for existing subscriptions of plan {}", planId);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeModuleAccessFromExistedSubscriptions(Long planId, Long moduleId) {
        asyncTaskExecutor.execute(() -> {
            var subscriptions = subscriptionService.getSubscriptionsByPlanId(planId).stream()
                    .filter(sub -> sub.isActive())
                    .toList();
            if (CollectionUtils.isEmpty(subscriptions)) {
                return;
            }
            List<RoleEntity> rolesInModule = roleService.getRolesByModuleId(moduleId);
            if (CollectionUtils.isEmpty(rolesInModule)) {
                log.error("No roles found in module {} when removing module access for existing subscriptions",
                        moduleId);
                return;
            }

            subscriptions.forEach(subscription -> {
                try {
                    var organization = organizationService.getOrganizationById(subscription.getOrganizationId());
                    if (organization == null) {
                        log.error("Organization with id {} not found. Skipping removing module access",
                                subscription.getOrganizationId());
                        return;
                    }
                    var usersInOrganization = userService.getUsersByOrganizationId(subscription.getOrganizationId());
                    usersInOrganization.forEach(user -> {
                        userModuleAccessService.revokeUserModuleAccess(user.getId(), moduleId, organization.getId());
                        combineRoleService.removeRolesFromUser(user, rolesInModule);
                    });
                } catch (Exception e) {
                    log.error("Error removing module access for subscription {}: {}", subscription.getId(),
                            e.getMessage());
                }
            });

            log.info("Completed removing module access for existing subscriptions of plan {}", planId);
        });
    }
}
