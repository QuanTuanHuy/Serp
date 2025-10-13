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
import serp.project.account.core.domain.enums.LicenseType;

/**
 * Mapping between subscription plans and modules
 * Defines which modules are included in each plan and at what license tier
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class SubscriptionPlanModuleEntity extends BaseEntity {

    private Long subscriptionPlanId;

    private Long moduleId;

    /**
     * Whether this module is included in the plan
     */
    private Boolean isIncluded;

    private LicenseType licenseType;

    /**
     * Maximum users allowed to use this specific module
     * null = inherit from subscription plan's maxUsers
     * Used for per-module user limits
     */
    private Integer maxUsersPerModule;

    private Long createdBy;

    private Long updatedBy;

    @JsonIgnore
    public boolean isAccessible() {
        return Boolean.TRUE.equals(this.isIncluded);
    }

    @JsonIgnore
    public boolean hasUserLimit() {
        return this.maxUsersPerModule != null && this.maxUsersPerModule > 0;
    }
}
