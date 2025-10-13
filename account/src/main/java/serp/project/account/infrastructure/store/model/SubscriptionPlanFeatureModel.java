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

@Entity
@Table(name = "subscription_plan_features", uniqueConstraints = {
        @UniqueConstraint(name = "uk_plan_feature", columnNames = { "subscription_plan_id", "feature_code" })
}, indexes = {
        @Index(name = "idx_plan_feature", columnList = "subscription_plan_id, feature_code"),
        @Index(name = "idx_feature_code", columnList = "feature_code")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class SubscriptionPlanFeatureModel extends BaseModel {

    @Column(name = "subscription_plan_id", nullable = false)
    private Long subscriptionPlanId;

    @Column(name = "feature_code", nullable = false, length = 100)
    private String featureCode;

    @Column(name = "feature_value", columnDefinition = "TEXT")
    private String featureValue;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
