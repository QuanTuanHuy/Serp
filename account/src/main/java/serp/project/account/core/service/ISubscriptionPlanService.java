/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import serp.project.account.core.domain.dto.request.CreateSubscriptionPlanRequest;
import serp.project.account.core.domain.dto.request.UpdateSubscriptionPlanRequest;
import serp.project.account.core.domain.entity.SubscriptionPlanEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;

import java.math.BigDecimal;
import java.util.List;

public interface ISubscriptionPlanService {

    /**
     * Create a new subscription plan
     */
    SubscriptionPlanEntity createPlan(CreateSubscriptionPlanRequest request, Long createdBy);

    /**
     * Update an existing subscription plan
     */
    SubscriptionPlanEntity updatePlan(Long planId, UpdateSubscriptionPlanRequest request, Long updatedBy);

    /**
     * Delete a subscription plan (soft delete by setting isActive=false)
     */
    void deletePlan(Long planId);

    /**
     * Get subscription plan by ID
     */
    SubscriptionPlanEntity getPlanById(Long planId);

    /**
     * Get subscription plan by plan code
     */
    SubscriptionPlanEntity getPlanByCode(String planCode);

    /**
     * Get all subscription plans
     */
    List<SubscriptionPlanEntity> getAllPlans();

    /**
     * Get all active subscription plans
     */
    List<SubscriptionPlanEntity> getAllActivePlans();

    /**
     * Get custom plan for organization
     */
    SubscriptionPlanEntity getCustomPlanByOrganizationId(Long organizationId);

    /**
     * Get standard plans (not custom)
     */
    List<SubscriptionPlanEntity> getStandardPlans();

    /**
     * Validate if plan can be deleted
     */
    void validatePlanDeletion(Long planId);

    /**
     * Calculate total price for plan with billing cycle
     * Returns monthly or yearly price based on billing cycle
     */
    BigDecimal calculatePlanPrice(SubscriptionPlanEntity plan, String billingCycle);

    /**
     * Calculate yearly savings amount
     */
    BigDecimal calculateYearlySavings(SubscriptionPlanEntity plan);

    /**
     * Add module to subscription plan
     */
    SubscriptionPlanModuleEntity addModuleToPlan(Long planId, Long moduleId, 
                                                  String licenseType, Boolean isIncluded, 
                                                  Integer maxUsersPerModule, Long createdBy);

    /**
     * Remove module from subscription plan
     */
    void removeModuleFromPlan(Long planId, Long moduleId);

    /**
     * Get all modules for a subscription plan
     */
    List<SubscriptionPlanModuleEntity> getPlanModules(Long planId);

    /**
     * Check if module exists in plan
     */
    boolean isModuleInPlan(Long planId, Long moduleId);

    /**
     * Validate plan code uniqueness
     */
    void validatePlanCodeUniqueness(String planCode, Long excludePlanId);
}
