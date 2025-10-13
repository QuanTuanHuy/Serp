/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.account.core.domain.enums.BillingCycle;
import serp.project.account.core.domain.enums.SubscriptionStatus;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationSubscriptionResponse {

    private Long id;

    private Long organizationId;

    private String organizationName;

    private SubscriptionPlanResponse subscriptionPlan;

    private SubscriptionStatus status;

    private BillingCycle billingCycle;

    private Long startDate;

    private Long endDate;

    private Long trialEndsAt;

    private Boolean isAutoRenew;

    private BigDecimal totalAmount;

    private String notes;

    private Long activatedBy;

    private String activatedByName;

    private Long activatedAt;

    private Long cancelledBy;

    private String cancelledByName;

    private Long cancelledAt;

    private String cancellationReason;

    // Computed fields
    private Boolean isActive;

    private Boolean isTrial;

    private Boolean isExpired;

    private Boolean canUpgrade;

    private Boolean canDowngrade;

    private Boolean canCancel;

    private Boolean canRenew;

    private Integer remainingDays;

    private Integer remainingTrialDays;

    private Boolean isEndingSoon;

    private Boolean isTrialEndingSoon;

    private Long createdAt;

    private Long updatedAt;
}
