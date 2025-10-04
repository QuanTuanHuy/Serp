/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.OpportunityStage;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateOpportunityRequest {
    private String name;
    private String description;
    
    private Long leadId;
    private Long customerId;
    
    private OpportunityStage stage;
    private BigDecimal estimatedValue;
    private LocalDate expectedCloseDate;
    private Long assignedTo;
    private String notes;
}
