/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.BillingCycle;
import serp.project.account.core.domain.enums.SubscriptionStatus;

import java.math.BigDecimal;


@Entity
@Table(name = "organization_subscriptions", indexes = {
        @Index(name = "idx_org_sub_org_id", columnList = "organization_id"),
        @Index(name = "idx_org_sub_status", columnList = "status"),
        @Index(name = "idx_org_sub_end_date", columnList = "end_date"),
        @Index(name = "idx_org_sub_org_status", columnList = "organization_id, status")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OrganizationSubscriptionModel extends BaseModel {

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "subscription_plan_id", nullable = false)
    private Long subscriptionPlanId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private SubscriptionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private BillingCycle billingCycle;

    @Column(name = "start_date", nullable = false)
    private Long startDate;

    @Column(name = "end_date")
    private Long endDate;

    @Column(name = "trial_ends_at")
    private Long trialEndsAt;

    @Column(name = "is_auto_renew", nullable = false)
    private Boolean isAutoRenew;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "activated_by")
    private Long activatedBy;

    @Column(name = "activated_at")
    private Long activatedAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancelled_at")
    private Long cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
