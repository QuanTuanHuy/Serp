/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ConvertLeadRequest {
    private Long leadId;
    
    // Opportunity details for conversion
    private String opportunityName;
    private BigDecimal opportunityAmount;
    private String opportunityDescription;
    
    // Customer details (if creating new customer)
    private Boolean createNewCustomer;
    private Long existingCustomerId;
}
