/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_customers_email", columnList = "email"),
        @Index(name = "idx_customers_name", columnList = "name")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class CustomerModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "company_size", length = 50)
    private String companySize;

    @Column(name = "parent_customer_id")
    private Long parentCustomerId;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "total_opportunities")
    private Integer totalOpportunities;

    @Column(name = "won_opportunities")
    private Integer wonOpportunities;

    @Column(name = "total_revenue", precision = 15, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "active_status", nullable = false, length = 20)
    private String activeStatus;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "address_street", length = 255)
    private String addressStreet;

    @Column(name = "address_city", length = 100)
    private String addressCity;

    @Column(name = "address_state", length = 100)
    private String addressState;

    @Column(name = "address_zip_code", length = 20)
    private String addressZipCode;

    @Column(name = "address_country", length = 100)
    private String addressCountry;
}
