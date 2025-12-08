/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.OpportunityStage;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpportunityResponse {
    private Long id;
    
    private String name;
    private String description;
    
    private Long leadId;
    private Long customerId;
    
    private OpportunityStage stage;
    private BigDecimal estimatedValue;
    private Integer probability;
    private LocalDate expectedCloseDate;
    private LocalDate actualCloseDate;
    private Long assignedTo;
    private String notes;
    private String lossReason;
    
    // Metadata
    private Long tenantId;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
