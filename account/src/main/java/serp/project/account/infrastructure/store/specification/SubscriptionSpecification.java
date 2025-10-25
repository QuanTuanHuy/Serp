/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.specification;

import org.springframework.data.jpa.domain.Specification;

import serp.project.account.core.domain.dto.request.GetSubscriptionParams;
import serp.project.account.infrastructure.store.model.OrganizationSubscriptionModel;

public class SubscriptionSpecification extends BaseSpecification<OrganizationSubscriptionModel> {
    public static Specification<OrganizationSubscriptionModel> hasOrganizationId(Long organizationId) {
        return equal("organizationId", organizationId);
    }

    public static Specification<OrganizationSubscriptionModel> hasStatus(String status) {
        return equal("status", status);
    }

    public static Specification<OrganizationSubscriptionModel> hasBillingCycle(String billingCycle) {
        return equal("billingCycle", billingCycle);
    }

    public static Specification<OrganizationSubscriptionModel> getAllSubscriptions(GetSubscriptionParams params) {
        if (params.getOrganizationId() == null && params.getStatus() == null && params.getBillingCycle() == null) {
            return alwaysTrue();
        }
        return hasOrganizationId(params.getOrganizationId())
                .and(hasStatus(params.getStatus()))
                .and(hasBillingCycle(params.getBillingCycle()));
    }
}
