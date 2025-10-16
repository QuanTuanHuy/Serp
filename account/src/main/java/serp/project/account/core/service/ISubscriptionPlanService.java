/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import serp.project.account.core.domain.dto.request.CreateSubscriptionPlanRequest;
import serp.project.account.core.domain.dto.request.UpdateSubscriptionPlanRequest;
import serp.project.account.core.domain.entity.SubscriptionPlanEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;

import java.util.List;

public interface ISubscriptionPlanService {

    SubscriptionPlanEntity createPlan(CreateSubscriptionPlanRequest request, Long createdBy);

    SubscriptionPlanEntity updatePlan(Long planId, UpdateSubscriptionPlanRequest request, Long updatedBy);

    void deletePlan(Long planId);

    SubscriptionPlanEntity getPlanById(Long planId);

    SubscriptionPlanEntity getPlanByCode(String planCode);

    List<SubscriptionPlanEntity> getAllPlans();

    List<SubscriptionPlanEntity> getAllActivePlans();

    SubscriptionPlanEntity getCustomPlanByOrganizationId(Long organizationId);

    List<SubscriptionPlanEntity> getStandardPlans();

    SubscriptionPlanEntity validatePlanDeletion(Long planId);

    SubscriptionPlanModuleEntity addModuleToPlan(Long planId, Long moduleId,
            String licenseType, Boolean isIncluded,
            Integer maxUsersPerModule, Long createdBy);

    void removeModuleFromPlan(Long planId, Long moduleId);

    List<SubscriptionPlanModuleEntity> getPlanModules(Long planId);

    boolean isModuleInPlan(Long planId, Long moduleId);

    void validatePlanCodeUniqueness(String planCode, Long excludePlanId);
}
