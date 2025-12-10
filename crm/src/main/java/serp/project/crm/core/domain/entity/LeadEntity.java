/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.LeadSource;
import serp.project.crm.core.domain.enums.LeadStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class LeadEntity extends BaseEntity {

    private String company;
    private String industry;
    private String companySize;
    private String website;

    private String name;
    private String email;
    private String phone;
    private String jobTitle;

    private AddressEntity address;

    private LeadSource leadSource;
    private LeadStatus leadStatus;

    private Long assignedTo;
    private BigDecimal estimatedValue;
    private Integer probability;

    private LocalDate expectedCloseDate;
    private String notes;

    private Long convertedOpportunityId;
    private Long convertedCustomerId;

    private static final Map<LeadStatus, Set<LeadStatus>> ALLOWED_STATUS_TRANSITIONS = Map.of(
            LeadStatus.NEW, Set.of(LeadStatus.CONTACTED, LeadStatus.NURTURING, LeadStatus.QUALIFIED,
                    LeadStatus.DISQUALIFIED),
            LeadStatus.CONTACTED, Set.of(LeadStatus.NURTURING, LeadStatus.QUALIFIED, LeadStatus.DISQUALIFIED),
            LeadStatus.NURTURING, Set.of(LeadStatus.QUALIFIED, LeadStatus.DISQUALIFIED),
            LeadStatus.QUALIFIED, Set.of(LeadStatus.CONVERTED, LeadStatus.DISQUALIFIED),
            LeadStatus.DISQUALIFIED, Set.of(),
            LeadStatus.CONVERTED, Set.of());

    // Lead qualification
    public boolean isQualified() {
        return LeadStatus.QUALIFIED.equals(this.leadStatus) ||
                LeadStatus.CONVERTED.equals(this.leadStatus);
    }

    public boolean canBeConverted() {
        return LeadStatus.QUALIFIED.equals(this.leadStatus) &&
                estimatedValue != null &&
                estimatedValue.compareTo(BigDecimal.ZERO) > 0;
    }

    public void qualify(Long qualifiedBy, String notes) {
        if (isQualified()) {
            throw new IllegalStateException("Lead is already qualified or converted.");
        }
        this.leadStatus = LeadStatus.QUALIFIED;
        this.notes = notes;
        this.setUpdatedBy(qualifiedBy);
    }

    public void disqualify(Long disqualifiedBy, String notes) {
        if (LeadStatus.DISQUALIFIED.equals(this.leadStatus)) {
            throw new IllegalStateException("Lead is already disqualified.");
        }
        this.leadStatus = LeadStatus.DISQUALIFIED;
        this.notes = notes;
        this.setUpdatedBy(disqualifiedBy);
    }

    // Lead scoring
    public void updateProbability(Integer probability, Long updatedBy) {
        if (probability < 0 || probability > 100) {
            throw new IllegalArgumentException("Probability must be between 0 and 100.");
        }
        this.probability = probability;
        this.setUpdatedBy(updatedBy);
    }

    // Lead assignment
    public void assignTo(Long salesRepId, Long assignedBy) {
        this.assignedTo = salesRepId;
        this.setUpdatedBy(assignedBy);
    }

    public void markAsConverted(Long convertedBy) {
        if (!canBeConverted()) {
            throw new IllegalStateException(
                    "Lead cannot be converted. Ensure it is qualified and has a valid estimated value.");
        }
        this.leadStatus = LeadStatus.CONVERTED;
        this.setUpdatedBy(convertedBy);
    }

    public void markAsConverted(Long convertedBy, Long opportunityId, Long customerId) {
        if (!canBeConverted()) {
            throw new IllegalStateException(
                    "Lead cannot be converted. Ensure it is qualified and has a valid estimated value.");
        }
        this.leadStatus = LeadStatus.CONVERTED;
        this.convertedOpportunityId = opportunityId;
        this.convertedCustomerId = customerId;
        this.setUpdatedBy(convertedBy);
    }

    // Helper method
    public boolean isOverdue() {
        return expectedCloseDate != null &&
                expectedCloseDate.isBefore(LocalDate.now()) &&
                !isQualified();
    }

    public void updateFrom(LeadEntity updates) {
        if (updates.getName() != null)
            this.name = updates.getName();
        if (updates.getEmail() != null)
            this.email = updates.getEmail();
        if (updates.getPhone() != null)
            this.phone = updates.getPhone();
        if (updates.getCompany() != null)
            this.company = updates.getCompany();
        if (updates.getJobTitle() != null)
            this.jobTitle = updates.getJobTitle();
        if (updates.getLeadSource() != null)
            this.leadSource = updates.getLeadSource();
        if (updates.getIndustry() != null)
            this.industry = updates.getIndustry();
        if (updates.getCompanySize() != null)
            this.companySize = updates.getCompanySize();
        if (updates.getEstimatedValue() != null)
            this.estimatedValue = updates.getEstimatedValue();
        if (updates.getLeadStatus() != null)
            this.updateStatus(updates.getLeadStatus(), updates.getUpdatedBy(), updates.getNotes());
        if (updates.getAssignedTo() != null)
            this.assignedTo = updates.getAssignedTo();
        if (updates.getWebsite() != null)
            this.website = updates.getWebsite();
        if (updates.getAddress() != null)
            this.address = updates.getAddress();
        if (updates.getNotes() != null)
            this.notes = updates.getNotes();
        if (updates.getExpectedCloseDate() != null)
            this.expectedCloseDate = updates.getExpectedCloseDate();
        if (updates.getProbability() != null)
            this.probability = updates.getProbability();
    }

    public void updateStatus(LeadStatus newStatus, Long updatedBy, String notes) {
        if (newStatus == null) {
            return;
        }

        LeadStatus currentStatus = this.leadStatus != null ? this.leadStatus : LeadStatus.NEW;

        if (newStatus.equals(currentStatus)) {
            if (notes != null) {
                this.notes = notes;
            }
            if (updatedBy != null) {
                this.setUpdatedBy(updatedBy);
            }
            return;
        }

        Set<LeadStatus> allowedNextStatuses = ALLOWED_STATUS_TRANSITIONS
                .getOrDefault(currentStatus, Set.of());
        if (!allowedNextStatuses.contains(newStatus)) {
            throw new IllegalStateException(
                    String.format("Invalid lead status transition: %s -> %s", currentStatus, newStatus));
        }

        this.leadStatus = newStatus;
        if (notes != null) {
            this.notes = notes;
        }
        if (updatedBy != null) {
            this.setUpdatedBy(updatedBy);
        }
    }

    public void setDefaults() {
        if (this.leadStatus == null) {
            this.leadStatus = LeadStatus.NEW;
        }
        if (this.probability == null) {
            this.probability = 0;
        }
    }
}
