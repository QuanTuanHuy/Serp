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
    private Long accountId;
    
    private OpportunityStage stage;
    private BigDecimal estimatedValue;
    private BigDecimal actualValue;
    private Integer probability;
    private LocalDate expectedCloseDate;
    private LocalDate actualCloseDate;
    private Long assignedTo;
    private String accountName;
    private String assignedToName;
    private String leadName;
    private Long lastActivityAt;
    private Long nextActivityAt;
    private Integer openActivityCount;
    private Integer overdueActivityCount;
    private String notes;
    private String lossReason;
    private String reopenReason;
    
    // Metadata
    private Long tenantId;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
