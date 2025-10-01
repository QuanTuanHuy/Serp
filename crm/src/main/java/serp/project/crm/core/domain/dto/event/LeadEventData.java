/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.entity.AddressEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadEventData {
    private Long leadId;
    private Long tenantId;
    private String company;
    private String industry;
    private String companySize;
    private String website;
    private String name;
    private String email;
    private String phone;
    private String jobTitle;
    private AddressEntity address;
    private String leadSource;
    private String leadStatus;
    private Long assignedTo;
    private BigDecimal estimatedValue;
    private Integer probability;
    private LocalDate expectedCloseDate;
    private String notes;
    private Long createdBy;
    private Long createdAt;
    private Long updatedBy;
    private Long updatedAt;

    // Additional data for conversion events
    private Long convertedToCustomerId;
    private Long convertedToOpportunityId;
}
