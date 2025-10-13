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

/**
 * Feature flags and limits for subscription plans
 * Stores extensible key-value pairs for plan features
 * Examples: storage limits, API rate limits, priority support, custom branding,
 * etc.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class SubscriptionPlanFeatureEntity extends BaseEntity {

    private Long subscriptionPlanId;

    /**
     * Feature code/identifier (e.g., "MAX_STORAGE_GB", "API_CALLS_PER_DAY")
     * Should be UPPERCASE snake_case
     */
    private String featureCode;

    /**
     * Feature value (can be string, number, JSON, etc.)
     * Examples:
     * - "100" (for MAX_STORAGE_GB)
     * - "10000" (for API_CALLS_PER_DAY)
     * - "true" (for PRIORITY_SUPPORT)
     * - "unlimited" (for MAX_PROJECTS)
     */
    private String featureValue;

    /**
     * Whether this feature is enabled for the plan
     */
    private Boolean isEnabled;

    private Long createdBy;

    private Long updatedBy;

    @JsonIgnore
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isEnabled);
    }

    @JsonIgnore
    public boolean isUnlimited() {
        return "unlimited".equalsIgnoreCase(this.featureValue) ||
                "-1".equals(this.featureValue);
    }

    @JsonIgnore
    public Integer getIntValue() {
        if (featureValue == null || featureValue.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(featureValue.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @JsonIgnore
    public Boolean getBooleanValue() {
        if (featureValue == null || featureValue.trim().isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(featureValue.trim());
    }
}
