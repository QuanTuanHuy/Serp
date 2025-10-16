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
import serp.project.account.core.domain.dto.request.*;
import serp.project.account.core.domain.enums.SubscriptionStatus;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IOrganizationSubscriptionService;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationSubscriptionUseCase {

    private final IOrganizationSubscriptionService organizationSubscriptionService;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> subscribe(Long organizationId, SubscribeRequest request, Long requestedBy) {
        try {
            log.info("[UseCase] Organization {} subscribing to plan {}", organizationId, request.getPlanId());

            var subscription = organizationSubscriptionService.subscribe(organizationId, request, requestedBy);

            // TODO Send notification to organization admin

            log.info("[UseCase] Organization {} successfully subscribed to plan {}", organizationId,
                    request.getPlanId());
            return responseUtils.success(subscription);
        } catch (AppException e) {
            log.error("Error subscribing organization {}: {}", organizationId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when subscribing organization {}: {}", organizationId, e.getMessage());
            throw e;
        }
    }

    /**
     * Start trial for organization
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> startTrial(Long organizationId, Long planId, Long requestedBy) {
        try {
            log.info("[UseCase] Organization {} starting trial for plan {}", organizationId, planId);

            var subscription = organizationSubscriptionService.startTrial(organizationId, planId, requestedBy);

            // TODO: Send Kafka event - trial started
            // kafkaProducer.sendTrialStartedEvent(subscription);

            // TODO: Send notification
            // notificationService.sendTrialStartedNotification(organizationId,
            // subscription);

            log.info("[UseCase] Organization {} successfully started trial", organizationId);
            return responseUtils.success(subscription);
        } catch (AppException e) {
            log.error("Error starting trial for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when starting trial for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Upgrade subscription to higher plan
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> upgradeSubscription(Long organizationId, UpgradeSubscriptionRequest request,
            Long requestedBy) {
        try {
            log.info("[UseCase] Organization {} upgrading subscription to plan {}", organizationId,
                    request.getNewPlanId());

            var newSubscription = organizationSubscriptionService.upgradeSubscription(organizationId, request,
                    requestedBy);

            // TODO: Process payment with proration
            // paymentService.processUpgradePayment(organizationId, newSubscription,
            // prorationAmount);

            // TODO: Send Kafka event - subscription upgraded
            // kafkaProducer.sendSubscriptionUpgradedEvent(newSubscription);

            // TODO: Send notification
            // notificationService.sendUpgradeConfirmation(organizationId, newSubscription);

            log.info("[UseCase] Organization {} successfully upgraded to plan {}", organizationId,
                    request.getNewPlanId());
            return responseUtils.success(newSubscription);
        } catch (AppException e) {
            log.error("Error upgrading subscription for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when upgrading subscription for organization {}: {}", organizationId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Downgrade subscription to lower plan
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> downgradeSubscription(Long organizationId, DowngradeSubscriptionRequest request,
            Long requestedBy) {
        try {
            log.info("[UseCase] Organization {} downgrading subscription to plan {}", organizationId,
                    request.getNewPlanId());

            var newSubscription = organizationSubscriptionService.downgradeSubscription(organizationId, request,
                    requestedBy);

            // TODO: Send Kafka event - subscription downgraded (scheduled)
            // kafkaProducer.sendSubscriptionDowngradedEvent(newSubscription);

            // TODO: Send notification
            // notificationService.sendDowngradeScheduledNotification(organizationId,
            // newSubscription);

            log.info("[UseCase] Organization {} scheduled downgrade to plan {}", organizationId,
                    request.getNewPlanId());
            return responseUtils.success(newSubscription);
        } catch (AppException e) {
            log.error("Error downgrading subscription for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when downgrading subscription for organization {}: {}", organizationId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Cancel subscription
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> cancelSubscription(Long organizationId, CancelSubscriptionRequest request,
            Long cancelledBy) {
        try {
            log.info("[UseCase] Organization {} cancelling subscription", organizationId);

            organizationSubscriptionService.cancelSubscription(organizationId, request, cancelledBy);

            // TODO: Send Kafka event - subscription cancelled
            // kafkaProducer.sendSubscriptionCancelledEvent(organizationId,
            // request.getReason());

            // TODO: Send notification
            // notificationService.sendCancellationConfirmation(organizationId);

            // TODO: Schedule data retention/cleanup
            // dataRetentionService.scheduleDataCleanup(organizationId);

            log.info("[UseCase] Organization {} successfully cancelled subscription", organizationId);
            return responseUtils.success("Subscription cancelled successfully");
        } catch (AppException e) {
            log.error("Error cancelling subscription for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when cancelling subscription for organization {}: {}", organizationId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Renew expired subscription
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> renewSubscription(Long organizationId, Long renewedBy) {
        try {
            log.info("[UseCase] Organization {} renewing subscription", organizationId);

            var newSubscription = organizationSubscriptionService.renewSubscription(organizationId, renewedBy);

            // TODO: Process payment
            // paymentService.processRenewalPayment(organizationId, newSubscription);

            // TODO: Send Kafka event - subscription renewed
            // kafkaProducer.sendSubscriptionRenewedEvent(newSubscription);

            // TODO: Send notification
            // notificationService.sendRenewalConfirmation(organizationId, newSubscription);

            log.info("[UseCase] Organization {} successfully renewed subscription", organizationId);
            return responseUtils.success(newSubscription);
        } catch (AppException e) {
            log.error("Error renewing subscription for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when renewing subscription for organization {}: {}", organizationId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Activate pending subscription (admin approval)
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> activateSubscription(Long subscriptionId, Long activatedBy) {
        try {
            log.info("[UseCase] Activating subscription {}", subscriptionId);

            var subscription = organizationSubscriptionService.activateSubscription(subscriptionId, activatedBy);

            // TODO: Grant module access based on subscription plan
            // moduleAccessService.grantAccessForSubscription(subscription);

            // TODO: Send Kafka event - subscription activated
            // kafkaProducer.sendSubscriptionActivatedEvent(subscription);

            // TODO: Send notification
            // notificationService.sendActivationNotification(subscription.getOrganizationId(),
            // subscription);

            log.info("[UseCase] Successfully activated subscription {}", subscriptionId);
            return responseUtils.success(subscription);
        } catch (AppException e) {
            log.error("Error activating subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when activating subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Reject pending subscription (admin rejection)
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> rejectSubscription(Long subscriptionId, RejectSubscriptionRequest request,
            Long rejectedBy) {
        try {
            log.info("[UseCase] Rejecting subscription {}", subscriptionId);

            organizationSubscriptionService.rejectSubscription(subscriptionId, request.getReason(), rejectedBy);

            // TODO: Send Kafka event - subscription rejected
            // kafkaProducer.sendSubscriptionRejectedEvent(subscriptionId,
            // request.getReason());

            // TODO: Send notification
            // notificationService.sendRejectionNotification(subscriptionId,
            // request.getReason());

            log.info("[UseCase] Successfully rejected subscription {}", subscriptionId);
            return responseUtils.success("Subscription rejected successfully");
        } catch (AppException e) {
            log.error("Error rejecting subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when rejecting subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Extend trial period
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> extendTrial(Long subscriptionId, ExtendTrialRequest request, Long extendedBy) {
        try {
            log.info("[UseCase] Extending trial for subscription {} by {} days", subscriptionId,
                    request.getAdditionalDays());

            var subscription = organizationSubscriptionService.extendTrial(subscriptionId, request.getAdditionalDays(),
                    extendedBy);

            // TODO: Send Kafka event - trial extended
            // kafkaProducer.sendTrialExtendedEvent(subscription);

            // TODO: Send notification
            // notificationService.sendTrialExtensionNotification(subscription.getOrganizationId(),
            // subscription);

            log.info("[UseCase] Successfully extended trial for subscription {}", subscriptionId);
            return responseUtils.success(subscription);
        } catch (AppException e) {
            log.error("Error extending trial for subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when extending trial for subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Expire subscription (system background job)
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> expireSubscription(Long subscriptionId) {
        try {
            log.info("[UseCase] Expiring subscription {}", subscriptionId);

            organizationSubscriptionService.expireSubscription(subscriptionId);

            // TODO: Revoke module access
            // moduleAccessService.revokeAccessForSubscription(subscriptionId);

            // TODO: Send Kafka event - subscription expired
            // kafkaProducer.sendSubscriptionExpiredEvent(subscriptionId);

            // TODO: Send notification
            // notificationService.sendExpirationNotification(subscriptionId);

            log.info("[UseCase] Successfully expired subscription {}", subscriptionId);
            return responseUtils.success("Subscription expired successfully");
        } catch (AppException e) {
            log.error("Error expiring subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when expiring subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get active subscription for organization
     */
    public GeneralResponse<?> getActiveSubscription(Long organizationId) {
        try {
            var subscription = organizationSubscriptionService.getActiveSubscription(organizationId);
            return responseUtils.success(subscription);
        } catch (AppException e) {
            log.error("Error getting active subscription for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting active subscription for organization {}: {}", organizationId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get subscription by ID
     */
    public GeneralResponse<?> getSubscriptionById(Long subscriptionId) {
        try {
            var subscription = organizationSubscriptionService.getSubscriptionById(subscriptionId);
            return responseUtils.success(subscription);
        } catch (AppException e) {
            log.error("Error getting subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get subscription history for organization
     */
    public GeneralResponse<?> getSubscriptionHistory(Long organizationId) {
        try {
            var subscriptions = organizationSubscriptionService.getSubscriptionHistory(organizationId);
            return responseUtils.success(subscriptions);
        } catch (Exception e) {
            log.error("Unexpected error when getting subscription history for organization {}: {}", organizationId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get subscriptions by status
     */
    public GeneralResponse<?> getSubscriptionsByStatus(SubscriptionStatus status) {
        try {
            var subscriptions = organizationSubscriptionService.getSubscriptionsByStatus(status);
            return responseUtils.success(subscriptions);
        } catch (Exception e) {
            log.error("Unexpected error when getting subscriptions by status {}: {}", status, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get expiring subscriptions (for background job)
     */
    public GeneralResponse<?> getExpiringSubscriptions(Long beforeTimestamp) {
        try {
            var subscriptions = organizationSubscriptionService.getExpiringSubscriptions(beforeTimestamp);
            return responseUtils.success(subscriptions);
        } catch (Exception e) {
            log.error("Unexpected error when getting expiring subscriptions: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get trial ending subscriptions (for notification job)
     */
    public GeneralResponse<?> getTrialEndingSubscriptions(Long beforeTimestamp) {
        try {
            var subscriptions = organizationSubscriptionService.getTrialEndingSubscriptions(beforeTimestamp);
            return responseUtils.success(subscriptions);
        } catch (Exception e) {
            log.error("Unexpected error when getting trial ending subscriptions: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Check if organization has active subscription
     */
    public GeneralResponse<?> hasActiveSubscription(Long organizationId) {
        try {
            var hasActive = organizationSubscriptionService.hasActiveSubscription(organizationId);
            return responseUtils.success(hasActive);
        } catch (Exception e) {
            log.error("Unexpected error when checking active subscription for organization {}: {}", organizationId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    /**
     * Get remaining days for subscription
     */
    public GeneralResponse<?> getRemainingDays(Long subscriptionId) {
        try {
            var remainingDays = organizationSubscriptionService.getRemainingDays(subscriptionId);
            return responseUtils.success(remainingDays);
        } catch (AppException e) {
            log.error("Error getting remaining days for subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting remaining days for subscription {}: {}", subscriptionId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }
}
