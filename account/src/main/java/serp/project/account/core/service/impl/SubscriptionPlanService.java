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
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.ISubscriptionPlanPort;
import serp.project.account.core.port.store.ISubscriptionPlanModulePort;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.infrastructure.store.mapper.SubscriptionPlanMapper;
import serp.project.account.infrastructure.store.mapper.SubscriptionPlanModuleMapper;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanService implements ISubscriptionPlanService {

    private final ISubscriptionPlanPort subscriptionPlanPort;
    private final ISubscriptionPlanModulePort subscriptionPlanModulePort;

    private final SubscriptionPlanMapper subscriptionPlanMapper;
    private final SubscriptionPlanModuleMapper subscriptionPlanModuleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionPlanEntity createPlan(CreateSubscriptionPlanRequest request, Long createdBy) {
        validatePlanCodeUniqueness(request.getPlanCode(), null);

        var plan = subscriptionPlanMapper.buildNewPlan(request, createdBy);

        var savedPlan = subscriptionPlanPort.save(plan);
        log.info("Successfully created subscription plan with ID: {}", savedPlan.getId());

        return savedPlan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionPlanEntity updatePlan(Long planId, UpdateSubscriptionPlanRequest request, Long updatedBy) {
        var plan = getPlanById(planId);
        plan = subscriptionPlanMapper.buildUpdatedPlan(plan, request, updatedBy);

        // Implement later: Update modules if provided in request

        var updatedPlan = subscriptionPlanPort.update(plan);
        log.info("Successfully updated subscription plan: {}", planId);

        return updatedPlan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long planId) {
        var plan = validatePlanDeletion(planId);
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
    public SubscriptionPlanEntity validatePlanDeletion(Long planId) {
        // Implement later: Check if any active subscriptions are using this plan
        return getPlanById(planId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionPlanModuleEntity addModuleToPlan(Long planId, Long moduleId,
            String licenseType, Boolean isIncluded,
            Integer maxUsersPerModule, Long createdBy) {
        getPlanById(planId);

        if (isModuleInPlan(planId, moduleId)) {
            log.error("Module {} already exists in plan {}", moduleId, planId);
            throw new AppException(Constants.ErrorMessage.MODULE_ALREADY_IN_PLAN);
        }

        var planModule = subscriptionPlanModuleMapper.buildNewPlanModule(
                planId, moduleId, licenseType, isIncluded, maxUsersPerModule, createdBy);

        var savedPlanModule = subscriptionPlanModulePort.save(planModule);

        return savedPlanModule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeModuleFromPlan(Long planId, Long moduleId) {

        if (!isModuleInPlan(planId, moduleId)) {
            log.error("Module {} not found in plan {}", moduleId, planId);
            throw new AppException(Constants.ErrorMessage.MODULE_NOT_IN_PLAN);
        }

        subscriptionPlanModulePort.deleteByPlanIdAndModuleId(planId, moduleId);
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
