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

@Entity
@Table(name = "contacts", indexes = {
        @Index(name = "idx_contacts_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_contacts_customer_id", columnList = "customer_id"),
        @Index(name = "idx_contacts_email", columnList = "email"),
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ContactModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "job_position", length = 100)
    private String jobPosition;

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

    @Column(name = "contact_type", length = 50)
    private String contactType;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "linkedin_url", length = 255)
    private String linkedInUrl;

    @Column(name = "twitter_handle", length = 100)
    private String twitterHandle;

    @Column(name = "active_status", nullable = false, length = 20)
    private String activeStatus;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
