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
import serp.project.account.core.domain.dto.request.AddModuleToPlanRequest;
import serp.project.account.core.domain.dto.request.CreateSubscriptionPlanRequest;
import serp.project.account.core.domain.dto.request.UpdateSubscriptionPlanRequest;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanUseCase {

    private final ISubscriptionPlanService subscriptionPlanService;
    private final ResponseUtils responseUtils;

    /**
     * Create a new subscription plan
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createPlan(CreateSubscriptionPlanRequest request, Long createdBy) {
        try {
            log.info("[UseCase] Creating subscription plan: {}", request.getPlanCode());

            var plan = subscriptionPlanService.createPlan(request, createdBy);

            // TODO: Send Kafka event - plan created
            // kafkaProducer.sendPlanCreatedEvent(plan);

            log.info("[UseCase] Successfully created subscription plan with ID: {}", plan.getId());
            return responseUtils.success(plan);
        } catch (AppException e) {
            log.error("Error creating subscription plan: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when creating subscription plan: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Update an existing subscription plan
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updatePlan(Long planId, UpdateSubscriptionPlanRequest request, Long updatedBy) {
        try {
            log.info("[UseCase] Updating subscription plan: {}", planId);

            var plan = subscriptionPlanService.updatePlan(planId, request, updatedBy);

            // TODO: Send Kafka event - plan updated
            // kafkaProducer.sendPlanUpdatedEvent(plan);

            log.info("[UseCase] Successfully updated subscription plan: {}", planId);
            return responseUtils.success(plan);
        } catch (AppException e) {
            log.error("Error updating subscription plan {}: {}", planId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when updating subscription plan {}: {}", planId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Delete (deactivate) a subscription plan
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> deletePlan(Long planId) {
        try {
            log.info("[UseCase] Deleting subscription plan: {}", planId);

            subscriptionPlanService.deletePlan(planId);

            // TODO: Send Kafka event - plan deleted
            // kafkaProducer.sendPlanDeletedEvent(planId);

            log.info("[UseCase] Successfully deleted subscription plan: {}", planId);
            return responseUtils.success("Subscription plan deleted successfully");
        } catch (AppException e) {
            log.error("Error deleting subscription plan {}: {}", planId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when deleting subscription plan {}: {}", planId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get plan by ID
     */
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

    /**
     * Get plan by code
     */
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

    /**
     * Get all plans
     */
    public GeneralResponse<?> getAllPlans() {
        try {
            var plans = subscriptionPlanService.getAllPlans();
            return responseUtils.success(plans);
        } catch (Exception e) {
            log.error("Unexpected error when getting all subscription plans: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get all active plans
     */
    public GeneralResponse<?> getAllActivePlans() {
        try {
            var plans = subscriptionPlanService.getAllActivePlans();
            return responseUtils.success(plans);
        } catch (Exception e) {
            log.error("Unexpected error when getting all active subscription plans: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get custom plan for organization
     */
    public GeneralResponse<?> getCustomPlanByOrganizationId(Long organizationId) {
        try {
            var plan = subscriptionPlanService.getCustomPlanByOrganizationId(organizationId);
            return responseUtils.success(plan);
        } catch (Exception e) {
            log.error("Unexpected error when getting custom plan for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get standard plans (non-custom)
     */
    public GeneralResponse<?> getStandardPlans() {
        try {
            var plans = subscriptionPlanService.getStandardPlans();
            return responseUtils.success(plans);
        } catch (Exception e) {
            log.error("Unexpected error when getting standard subscription plans: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Calculate plan price based on billing cycle
     */
    public GeneralResponse<?> calculatePlanPrice(Long planId, String billingCycle) {
        try {
            var plan = subscriptionPlanService.getPlanById(planId);
            var price = subscriptionPlanService.calculatePlanPrice(plan, billingCycle);
            return responseUtils.success(price);
        } catch (AppException e) {
            log.error("Error calculating plan price for {}: {}", planId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when calculating plan price for {}: {}", planId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Calculate yearly savings for a plan
     */
    public GeneralResponse<?> calculateYearlySavings(Long planId) {
        try {
            var plan = subscriptionPlanService.getPlanById(planId);
            var savings = subscriptionPlanService.calculateYearlySavings(plan);
            return responseUtils.success(savings);
        } catch (AppException e) {
            log.error("Error calculating yearly savings for plan {}: {}", planId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when calculating yearly savings for plan {}: {}", planId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Add module to plan
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> addModuleToPlan(Long planId, AddModuleToPlanRequest request, Long createdBy) {
        try {
            log.info("[UseCase] Adding module {} to plan {}", request.getModuleId(), planId);

            var planModule = subscriptionPlanService.addModuleToPlan(
                    planId,
                    request.getModuleId(),
                    request.getLicenseType().name(),
                    request.getIsIncluded(),
                    request.getMaxUsersPerModule(),
                    createdBy
            );

            // TODO: Send Kafka event - module added to plan
            // kafkaProducer.sendModuleAddedToPlanEvent(planModule);

            log.info("[UseCase] Successfully added module {} to plan {}", request.getModuleId(), planId);
            return responseUtils.success(planModule);
        } catch (AppException e) {
            log.error("Error adding module to plan {}: {}", planId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when adding module to plan {}: {}", planId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Remove module from plan
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> removeModuleFromPlan(Long planId, Long moduleId) {
        try {
            log.info("[UseCase] Removing module {} from plan {}", moduleId, planId);

            subscriptionPlanService.removeModuleFromPlan(planId, moduleId);

            // TODO: Send Kafka event - module removed from plan
            // kafkaProducer.sendModuleRemovedFromPlanEvent(planId, moduleId);

            log.info("[UseCase] Successfully removed module {} from plan {}", moduleId, planId);
            return responseUtils.success("Module removed from plan successfully");
        } catch (AppException e) {
            log.error("Error removing module from plan {}: {}", planId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when removing module from plan {}: {}", planId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get modules for a plan
     */
    public GeneralResponse<?> getPlanModules(Long planId) {
        try {
            var modules = subscriptionPlanService.getPlanModules(planId);
            return responseUtils.success(modules);
        } catch (Exception e) {
            log.error("Unexpected error when getting modules for plan {}: {}", planId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Check if module is in plan
     */
    public GeneralResponse<?> isModuleInPlan(Long planId, Long moduleId) {
        try {
            var exists = subscriptionPlanService.isModuleInPlan(planId, moduleId);
            return responseUtils.success(exists);
        } catch (Exception e) {
            log.error("Unexpected error when checking module in plan: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }
}
