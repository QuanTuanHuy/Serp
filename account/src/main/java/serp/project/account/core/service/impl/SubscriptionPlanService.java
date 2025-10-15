/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateSubscriptionPlanRequest;
import serp.project.account.core.domain.dto.request.UpdateSubscriptionPlanRequest;
import serp.project.account.core.domain.entity.SubscriptionPlanEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;
import serp.project.account.core.domain.enums.BillingCycle;
import serp.project.account.core.domain.enums.LicenseType;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.ISubscriptionPlanPort;
import serp.project.account.core.port.store.ISubscriptionPlanModulePort;
import serp.project.account.core.service.ISubscriptionPlanService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanService implements ISubscriptionPlanService {

    private final ISubscriptionPlanPort subscriptionPlanPort;
    private final ISubscriptionPlanModulePort subscriptionPlanModulePort;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionPlanEntity createPlan(CreateSubscriptionPlanRequest request, Long createdBy) {
        log.info("Creating subscription plan: {}", request.getPlanCode());

        // Validate plan code uniqueness
        validatePlanCodeUniqueness(request.getPlanCode(), null);

        var plan = SubscriptionPlanEntity.builder()
                .planName(request.getPlanName())
                .planCode(request.getPlanCode())
                .description(request.getDescription())
                .monthlyPrice(request.getMonthlyPrice())
                .yearlyPrice(request.getYearlyPrice())
                .maxUsers(request.getMaxUsers())
                .trialDays(request.getTrialDays() != null ? request.getTrialDays() : 0)
                .isActive(true)
                .isCustom(request.getIsCustom() != null ? request.getIsCustom() : false)
                .organizationId(request.getOrganizationId())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 999)
                .createdBy(createdBy)
                .createdAt(Instant.now().toEpochMilli())
                .build();

        var savedPlan = subscriptionPlanPort.save(plan);
        log.info("Successfully created subscription plan with ID: {}", savedPlan.getId());

        return savedPlan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionPlanEntity updatePlan(Long planId, UpdateSubscriptionPlanRequest request, Long updatedBy) {
        log.info("Updating subscription plan: {}", planId);

        var plan = getPlanById(planId);

        // Update fields if provided
        if (request.getPlanName() != null) {
            plan.setPlanName(request.getPlanName());
        }
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        if (request.getMonthlyPrice() != null) {
            plan.setMonthlyPrice(request.getMonthlyPrice());
        }
        if (request.getYearlyPrice() != null) {
            plan.setYearlyPrice(request.getYearlyPrice());
        }
        if (request.getMaxUsers() != null) {
            plan.setMaxUsers(request.getMaxUsers());
        }
        if (request.getTrialDays() != null) {
            plan.setTrialDays(request.getTrialDays());
        }
        if (request.getIsActive() != null) {
            plan.setIsActive(request.getIsActive());
        }
        if (request.getDisplayOrder() != null) {
            plan.setDisplayOrder(request.getDisplayOrder());
        }

        plan.setUpdatedBy(updatedBy);
        plan.setUpdatedAt(Instant.now().toEpochMilli());

        var updatedPlan = subscriptionPlanPort.update(plan);
        log.info("Successfully updated subscription plan: {}", planId);

        return updatedPlan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long planId) {
        log.info("Deleting subscription plan: {}", planId);

        validatePlanDeletion(planId);

        var plan = getPlanById(planId);
        plan.setIsActive(false);
        plan.setUpdatedAt(Instant.now().toEpochMilli());

        subscriptionPlanPort.update(plan);
        log.info("Successfully deleted (deactivated) subscription plan: {}", planId);
    }

    @Override
    public SubscriptionPlanEntity getPlanById(Long planId) {
        return subscriptionPlanPort.getById(planId)
                .orElseThrow(() -> {
                    log.error("Subscription plan not found with ID: {}", planId);
                    return new AppException(Constants.ErrorMessage.SUBSCRIPTION_PLAN_NOT_FOUND);
                });
    }

    @Override
    public SubscriptionPlanEntity getPlanByCode(String planCode) {
        return subscriptionPlanPort.getByPlanCode(planCode)
                .orElseThrow(() -> {
                    log.error("Subscription plan not found with code: {}", planCode);
                    return new AppException(Constants.ErrorMessage.SUBSCRIPTION_PLAN_NOT_FOUND);
                });
    }

    @Override
    public List<SubscriptionPlanEntity> getAllPlans() {
        return subscriptionPlanPort.getAll();
    }

    @Override
    public List<SubscriptionPlanEntity> getAllActivePlans() {
        return subscriptionPlanPort.getAllActive();
    }

    @Override
    public SubscriptionPlanEntity getCustomPlanByOrganizationId(Long organizationId) {
        return subscriptionPlanPort.getCustomPlanByOrganizationId(organizationId)
                .orElse(null);
    }

    @Override
    public List<SubscriptionPlanEntity> getStandardPlans() {
        return subscriptionPlanPort.getByIsCustom(false);
    }

    @Override
    public void validatePlanDeletion(Long planId) {
        // TODO: Check if any active subscriptions are using this plan
        // For now, just check if plan exists
        getPlanById(planId);
    }

    @Override
    public BigDecimal calculatePlanPrice(SubscriptionPlanEntity plan, String billingCycle) {
        if (billingCycle == null) {
            return plan.getMonthlyPrice();
        }

        return BillingCycle.YEARLY.name().equals(billingCycle) 
                ? plan.getYearlyPrice() 
                : plan.getMonthlyPrice();
    }

    @Override
    public BigDecimal calculateYearlySavings(SubscriptionPlanEntity plan) {
        if (plan.getMonthlyPrice() == null || plan.getYearlyPrice() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyTotal = plan.getMonthlyPrice().multiply(BigDecimal.valueOf(12));
        BigDecimal savings = monthlyTotal.subtract(plan.getYearlyPrice());

        return savings.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionPlanModuleEntity addModuleToPlan(Long planId, Long moduleId, 
                                                         String licenseType, Boolean isIncluded,
                                                         Integer maxUsersPerModule, Long createdBy) {
        log.info("Adding module {} to plan {}", moduleId, planId);

        // Validate plan exists
        getPlanById(planId);

        // Check if module already in plan
        if (isModuleInPlan(planId, moduleId)) {
            log.error("Module {} already exists in plan {}", moduleId, planId);
            throw new AppException(Constants.ErrorMessage.MODULE_ALREADY_IN_PLAN);
        }

        var planModule = SubscriptionPlanModuleEntity.builder()
                .subscriptionPlanId(planId)
                .moduleId(moduleId)
                .licenseType(LicenseType.valueOf(licenseType))
                .isIncluded(isIncluded != null ? isIncluded : false)
                .maxUsersPerModule(maxUsersPerModule)
                .createdBy(createdBy)
                .createdAt(Instant.now().toEpochMilli())
                .build();

        var savedPlanModule = subscriptionPlanModulePort.save(planModule);
        log.info("Successfully added module {} to plan {}", moduleId, planId);

        return savedPlanModule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeModuleFromPlan(Long planId, Long moduleId) {
        log.info("Removing module {} from plan {}", moduleId, planId);

        if (!isModuleInPlan(planId, moduleId)) {
            log.error("Module {} not found in plan {}", moduleId, planId);
            throw new AppException(Constants.ErrorMessage.MODULE_NOT_IN_PLAN);
        }

        subscriptionPlanModulePort.deleteByPlanIdAndModuleId(planId, moduleId);
        log.info("Successfully removed module {} from plan {}", moduleId, planId);
    }

    @Override
    public List<SubscriptionPlanModuleEntity> getPlanModules(Long planId) {
        return subscriptionPlanModulePort.getBySubscriptionPlanId(planId);
    }

    @Override
    public boolean isModuleInPlan(Long planId, Long moduleId) {
        return subscriptionPlanModulePort.existsByPlanIdAndModuleId(planId, moduleId);
    }

    @Override
    public void validatePlanCodeUniqueness(String planCode, Long excludePlanId) {
        var existingPlan = subscriptionPlanPort.getByPlanCode(planCode);

        if (existingPlan.isPresent() && 
            (excludePlanId == null || !existingPlan.get().getId().equals(excludePlanId))) {
            log.error("Subscription plan with code {} already exists", planCode);
            throw new AppException(Constants.ErrorMessage.SUBSCRIPTION_PLAN_CODE_ALREADY_EXISTS);
        }
    }
}
