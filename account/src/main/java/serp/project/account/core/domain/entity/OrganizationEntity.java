/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.BillingCycle;
import serp.project.account.core.domain.enums.OrganizationStatus;
import serp.project.account.core.domain.enums.OrganizationType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OrganizationEntity extends BaseEntity {
    private String name;

    private String code;

    private String description;

    private String address;

    private Long ownerId;

    private OrganizationType organizationType;

    private String industry;

    private Integer employeeCount;

    private Long subscriptionPlanId;

    private Long subscriptionExpiresAt;

    private BillingCycle currentBillingCycle;

    private Long nextBillingDate;

    private OrganizationStatus status;

    private String timezone;

    private String currency;

    private String language;

    private String logoUrl;

    private String primaryColor;

    private String website;

    private String phoneNumber;

    private String email;

    private List<ModuleLicenseEntity> moduleLicenses;
}
