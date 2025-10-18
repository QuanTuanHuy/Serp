package serp.project.account.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.BillingCycle;
import serp.project.account.core.domain.enums.OrganizationStatus;
import serp.project.account.core.domain.enums.OrganizationType;

@Entity
@Table(name = "organizations")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OrganizationModel extends BaseModel {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "organization_type")
    @Enumerated(EnumType.STRING)
    private OrganizationType organizationType;

    @Column(name = "industry")
    private String industry;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(name = "subscription_expires_at")
    private Long subscriptionExpiresAt;

    @Column(name = "current_billing_cycle", length = 20)
    @Enumerated(EnumType.STRING)
    private BillingCycle currentBillingCycle;

    @Column(name = "next_billing_date")
    private Long nextBillingDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrganizationStatus status;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "currency")
    private String currency;

    @Column(name = "language")
    private String language;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "primary_color")
    private String primaryColor;

    @Column(name = "website")
    private String website;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;
}
