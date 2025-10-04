/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.ContactType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ContactEntity extends BaseEntity {
    private String name;
    private String email;
    private String phone;
    private String jobPosition;
    
    private Long customerId;
    private Boolean isPrimary;
    
    private AddressEntity address;
    private ContactType contactType;
    private ActiveStatus activeStatus;
    
    private String linkedInUrl;
    private String twitterHandle;
    
    private String notes;
    
    // Business logic
    public boolean isActive() {
        return ActiveStatus.ACTIVE.equals(this.activeStatus);
    }
    
    public void activate(Long activatedBy) {
        if (isActive()) {
            throw new IllegalStateException("Contact is already active.");
        }
        this.activeStatus = ActiveStatus.ACTIVE;
        this.setUpdatedBy(activatedBy);
    }
    
    public void deactivate(Long deactivatedBy) {
        if (!isActive()) {
            throw new IllegalStateException("Contact is already inactive.");
        }
        this.activeStatus = ActiveStatus.INACTIVE;
        this.setUpdatedBy(deactivatedBy);
    }
    
    public void setPrimaryContact(Long updatedBy) {
        this.isPrimary = true;
        this.setUpdatedBy(updatedBy);
    }
    
    public void removePrimaryStatus(Long updatedBy) {
        this.isPrimary = false;
        this.setUpdatedBy(updatedBy);
    }

    // ========== Update Methods ==========

    /**
     * Update contact fields from another contact (for PATCH operations)
     */
    public void updateFrom(ContactEntity updates) {
        if (updates.getName() != null) this.name = updates.getName();
        if (updates.getEmail() != null) this.email = updates.getEmail();
        if (updates.getPhone() != null) this.phone = updates.getPhone();
        if (updates.getJobPosition() != null) this.jobPosition = updates.getJobPosition();
        if (updates.getContactType() != null) this.contactType = updates.getContactType();
        if (updates.getActiveStatus() != null) this.activeStatus = updates.getActiveStatus();
        if (updates.getAddress() != null) this.address = updates.getAddress();
        if (updates.getLinkedInUrl() != null) this.linkedInUrl = updates.getLinkedInUrl();
        if (updates.getTwitterHandle() != null) this.twitterHandle = updates.getTwitterHandle();
        if (updates.getNotes() != null) this.notes = updates.getNotes();
    }

    /**
     * Set default values for new contact
     */
    public void setDefaults() {
        if (this.activeStatus == null) {
            this.activeStatus = ActiveStatus.ACTIVE;
        }
        if (this.isPrimary == null) {
            this.isPrimary = false;
        }
        if (this.contactType == null) {
            this.contactType = ContactType.INDIVIDUAL;
        }
    }
}
