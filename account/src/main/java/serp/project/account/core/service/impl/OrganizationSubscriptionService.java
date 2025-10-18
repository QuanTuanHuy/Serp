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
import serp.project.account.core.domain.entity.SubscriptionPlanEntity;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSubscriptionEntity subscribe(Long organizationId, SubscribeRequest request, Long requestedBy,
            SubscriptionPlanEntity plan) {

        SubscriptionStatus status = plan.isFreePlan()
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

        BigDecimal totalAmount = plan.getPriceByBillingCycle(billingCycle.toString());

        var subscription = organizationSubscriptionMapper.buildNewOrgSub(
                organizationId,
                plan.getId(),
                status,
                billingCycle,
                startDate,
                endDate,
                trialEndsAt,
                request.getIsAutoRenew() != null ? request.getIsAutoRenew() : true,
                totalAmount,
                request.getNotes(),
                requestedBy);

        var savedSubscription = organizationSubscriptionPort.save(subscription);

        log.info("Organization {} subscribed to plan {} with status {}",
                organizationId, request.getPlanId(), status);

        return savedSubscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSubscriptionEntity startTrial(Long organizationId, SubscriptionPlanEntity plan,
            Long requestedBy) {

        var now = Instant.now().toEpochMilli();
        Long trialEndsAt = now + (plan.getTrialDays() * 24 * 60 * 60 * 1000L);

        var subscription = organizationSubscriptionMapper.createTrialSubscription(
                organizationId,
                plan.getId(),
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
            Long requestedBy, SubscriptionPlanEntity currentPlan, SubscriptionPlanEntity newPlan) {

        validateUpgrade(currentPlan, newPlan);

        var currentSubscription = getActiveSubscription(organizationId);
        if (!currentSubscription.canUpgrade()) {
            throw new AppException(Constants.ErrorMessage.SUBSCRIPTION_CANNOT_BE_UPGRADED);
        }

        var now = Instant.now().toEpochMilli();
        BillingCycle newBillingCycle = request.getBillingCycle() != null
                ? request.getBillingCycle()
                : currentSubscription.getBillingCycle();

        // Calculate proration
        BigDecimal prorationAmount = currentSubscription.calculateProration(currentPlan, newPlan);
        BigDecimal newTotalAmount = newPlan.getPriceByBillingCycle(newBillingCycle.toString());
        newTotalAmount = newTotalAmount.subtract(prorationAmount);

        // Expire current subscription
        currentSubscription.expire();
        organizationSubscriptionPort.update(currentSubscription);

        // Create new subscription (immediate activation)
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
                newTotalAmount,
                request.getNotes(),
                requestedBy);

        newSubscription.setActivatedBy(requestedBy);
        newSubscription.setActivatedAt(now);
        newSubscription.setCreatedAt(now);

        var savedSubscription = organizationSubscriptionPort.save(newSubscription);

        log.info("Organization {} upgraded to plan {} with proration: {}",
                organizationId, newPlan.getId(), prorationAmount);

        return savedSubscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSubscriptionEntity downgradeSubscription(Long organizationId,
            DowngradeSubscriptionRequest request,
            Long requestedBy, SubscriptionPlanEntity currentPlan, SubscriptionPlanEntity newPlan) {

        // Implement later: Fix logic

        validateDowngrade(currentPlan, newPlan);

        var currentSubscription = getActiveSubscription(organizationId);
        currentSubscription.expire();
        organizationSubscriptionPort.update(currentSubscription);

        var now = Instant.now().toEpochMilli();
        Long newStartDate = currentSubscription.getEndDate();
        Long newEndDate = calculateEndDate(newStartDate, currentSubscription.getBillingCycle());
        BigDecimal newTotalAmount = newPlan.getPriceByBillingCycle(currentSubscription.getBillingCycle().toString());

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

        return savedSubscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSubscription(Long organizationId, CancelSubscriptionRequest request, Long cancelledBy) {
        var subscription = getActiveSubscription(organizationId);
        subscription.cancel(cancelledBy, request.getReason());

        organizationSubscriptionPort.update(subscription);

        log.info("Organization {} cancelled subscription. Reason: {}", organizationId, request.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSubscriptionEntity renewSubscription(Long organizationId, Long currentSubscriptionId,
            Long renewedBy) {
        // Implement later: Fix logic

        var currentSubscription = getSubscriptionById(currentSubscriptionId);
        if (!currentSubscription.canRenew()) {
            throw new AppException(Constants.ErrorMessage.SUBSCRIPTION_NOT_EXPIRED);
        }
        currentSubscription.expire();
        organizationSubscriptionPort.update(currentSubscription);

        var plan = subscriptionPlanService.getPlanById(currentSubscription.getSubscriptionPlanId());
        var now = Instant.now().toEpochMilli();
        Long newEndDate = calculateEndDate(now, currentSubscription.getBillingCycle());

        var newSubscription = OrganizationSubscriptionEntity.builder()
                .organizationId(organizationId)
                .subscriptionPlanId(plan.getId())
                .status(SubscriptionStatus.PENDING)
                .billingCycle(currentSubscription.getBillingCycle())
                .startDate(now)
                .endDate(newEndDate)
                .trialEndsAt(null)
                .isAutoRenew(currentSubscription.getIsAutoRenew())
                .totalAmount(
                        plan.getPriceByBillingCycle(currentSubscription.getBillingCycle().toString()))
                .createdBy(renewedBy)
                .createdAt(now)
                .build();

        var savedSubscription = organizationSubscriptionPort.save(newSubscription);

        return savedSubscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSubscriptionEntity activateSubscription(Long subscriptionId, Long activatedBy) {
        var subscription = getSubscriptionById(subscriptionId);

        if (!subscription.isPending()) {
            throw new AppException(Constants.ErrorMessage.SUBSCRIPTION_NOT_PENDING_APPROVAL);
        }

        subscription.activate(activatedBy);
        return organizationSubscriptionPort.update(subscription);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectSubscription(Long subscriptionId, String reason, Long rejectedBy) {
        var subscription = getSubscriptionById(subscriptionId);

        if (!subscription.isPending()) {
            throw new AppException(Constants.ErrorMessage.SUBSCRIPTION_NOT_PENDING_APPROVAL);
        }

        subscription.rejectSubscription(rejectedBy, reason);
        organizationSubscriptionPort.update(subscription);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSubscriptionEntity extendTrial(Long subscriptionId, int additionalDays, Long extendedBy) {
        var subscription = getSubscriptionById(subscriptionId);

        if (!subscription.isTrial()) {
            throw new AppException(Constants.ErrorMessage.SUBSCRIPTION_NOT_IN_TRIAL);
        }
        subscription.extendTrial(additionalDays);

        return organizationSubscriptionPort.update(subscription);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void expireSubscription(Long subscriptionId) {
        var subscription = getSubscriptionById(subscriptionId);

        var now = Instant.now().toEpochMilli();
        subscription.expire();
        subscription.setUpdatedAt(now);

        organizationSubscriptionPort.update(subscription);
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
        // Implement later: module access checking based on subscription plan
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

        var newPlan = subscriptionPlanPort.getById(newPlanId)
                .orElseThrow(() -> {
                    log.error("Subscription plan not found with ID: {}", newPlanId);
                    return new AppException(Constants.ErrorMessage.SUBSCRIPTION_PLAN_NOT_FOUND);
                });

        if (!newPlan.isAvailable()) {
            throw new AppException(Constants.ErrorMessage.PLAN_NOT_ACTIVE);
        }
    }

    @Override
    public void validateCancellation(Long organizationId) {
        if (!hasActiveSubscription(organizationId)) {
            throw new AppException(Constants.ErrorMessage.NO_ACTIVE_SUBSCRIPTION);
        }
    }

    @Override
    public void validateSubscriptionChange(SubscriptionPlanEntity currentPlan, SubscriptionPlanEntity newPlan) {
        if (!newPlan.isAvailable()) {
            throw new AppException(Constants.ErrorMessage.PLAN_NOT_ACTIVE);
        }
    }

    @Override
    public void validateUpgrade(SubscriptionPlanEntity currentPlan, SubscriptionPlanEntity newPlan) {
        if (currentPlan.isFreePlan() && !newPlan.isFreePlan()) {
            return;
        }
        if (newPlan.getMonthlyPrice().compareTo(currentPlan.getMonthlyPrice()) <= 0) {
            throw new AppException(Constants.ErrorMessage.NEW_PLAN_MUST_BE_HIGHER_THAN_CURRENT);
        }
    }

    public void validateDowngrade(SubscriptionPlanEntity currentPlan, SubscriptionPlanEntity newPlan) {
        if (newPlan.isFreePlan()) {
            return;
        }
        if (newPlan.getMonthlyPrice().compareTo(currentPlan.getMonthlyPrice()) >= 0) {
            throw new AppException(Constants.ErrorMessage.NEW_PLAN_MUST_BE_LOWER_THAN_CURRENT);
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
