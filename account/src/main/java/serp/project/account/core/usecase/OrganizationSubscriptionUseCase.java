/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.*;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IOrganizationSubscriptionService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationSubscriptionUseCase {

    private final IOrganizationSubscriptionService organizationSubscriptionService;
    private final ISubscriptionPlanService subscriptionPlanService;
    private final IOrganizationService organizationService;

    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> subscribe(Long organizationId, SubscribeRequest request, Long requestedBy) {
        try {
            log.info("[UseCase] Organization {} subscribing to plan {}", organizationId, request.getPlanId());

            if (organizationSubscriptionService.hasActiveSubscription(organizationId)) {
                throw new AppException(Constants.ErrorMessage.ORGANIZATION_ALREADY_HAS_ACTIVE_SUBSCRIPTION);
            }
            var plan = subscriptionPlanService.getPlanById(request.getPlanId());
            if (!plan.isAvailable()) {
                throw new AppException(Constants.ErrorMessage.PLAN_NOT_ACTIVE);
            }

            var subscription = organizationSubscriptionService.subscribe(organizationId, request, requestedBy, plan);
            organizationService.updateSubscription(organizationId, subscription);

            // Implement later: Send notification

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

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> startTrial(Long organizationId, Long planId, Long requestedBy) {
        try {
            log.info("[UseCase] Organization {} starting trial for plan {}", organizationId, planId);

            if (!organizationSubscriptionService.hasActiveSubscription(organizationId)) {
                throw new AppException(Constants.ErrorMessage.ORGANIZATION_ALREADY_HAS_ACTIVE_SUBSCRIPTION);
            }
            var plan = subscriptionPlanService.getPlanById(planId);
            if (!plan.isAvailable()) {
                throw new AppException(Constants.ErrorMessage.PLAN_NOT_ACTIVE);
            }
            if (!plan.hasTrial()) {
                throw new AppException(Constants.ErrorMessage.PLAN_DOES_NOT_SUPPORT_TRIAL);
            }

            var subscription = organizationSubscriptionService.startTrial(organizationId, plan, requestedBy);
            organizationService.updateSubscription(organizationId, subscription);

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

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> upgradeSubscription(Long organizationId, UpgradeSubscriptionRequest request,
            Long requestedBy) {
        try {
            log.info("[UseCase] Organization {} upgrading subscription to plan {}", organizationId,
                    request.getNewPlanId());

            var currentSubscription = organizationSubscriptionService.getActiveSubscription(organizationId);
            var currentPlan = subscriptionPlanService.getPlanById(currentSubscription.getSubscriptionPlanId());
            var newPlan = subscriptionPlanService.getPlanById(request.getNewPlanId());
            if (!newPlan.isAvailable()) {
                throw new AppException(Constants.ErrorMessage.PLAN_NOT_ACTIVE);
            }

            var newSubscription = organizationSubscriptionService.upgradeSubscription(organizationId, request,
                    requestedBy, currentPlan, newPlan);
            organizationService.updateSubscription(organizationId, newSubscription);

            // Implement later: Process payment with proration

            log.info("[UseCase] Organization {} successfully upgraded to plan {}", organizationId,
                    request.getNewPlanId());
            return responseUtils.success(newSubscription);
        } catch (AppException e) {
            log.error("Error upgrading subscription for organization {}: {}", organizationId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when upgrading subscription for organization {}: {}", organizationId,
                    e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> downgradeSubscription(Long organizationId, DowngradeSubscriptionRequest request,
            Long requestedBy) {
        try {
            log.info("[UseCase] Organization {} downgrading subscription to plan {}", organizationId,
                    request.getNewPlanId());

            var currentSubscription = organizationSubscriptionService.getActiveSubscription(organizationId);
            var currentPlan = subscriptionPlanService.getPlanById(currentSubscription.getSubscriptionPlanId());
            var newPlan = subscriptionPlanService.getPlanById(request.getNewPlanId());
            if (!newPlan.isAvailable()) {
                throw new AppException(Constants.ErrorMessage.PLAN_NOT_ACTIVE);
            }

            var newSubscription = organizationSubscriptionService.downgradeSubscription(organizationId, request,
                    requestedBy, currentPlan, newPlan);
            organizationService.updateSubscription(organizationId, newSubscription);

            // Implement later: Send notification

            log.info("[UseCase] Organization {} scheduled downgrade to plan {}", organizationId,
                    request.getNewPlanId());
            return responseUtils.success(newSubscription);
        } catch (AppException e) {
            log.error("Error downgrading subscription for organization {}: {}", organizationId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when downgrading subscription for organization {}: {}", organizationId,
                    e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> cancelSubscription(Long organizationId, CancelSubscriptionRequest request,
            Long cancelledBy) {
        try {
            log.info("[UseCase] Organization {} cancelling subscription", organizationId);

            organizationSubscriptionService.cancelSubscription(organizationId, request, cancelledBy);

            // Implement later: Send notification

            log.info("[UseCase] Organization {} successfully cancelled subscription", organizationId);
            return responseUtils.success("Subscription cancelled successfully");
        } catch (AppException e) {
            log.error("Error cancelling subscription for organization {}: {}", organizationId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when cancelling subscription for organization {}: {}", organizationId,
                    e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> renewSubscription(Long organizationId, Long renewedBy) {
        try {
            log.info("[UseCase] Organization {} renewing subscription", organizationId);

            var organization = organizationService.getOrganizationById(organizationId);
            var newSubscription = organizationSubscriptionService.renewSubscription(
                    organizationId,
                    organization.getSubscriptionId(),
                    renewedBy);
                    
            organizationService.updateSubscription(organizationId, newSubscription);

            // Implement later: Process payment
            // Send notification

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

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> activateSubscription(Long subscriptionId, Long activatedBy) {
        try {
            log.info("[UseCase] Activating subscription {}", subscriptionId);

            var subscription = organizationSubscriptionService.activateSubscription(subscriptionId, activatedBy);

            // Implement later: Grant module access based on subscription plan

            log.info("[UseCase] Successfully activated subscription {}", subscriptionId);
            return responseUtils.success(subscription);
        } catch (AppException e) {
            log.error("Error activating subscription {}: {}", subscriptionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when activating subscription {}: {}", subscriptionId, e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> rejectSubscription(Long subscriptionId, RejectSubscriptionRequest request,
            Long rejectedBy) {
        try {
            log.info("[UseCase] Rejecting subscription {}", subscriptionId);

            organizationSubscriptionService.rejectSubscription(subscriptionId, request.getReason(), rejectedBy);

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

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> extendTrial(Long subscriptionId, ExtendTrialRequest request, Long extendedBy) {
        try {
            log.info("[UseCase] Extending trial for subscription {} by {} days", subscriptionId,
                    request.getAdditionalDays());

            var subscription = organizationSubscriptionService.extendTrial(subscriptionId, request.getAdditionalDays(),
                    extendedBy);

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

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> expireSubscription(Long subscriptionId) {
        try {
            log.info("[UseCase] Expiring subscription {}", subscriptionId);

            organizationSubscriptionService.expireSubscription(subscriptionId);

            // Implement later: Revoke module access

            log.info("[UseCase] Successfully expired subscription {}", subscriptionId);
            return responseUtils.success("Subscription expired successfully");
        } catch (AppException e) {
            log.error("Error expiring subscription {}: {}", subscriptionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when expiring subscription {}: {}", subscriptionId, e.getMessage());
            throw e;
        }
    }

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

}
