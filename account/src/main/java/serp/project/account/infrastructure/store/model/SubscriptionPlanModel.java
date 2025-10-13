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

import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans", indexes = {
        @Index(name = "idx_plan_code", columnList = "plan_code"),
        @Index(name = "idx_is_active", columnList = "is_active"),
        @Index(name = "idx_is_custom_org", columnList = "is_custom, organization_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class SubscriptionPlanModel extends BaseModel {

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Column(name = "plan_code", nullable = false, unique = true, length = 50)
    private String planCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "yearly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal yearlyPrice;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "trial_days", nullable = false)
    private Integer trialDays;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_custom", nullable = false)
    private Boolean isCustom;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
