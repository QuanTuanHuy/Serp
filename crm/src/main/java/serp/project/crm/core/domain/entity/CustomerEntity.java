/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.ActiveStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class CustomerEntity extends BaseEntity {
    private String name;

    private String phone;
    private String email;
    private String website;
    private String industry;
    private String companySize;

    private Long parentCustomerId;

    private String taxId;
    private BigDecimal creditLimit;

    private Integer totalOpportunities;
    private Integer wonOpportunities;
    private BigDecimal totalRevenue;

    private ActiveStatus activeStatus;
    private String notes;

    private AddressEntity address;

    private List<ContactEntity> contacts;

    // Status management
    public boolean isActive() {
        return ActiveStatus.ACTIVE.equals(this.activeStatus);
    }

    public void activate(Long activatedBy) {
        if (isActive()) {
            throw new IllegalStateException("Customer is already active.");
        }
        this.activeStatus = ActiveStatus.ACTIVE;
        this.setUpdatedBy(activatedBy);
    }

    public void deactivate(Long deactivatedBy) {
        if (!isActive()) {
            throw new IllegalStateException("Customer is already inactive.");
        }
        this.activeStatus = ActiveStatus.INACTIVE;
        this.setUpdatedBy(deactivatedBy);
    }

    // Financial updates
    public void updateCreditLimit(BigDecimal newLimit, Long updatedBy) {
        if (newLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Credit limit cannot be negative.");
        }
        this.creditLimit = newLimit;
        this.setUpdatedBy(updatedBy);
    }

    // Opportunity tracking
    public void recordOpportunityResult(boolean won, BigDecimal revenue, Long updatedBy) {
        this.totalOpportunities = (this.totalOpportunities == null ? 0 : this.totalOpportunities) + 1;
        if (won) {
            this.wonOpportunities = (this.wonOpportunities == null ? 0 : this.wonOpportunities) + 1;
            this.totalRevenue = (this.totalRevenue == null ? BigDecimal.ZERO : this.totalRevenue).add(revenue);
        }
        this.setUpdatedBy(updatedBy);
    }

}
