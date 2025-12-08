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
import java.time.LocalDate;

@Entity
@Table(name = "leads", indexes = {
        @Index(name = "idx_leads_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_leads_lead_status", columnList = "lead_status"),
        @Index(name = "idx_leads_assigned_to", columnList = "assigned_to")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class LeadModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "company", nullable = false, length = 255)
    private String company;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "company_size", length = 50)
    private String companySize;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

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

    @Column(name = "lead_source", nullable = false, length = 50)
    private String leadSource;

    @Column(name = "lead_status", nullable = false, length = 50)
    private String leadStatus;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "estimated_value", precision = 15, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "probability")
    private Integer probability;

    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "converted_opportunity_id")
    private Long convertedOpportunityId;

    @Column(name = "converted_customer_id")
    private Long convertedCustomerId;
}
