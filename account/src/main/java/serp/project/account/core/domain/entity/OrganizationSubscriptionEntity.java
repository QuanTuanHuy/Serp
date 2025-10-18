/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.BillingCycle;
import serp.project.account.core.domain.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Entity track subscription history của organization
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OrganizationSubscriptionEntity extends BaseEntity {

    private Long organizationId;

    private Long subscriptionPlanId;

    private SubscriptionStatus status;

    private BillingCycle billingCycle;

    private Long startDate;

    private Long endDate;

    private Long trialEndsAt;

    private Boolean isAutoRenew;

    private BigDecimal totalAmount;

    private String notes;

    private Long activatedBy;

    private Long activatedAt;

    private Long cancelledBy;

    private Long cancelledAt;

    private String cancellationReason;

    private Long createdBy;

    private Long updatedBy;

    @JsonIgnore
    public boolean isActive() {
        return this.status == SubscriptionStatus.ACTIVE && !isExpired();
    }

    @JsonIgnore
    public boolean isTrial() {
        if (this.status != SubscriptionStatus.TRIAL) {
            return false;
        }
        if (this.trialEndsAt == null) {
            return false;
        }
        long now = Instant.now().toEpochMilli();
        return now < this.trialEndsAt;
    }

    @JsonIgnore
    public boolean isExpired() {
        if (this.endDate == null) {
            return false; // Perpetual (FREE plan)
        }
        long now = Instant.now().toEpochMilli();
        return now > this.endDate;
    }

    @JsonIgnore
    public boolean isCancelled() {
        return this.status == SubscriptionStatus.CANCELLED;
    }

    @JsonIgnore
    public boolean isPending() {
        return this.status == SubscriptionStatus.PENDING;
    }

    @JsonIgnore
    public boolean canUpgrade() {
        return this.status == SubscriptionStatus.ACTIVE || this.status == SubscriptionStatus.TRIAL;
    }

    @JsonIgnore
    public boolean canDowngrade() {
        return this.status == SubscriptionStatus.ACTIVE;
    }

    @JsonIgnore
    public boolean canCancel() {
        return this.status == SubscriptionStatus.ACTIVE || this.status == SubscriptionStatus.TRIAL;
    }

    @JsonIgnore
    public boolean canRenew() {
        return this.status == SubscriptionStatus.EXPIRED ||
                (this.status == SubscriptionStatus.ACTIVE && getRemainingDays() <= 7);
    }

    /**
     * Get remaining days until expiration
     * 
     * @return remaining days, -1 if perpetual, 0 if expired
     */
    @JsonIgnore
    public int getRemainingDays() {
        if (this.endDate == null) {
            return -1;
        }
        long now = Instant.now().toEpochMilli();
        if (now > this.endDate) {
            return 0;
        }
        long diffMillis = this.endDate - now;
        return (int) (diffMillis / (1000 * 60 * 60 * 24));
    }

    /**
     * Get remaining trial days
     * 
     * @return remaining trial days, 0 if not trial or trial expired
     */
    @JsonIgnore
    public int getRemainingTrialDays() {
        if (!isTrial() || this.trialEndsAt == null) {
            return 0;
        }
        long now = Instant.now().toEpochMilli();
        if (now > this.trialEndsAt) {
            return 0;
        }
        long diffMillis = this.trialEndsAt - now;
        return (int) (diffMillis / (1000 * 60 * 60 * 24));
    }

    /**
     * Calculate proration amount when upgrading/downgrading
     * Formula: (remainingDays / totalDays) * oldPlanPrice - newPlanPrice
     * 
     * @param newPlan     The new subscription plan
     * @param currentPlan The current subscription plan
     * @return Proration amount (positive = refund, negative = charge)
     */
    @JsonIgnore
    public BigDecimal calculateProration(SubscriptionPlanEntity currentPlan, SubscriptionPlanEntity newPlan) {
        if (this.endDate == null || currentPlan == null || newPlan == null) {
            return BigDecimal.ZERO;
        }

        int remainingDays = getRemainingDays();
        if (remainingDays <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal currentPlanPrice = this.billingCycle == BillingCycle.MONTHLY
                ? currentPlan.getMonthlyPrice()
                : currentPlan.getYearlyPrice();

        BigDecimal newPlanPrice = this.billingCycle == BillingCycle.MONTHLY
                ? newPlan.getMonthlyPrice()
                : newPlan.getYearlyPrice();

        if (currentPlanPrice == null || newPlanPrice == null) {
            return BigDecimal.ZERO;
        }

        int totalDays = this.billingCycle == BillingCycle.MONTHLY ? 30 : 365;

        BigDecimal unusedAmount = currentPlanPrice
                .multiply(BigDecimal.valueOf(remainingDays))
                .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);

        return unusedAmount.subtract(newPlanPrice);
    }

    @JsonIgnore
    public boolean isTrialEndingSoon() {
        return isTrial() && getRemainingTrialDays() <= 3;
    }

    @JsonIgnore
    public boolean isEndingSoon() {
        return isActive() && getRemainingDays() > 0 && getRemainingDays() <= 7;
    }

    @JsonIgnore
    public void activate(Long adminId) {
        if (this.status != SubscriptionStatus.PENDING && this.status != SubscriptionStatus.TRIAL) {
            throw new IllegalStateException("Can only activate PENDING or TRIAL subscriptions");
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.activatedBy = adminId;
        this.activatedAt = Instant.now().toEpochMilli();
    }

    @JsonIgnore
    public void expire() {
        if (this.status != SubscriptionStatus.ACTIVE && this.status != SubscriptionStatus.TRIAL) {
            throw new IllegalStateException("Can only expire ACTIVE or TRIAL subscriptions");
        }
        this.status = SubscriptionStatus.EXPIRED;
        this.setUpdatedAt(Instant.now().toEpochMilli());
    }

    @JsonIgnore
    public void cancel(Long userId, String reason) {
        if (!canCancel()) {
            throw new IllegalStateException("Cannot cancel subscription in current status: " + this.status);
        }
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledBy = userId;
        this.cancelledAt = Instant.now().toEpochMilli();
        this.cancellationReason = reason;
        this.setUpdatedAt(Instant.now().toEpochMilli());
    }

    @JsonIgnore
    public void extendTrial(int additionalDays) {
        if (this.status != SubscriptionStatus.TRIAL) {
            throw new IllegalStateException("Can only extend trial subscriptions");
        }
        if (this.trialEndsAt == null) {
            throw new IllegalStateException("Trial end date is not set");
        }
        Instant currentTrialEnd = Instant.ofEpochMilli(this.trialEndsAt);
        Instant newTrialEnd = currentTrialEnd.plus(additionalDays, ChronoUnit.DAYS);
        this.trialEndsAt = newTrialEnd.toEpochMilli();
    }

    @JsonIgnore
    public void rejectSubscription(Long rejectedBy, String reason) {
        if (this.status != SubscriptionStatus.PENDING) {
            throw new IllegalStateException("Can only reject PENDING subscriptions");
        }
        long now = Instant.now().toEpochMilli();
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledBy = rejectedBy;
        this.cancelledAt = now;
        this.cancellationReason = reason;
        this.updatedBy = rejectedBy;
        this.setUpdatedAt(now);
    }
}
