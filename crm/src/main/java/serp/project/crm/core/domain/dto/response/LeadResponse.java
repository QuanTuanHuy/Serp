/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.LeadSource;
import serp.project.crm.core.domain.enums.LeadStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LeadResponse {
    private Long id;
    
    // Company information
    private String company;
    private String industry;
    private String companySize;
    private String website;

    // Contact information
    private String name;
    private String email;
    private String phone;
    private String jobTitle;

    // Address
    private AddressResponse address;

    // Lead details
    private LeadSource leadSource;
    private LeadStatus leadStatus;
    private Long assignedTo;
    private BigDecimal estimatedValue;
    private Integer probability;
    private LocalDate expectedCloseDate;
    private String notes;

    // Metadata
    private Long tenantId;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
