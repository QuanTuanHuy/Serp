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

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class SubscriptionPlanEntity extends BaseEntity {

    private String planName;

    private String planCode;

    private String description;

    private BigDecimal monthlyPrice;

    private BigDecimal yearlyPrice;

    private Integer maxUsers;

    /**
     * Trial period duration in days
     * 0 = no trial
     */
    private Integer trialDays;

    /**
     * Whether this plan is active and available for selection
     */
    private Boolean isActive;

    /**
     * Whether this is a custom plan for a specific organization
     * true = custom plan (organization_id must be set)
     */
    private Boolean isCustom;

    private Long organizationId;

    private Integer displayOrder;

    private Long createdBy;

    private Long updatedBy;

    @JsonIgnore
    public boolean isAvailable() {
        return Boolean.TRUE.equals(this.isActive);
    }

    @JsonIgnore
    public boolean isPredefined() {
        return Boolean.FALSE.equals(this.isCustom) && this.organizationId == null;
    }

    @JsonIgnore
    public boolean hasTrial() {
        return this.trialDays != null && this.trialDays > 0;
    }

    @JsonIgnore
    public boolean hasUserLimit() {
        return this.maxUsers != null && this.maxUsers > 0;
    }

    @JsonIgnore
    public BigDecimal getYearlySavings() {
        if (monthlyPrice == null || yearlyPrice == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal monthlyTotal = monthlyPrice.multiply(BigDecimal.valueOf(12));
        return monthlyTotal.subtract(yearlyPrice);
    }

    public BigDecimal getPriceByBillingCycle(String billingCycle) {
        if (billingCycle == null) {
            return this.monthlyPrice;
        }
        return BillingCycle.MONTHLY.name().equalsIgnoreCase(billingCycle) ? this.monthlyPrice : this.yearlyPrice;
    }
}
