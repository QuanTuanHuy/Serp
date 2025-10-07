/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.ActiveStatus;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CustomerResponse {
    private Long id;
    
    private String name;
    private String industry;
    private String companySize;
    private String website;
    private String phone;
    private String email;
    
    private AddressResponse address;
    
    private String taxId;
    private BigDecimal creditLimit;
    private String paymentTerms;
    private ActiveStatus activeStatus;
    
    private BigDecimal totalRevenue;
    private Integer totalOpportunities;
    private Integer wonOpportunities;
    
    private String notes;
    
    // Metadata
    private Long tenantId;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
