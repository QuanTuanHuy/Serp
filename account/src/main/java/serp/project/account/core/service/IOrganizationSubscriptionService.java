/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import serp.project.account.core.domain.dto.request.SubscribeRequest;
import serp.project.account.core.domain.dto.request.UpgradeSubscriptionRequest;
import serp.project.account.core.domain.dto.request.DowngradeSubscriptionRequest;
import serp.project.account.core.domain.dto.request.GetSubscriptionParams;
import serp.project.account.core.domain.dto.request.CancelSubscriptionRequest;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanEntity;
import serp.project.account.core.domain.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.util.Pair;

public interface IOrganizationSubscriptionService {

        Pair<List<OrganizationSubscriptionEntity>, Long> getAllSubscriptions(GetSubscriptionParams params);

        /**
         * Subscribe organization to a plan (creates PENDING_APPROVAL subscription for
         * paid plans, ACTIVE for FREE)
         */
        OrganizationSubscriptionEntity subscribe(Long organizationId, SubscribeRequest request, Long requestedBy,
                        SubscriptionPlanEntity plan);

        /**
         * Start trial period for organization
         */
        OrganizationSubscriptionEntity startTrial(Long organizationId, SubscriptionPlanEntity plan, Long requestedBy);

        /**
         * Upgrade organization subscription to higher plan
         */
        OrganizationSubscriptionEntity upgradeSubscription(
                        Long organizationId,
                        UpgradeSubscriptionRequest request,
                        Long requestedBy,
                        SubscriptionPlanEntity currentPlan,
                        SubscriptionPlanEntity newPlan);

        /**
         * Downgrade organization subscription to lower plan
         */
        OrganizationSubscriptionEntity downgradeSubscription(
                        Long organizationId,
                        DowngradeSubscriptionRequest request,
                        Long requestedBy,
                        SubscriptionPlanEntity currentPlan,
                        SubscriptionPlanEntity newPlan);

        /**
         * Cancel organization subscription
         */
        void cancelSubscription(Long organizationId, CancelSubscriptionRequest request, Long cancelledBy);

        /**
         * Renew organization subscription
         */
        OrganizationSubscriptionEntity renewSubscription(Long organizationId, Long currentSubscriptionId,
                        Long renewedBy);

        /**
         * Activate subscription (admin approval)
         */
        OrganizationSubscriptionEntity activateSubscription(Long subscriptionId, Long activatedBy);

        /**
         * Reject subscription (admin rejection)
         */
        void rejectSubscription(Long subscriptionId, String reason, Long rejectedBy);

        /**
         * Extend trial period
         */
        OrganizationSubscriptionEntity extendTrial(Long subscriptionId, int additionalDays, Long extendedBy);

        /**
         * Expire subscription (background job)
         */
        void expireSubscription(Long subscriptionId);

        /**
         * Get active subscription for organization
         */
        OrganizationSubscriptionEntity getActiveSubscription(Long organizationId);

        /**
         * Get subscription by ID
         */
        OrganizationSubscriptionEntity getSubscriptionById(Long subscriptionId);

        /**
         * Get subscription history for organization
         */
        List<OrganizationSubscriptionEntity> getSubscriptionHistory(Long organizationId);

        /**
         * Get subscriptions by status
         */
        List<OrganizationSubscriptionEntity> getSubscriptionsByStatus(SubscriptionStatus status);

        /**
         * Get subscriptions expiring before timestamp
         */
        List<OrganizationSubscriptionEntity> getExpiringSubscriptions(Long beforeTimestamp);

        /**
         * Get trial subscriptions ending before timestamp
         */
        List<OrganizationSubscriptionEntity> getTrialEndingSubscriptions(Long beforeTimestamp);

        /**
         * Check if organization has active subscription
         */
        boolean hasActiveSubscription(Long organizationId);

        /**
         * Check if organization can access module based on subscription
         */
        boolean canAccessModule(Long organizationId, Long moduleId);

        /**
         * Calculate proration amount for upgrade/downgrade
         */
        BigDecimal calculateProration(OrganizationSubscriptionEntity currentSubscription,
                        Long newPlanId, String newBillingCycle);

        /**
         * Get remaining days of subscription
         */
        int getRemainingDays(Long subscriptionId);

        /**
         * Validate subscription change (upgrade/downgrade)
         */
        void validateSubscriptionChange(Long organizationId, Long newPlanId);

        /**
         * Validate subscription change
         */
        void validateSubscriptionChange(SubscriptionPlanEntity currentPlan, SubscriptionPlanEntity newPlan);

        /**
         * Validate subscription upgrade
         */
        void validateUpgrade(SubscriptionPlanEntity currentPlan, SubscriptionPlanEntity newPlan);

        /**
         * Validate subscription cancellation
         */
        void validateCancellation(Long organizationId);
}
