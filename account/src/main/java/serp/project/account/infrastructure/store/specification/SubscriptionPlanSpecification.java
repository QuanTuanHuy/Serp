/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.specification;

import org.springframework.data.jpa.domain.Specification;

import serp.project.account.core.domain.dto.request.GetSubscriptionPlanParams;
import serp.project.account.infrastructure.store.model.SubscriptionPlanModel;

public class SubscriptionPlanSpecification extends BaseSpecification<SubscriptionPlanModel> {
    public static Specification<SubscriptionPlanModel> isCustomPlan(Boolean isCustom) {
        return equal("isCustom", isCustom);
    }

    public static Specification<SubscriptionPlanModel> organizationIdEquals(Long organizationId) {
        return equal("organizationId", organizationId);
    }

    public static Specification<SubscriptionPlanModel> getAllPlans(GetSubscriptionPlanParams params) {
        if (params.getIsCustom() == null && params.getOrganizationId() == null) {
            return alwaysTrue();
        }
        return isCustomPlan(params.getIsCustom())
                .and(organizationIdEquals(params.getOrganizationId()));
    }
}
