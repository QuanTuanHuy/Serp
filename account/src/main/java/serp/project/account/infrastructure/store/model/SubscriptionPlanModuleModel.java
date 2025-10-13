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
import serp.project.account.core.domain.enums.LicenseType;

@Entity
@Table(name = "subscription_plan_modules", uniqueConstraints = {
        @UniqueConstraint(name = "uk_plan_module", columnNames = { "subscription_plan_id", "module_id" })
}, indexes = {
        @Index(name = "idx_plan_module", columnList = "subscription_plan_id, module_id"),
        @Index(name = "idx_module_plan", columnList = "module_id, subscription_plan_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class SubscriptionPlanModuleModel extends BaseModel {

    @Column(name = "subscription_plan_id", nullable = false)
    private Long subscriptionPlanId;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    @Column(name = "is_included", nullable = false)
    private Boolean isIncluded;

    @Column(name = "license_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private LicenseType licenseType;

    @Column(name = "max_users_per_module")
    private Integer maxUsersPerModule;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
