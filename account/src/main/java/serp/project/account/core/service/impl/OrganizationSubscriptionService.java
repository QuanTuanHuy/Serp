/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.dto.request.CancelSubscriptionRequest;
import serp.project.account.core.domain.dto.request.DowngradeSubscriptionRequest;
import serp.project.account.core.domain.dto.request.SubscribeRequest;
import serp.project.account.core.domain.dto.request.UpgradeSubscriptionRequest;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.enums.BillingCycle;
import serp.project.account.core.domain.enums.SubscriptionStatus;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IOrganizationSubscriptionPort;
import serp.project.account.core.port.store.ISubscriptionPlanPort;
import serp.project.account.core.service.IOrganizationSubscriptionService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.infrastructure.store.mapper.OrganizationSubscriptionMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationSubscriptionService implements IOrganizationSubscriptionService {

    private final IOrganizationSubscriptionPort organizationSubscriptionPort;
    private final ISubscriptionPlanPort subscriptionPlanPort;
    private final ISubscriptionPlanService subscriptionPlanService;
    private final OrganizationSubscriptionMapper organizationSubscriptionMapper;

    private static final String FREE_PLAN_CODE = "FREE";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSubscriptionEntity subscribe(Long organizationId, SubscribeRequest request, Long requestedBy) {
        if (hasActiveSubscription(organizationId)) {
            log.error("Organization {} already has active subscription", organizationId);
            throw new AppException(Constants.ErrorMessage.ORGANIZATION_ALREADY_HAS_ACTIVE_SUBSCRIPTION);
        }

        var plan = subscriptionPlanPort.getById(request.getPlanId())
                .orElseThrow(() -> {
                    log.error("Subscription plan not found with ID: {}", request.getPlanId());
                    return new AppException(Constants.ErrorMessage.SUBSCRIPTION_PLAN_NOT_FOUND);
                });

        SubscriptionStatus status = FREE_PLAN_CODE.equalsIgnoreCase(plan.getPlanCode())
                ? SubscriptionStatus.ACTIVE
                : SubscriptionStatus.PENDING;

        var now = Instant.now().toEpochMilli();
        BillingCycle billingCycle = request.getBillingCycle() != null
                ? request.getBillingCycle()
                : BillingCycle.MONTHLY;

        Long startDate = now;
        Long endDate = calculateEndDate(startDate, billingCycle);
        Long trialEndsAt = plan.getTrialDays() > 0
                ? startDate + (plan.getTrialDays() * 24 * 60 * 60 * 1000L)
                : null;

        BigDecimal totalAmount = plan.getPriceByBillingCycle(billingCycle.name());

        var subscription = OrganizationSubscriptionEntity.builder()
                .organizationId(organizationId)
                .subscriptionPlanId(plan.getId())
                .status(status)
                .billingCycle(billingCycle)
                .startDate(startDate)
                .endDate(endDate)
                .trialEndsAt(trialEndsAt)
                .isAutoRenew(request.getIsAutoRenew() != null ? request.getIsAutoRenew() : true)
                .totalAmount(totalAmount)
                .notes(request.getNotes())
                .createdBy(requestedBy)
                .createdAt(now)
                .build();

        if (SubscriptionStatus.ACTIVE.equals(status)) {
            subscription.setActivatedBy(requestedBy);
            subscription.setActivatedAt(now);
        }

        var savedSubscription = organizationSubscriptionPort.save(subscription);

        log.info("Organization {} subscribed to plan {} with status {}",
                organizationId, request.getPlanId(), status);

        return savedSubscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSubscriptionEntity startTrial(Long organizationId, Long planId, Long requestedBy) {
        log.info("Organization {} starting trial for plan {}", organizationId, planId);

        if (hasActiveSubscription(organizationId)) {
            throw new AppException(Constants.ErrorMessage.ORGANIZATION_ALREADY_HAS_ACTIVE_SUBSCRIPTION);
        }

        var plan = subscriptionPlanPort.getById(planId)
                .orElseThrow(() -> {
                    log.error("Subscription plan not found with ID: {}", planId);
                    return new AppException(Constants.ErrorMessage.SUBSCRIPTION_PLAN_NOT_FOUND);
                });

        if (plan.getTrialDays() == null || plan.getTrialDays() <= 0) {
            throw new AppException(Constants.ErrorMessage.PLAN_DOES_NOT_SUPPORT_TRIAL);
        }

        var now = Instant.now().toEpochMilli();
        Long trialEndsAt = now + (plan.getTrialDays() * 24 * 60 * 60 * 1000L);

        var subscription = organizationSubscriptionMapper.createTrialSubscription(
                organizationId,
                planId,
                BillingCycle.MONTHLY,
                now,
                trialEndsAt,
                trialEndsAt,
                true,
                BigDecimal.ZERO,
                null,
                requestedBy);

        subscription.setActivatedBy(requestedBy);
        subscription.setActivatedAt(now);
        subscription.setCreatedAt(now);

        var savedSubscription = organizationSubscriptionPort.save(subscription);

        return savedSubscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSubscriptionEntity upgradeSubscription(Long organizationId,
            UpgradeSubscriptionRequest request,
            Long requestedBy) {
        log.info("Organization {} upgrading subscription to plan {}", organizationId, request.getNewPlanId());

        validateSubscriptionChange(organizationId, request.getNewPlanId());

        var currentSubscription = getActiveSubscription(organizationId);
        var currentPlan = subscriptionPlanService.getPlanById(currentSubscription.getSubscriptionPlanId());
        var newPlan = subscriptionPlanService.getPlanById(request.getNewPlanId());

        // Validate upgrade (new plan should be "higher" than current)
        if (newPlan.getMonthlyPrice().compareTo(currentPlan.getMonthlyPrice()) <= 0) {
            throw new AppException(Constants.ErrorMessage.NEW_PLAN_MUST_BE_HIGHER_THAN_CURRENT);
        }

        var now = Instant.now().toEpochMilli();
        BillingCycle newBillingCycle = request.getBillingCycle() != null
                ? request.getBillingCycle()
                : currentSubscription.getBillingCycle();

        // Calculate proration
        BigDecimal prorationAmount = currentSubscription.calculateProration(currentPlan, newPlan);
        BigDecimal newTotalAmount = newPlan.getPriceByBillingCycle(newBillingCycle.name());

        // Expire current subscription
        currentSubscription.expire();
        currentSubscription.setUpdatedAt(now);
        organizationSubscriptionPort.update(currentSubscription);

        // Create new subscription (immediate activation) using mapper
        Long newEndDate = calculateEndDate(now, newBillingCycle);
        boolean isAutoRenew = request.getIsAutoRenew() != null ? request.getIsAutoRenew()
                : currentSubscription.getIsAutoRenew();

        var newSubscription = organizationSubscriptionMapper.createActiveSubscription(
                organizationId,
                newPlan.getId(),
                newBillingCycle,
                now,
                newEndDate,
                isAutoRenew,
                newTotalAmount.subtract(prorationAmount),
                request.getNotes(),
                requestedBy);

        newSubscription.setActivatedBy(requestedBy);
        newSubscription.setActivatedAt(now);
        newSubscription.setCreatedAt(now);

        var savedSubscription = organizationSubscriptionPort.save(newSubscription);

        log.info("Organization {} upgraded to plan {} with proration: {}",
                organizationId, newPlan.getId(), prorationAmount);

        // TODO: Send Kafka event - subscription upgraded

        return savedSubscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSubscriptionEntity downgradeSubscription(Long organizationId,
            DowngradeSubscriptionRequest request,
            Long requestedBy) {
        log.info("Organization {} downgrading subscription to plan {}", organizationId, request.getNewPlanId());

        validateSubscriptionChange(organizationId, request.getNewPlanId());

        var currentSubscription = getActiveSubscription(organizationId);
        var currentPlan = subscriptionPlanService.getPlanById(currentSubscription.getSubscriptionPlanId());
        var newPlan = subscriptionPlanService.getPlanById(request.getNewPlanId());

        // Validate downgrade (new plan should be "lower" than current)
        if (newPlan.getMonthlyPrice().compareTo(currentPlan.getMonthlyPrice()) >= 0) {
            throw new AppException(Constants.ErrorMessage.NEW_PLAN_MUST_BE_LOWER_THAN_CURRENT);
        }

        var now = Instant.now().toEpochMilli();

        // Downgrade takes effect at end of current billing period
        Long newStartDate = currentSubscription.getEndDate();
        Long newEndDate = calculateEndDate(newStartDate, currentSubscription.getBillingCycle());
        BigDecimal newTotalAmount = newPlan.getPriceByBillingCycle(currentSubscription.getBillingCycle().name());

        var newSubscription = OrganizationSubscriptionEntity.builder()
                .organizationId(organizationId)
                .subscriptionPlanId(newPlan.getId())
                .status(SubscriptionStatus.PENDING)
                .billingCycle(currentSubscription.getBillingCycle())
                .startDate(newStartDate)
                .endDate(newEndDate)
                .trialEndsAt(null)
                .isAutoRenew(currentSubscription.getIsAutoRenew())
                .totalAmount(newTotalAmount)
                .notes(request.getNotes())
                .createdBy(requestedBy)
                .createdAt(now)
                .build();

        var savedSubscription = organizationSubscriptionPort.save(newSubscription);

        log.info("Organization {} scheduled downgrade to plan {} effective at {}",
                organizationId, newPlan.getId(), newStartDate);

        // TODO: Send Kafka event - subscription downgraded (scheduled)

        return savedSubscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSubscription(Long organizationId, CancelSubscriptionRequest request, Long cancelledBy) {
        log.info("Organization {} cancelling subscription", organizationId);

        validateCancellation(organizationId);

        var subscription = getActiveSubscription(organizationId);
        var now = Instant.now().toEpochMilli();

        subscription.cancel(cancelledBy, request.getReason());
        subscription.setUpdatedAt(now);

        organizationSubscriptionPort.update(subscription);

        log.info("Organization {} cancelled subscription. Reason: {}", organizationId, request.getReason());

        // TODO: Send Kafka event - subscription cancelled
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSubscriptionEntity renewSubscription(Long organizationId, Long renewedBy) {
        log.info("Organization {} renewing subscription", organizationId);

        var expiredSubscription = getActiveSubscription(organizationId);

        if (!expiredSubscription.isExpired()) {
            throw new AppException(Constants.ErrorMessage.SUBSCRIPTION_NOT_EXPIRED);
        }

        var plan = subscriptionPlanService.getPlanById(expiredSubscription.getSubscriptionPlanId());
        var now = Instant.now().toEpochMilli();
        Long newEndDate = calculateEndDate(now, expiredSubscription.getBillingCycle());

        var newSubscription = OrganizationSubscriptionEntity.builder()
                .organizationId(organizationId)
                .subscriptionPlanId(plan.getId())
                .status(SubscriptionStatus.PENDING)
                .billingCycle(expiredSubscription.getBillingCycle())
                .startDate(now)
                .endDate(newEndDate)
                .trialEndsAt(null)
                .isAutoRenew(expiredSubscription.getIsAutoRenew())
                .totalAmount(
                        plan.getPriceByBillingCycle(expiredSubscription.getBillingCycle().name()))
                .createdBy(renewedBy)
                .createdAt(now)
                .build();

        var savedSubscription = organizationSubscriptionPort.save(newSubscription);

        log.info("Organization {} renewed subscription", organizationId);

        // TODO: Send Kafka event - subscription renewed

        return savedSubscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSubscriptionEntity activateSubscription(Long subscriptionId, Long activatedBy) {
        log.info("Activating subscription {}", subscriptionId);

        var subscription = getSubscriptionById(subscriptionId);

        if (!SubscriptionStatus.PENDING.equals(subscription.getStatus())) {
            throw new AppException(Constants.ErrorMessage.SUBSCRIPTION_NOT_PENDING_APPROVAL);
        }

        var now = Instant.now().toEpochMilli();
        subscription.activate(activatedBy);
        subscription.setUpdatedAt(now);

        var activatedSubscription = organizationSubscriptionPort.update(subscription);

        log.info("Subscription {} activated", subscriptionId);

        // TODO: Send Kafka event - subscription activated

        return activatedSubscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectSubscription(Long subscriptionId, String reason, Long rejectedBy) {
        log.info("Rejecting subscription {}", subscriptionId);

        var subscription = getSubscriptionById(subscriptionId);

        if (!SubscriptionStatus.PENDING.equals(subscription.getStatus())) {
            throw new AppException(Constants.ErrorMessage.SUBSCRIPTION_NOT_PENDING_APPROVAL);
        }

        subscription.rejectSubsciption(rejectedBy, reason);

        organizationSubscriptionPort.update(subscription);

        log.info("Subscription {} rejected. Reason: {}", subscriptionId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSubscriptionEntity extendTrial(Long subscriptionId, int additionalDays, Long extendedBy) {
        log.info("Extending trial for subscription {} by {} days", subscriptionId, additionalDays);

        var subscription = getSubscriptionById(subscriptionId);

        if (!subscription.isTrial()) {
            throw new AppException(Constants.ErrorMessage.SUBSCRIPTION_NOT_IN_TRIAL);
        }

        var now = Instant.now().toEpochMilli();
        subscription.extendTrial(additionalDays);
        subscription.setUpdatedAt(now);
        subscription.setUpdatedBy(extendedBy);

        var extendedSubscription = organizationSubscriptionPort.update(subscription);

        log.info("Trial extended for subscription {} until {}", subscriptionId, extendedSubscription.getTrialEndsAt());

        return extendedSubscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void expireSubscription(Long subscriptionId) {
        log.info("Expiring subscription {}", subscriptionId);

        var subscription = getSubscriptionById(subscriptionId);
        var now = Instant.now().toEpochMilli();

        subscription.expire();
        subscription.setUpdatedAt(now);

        organizationSubscriptionPort.update(subscription);

        log.info("Subscription {} expired", subscriptionId);

    }

    @Override
    public OrganizationSubscriptionEntity getActiveSubscription(Long organizationId) {
        return organizationSubscriptionPort.getActiveByOrganizationId(organizationId)
                .orElseThrow(() -> {
                    log.error("No active subscription found for organization {}", organizationId);
                    return new AppException(Constants.ErrorMessage.ACTIVE_SUBSCRIPTION_NOT_FOUND);
                });
    }

    @Override
    public OrganizationSubscriptionEntity getSubscriptionById(Long subscriptionId) {
        return organizationSubscriptionPort.getById(subscriptionId)
                .orElseThrow(() -> {
                    log.error("Subscription not found with ID: {}", subscriptionId);
                    return new AppException(Constants.ErrorMessage.SUBSCRIPTION_NOT_FOUND);
                });
    }

    @Override
    public List<OrganizationSubscriptionEntity> getSubscriptionHistory(Long organizationId) {
        return organizationSubscriptionPort.getByOrganizationId(organizationId);
    }

    @Override
    public List<OrganizationSubscriptionEntity> getSubscriptionsByStatus(SubscriptionStatus status) {
        return organizationSubscriptionPort.getByStatus(status);
    }

    @Override
    public List<OrganizationSubscriptionEntity> getExpiringSubscriptions(Long beforeTimestamp) {
        return organizationSubscriptionPort.getExpiringBefore(beforeTimestamp);
    }

    @Override
    public List<OrganizationSubscriptionEntity> getTrialEndingSubscriptions(Long beforeTimestamp) {
        return organizationSubscriptionPort.getTrialEndingBefore(beforeTimestamp);
    }

    @Override
    public boolean hasActiveSubscription(Long organizationId) {
        return organizationSubscriptionPort.existsActiveSubscriptionForOrganization(organizationId);
    }

    @Override
    public boolean canAccessModule(Long organizationId, Long moduleId) {
        // TODO: Implement module access checking based on subscription plan
        // Check if organization's subscription plan includes the module
        return false;
    }

    @Override
    public BigDecimal calculateProration(OrganizationSubscriptionEntity currentSubscription,
            Long newPlanId, String newBillingCycle) {
        var currentPlan = subscriptionPlanService.getPlanById(currentSubscription.getSubscriptionPlanId());
        var newPlan = subscriptionPlanService.getPlanById(newPlanId);

        return currentSubscription.calculateProration(currentPlan, newPlan);
    }

    @Override
    public int getRemainingDays(Long subscriptionId) {
        var subscription = getSubscriptionById(subscriptionId);
        return subscription.getRemainingDays();
    }

    @Override
    public void validateSubscriptionChange(Long organizationId, Long newPlanId) {
        if (!hasActiveSubscription(organizationId)) {
            throw new AppException(Constants.ErrorMessage.NO_ACTIVE_SUBSCRIPTION);
        }

        var newPlan = subscriptionPlanService.getPlanById(newPlanId);

        if (!newPlan.getIsActive()) {
            throw new AppException(Constants.ErrorMessage.PLAN_NOT_ACTIVE);
        }
    }

    @Override
    public void validateCancellation(Long organizationId) {
        if (!hasActiveSubscription(organizationId)) {
            throw new AppException(Constants.ErrorMessage.NO_ACTIVE_SUBSCRIPTION);
        }
    }

    // === Helper Methods ===

    private Long calculateEndDate(Long startDate, BillingCycle billingCycle) {
        Instant startInstant = Instant.ofEpochMilli(startDate);

        Instant endInstant = BillingCycle.YEARLY.equals(billingCycle)
                ? startInstant.plus(365, ChronoUnit.DAYS)
                : startInstant.plus(30, ChronoUnit.DAYS);

        return endInstant.toEpochMilli();
    }
}
