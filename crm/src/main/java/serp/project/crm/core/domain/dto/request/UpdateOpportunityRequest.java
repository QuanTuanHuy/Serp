/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.constraints.*;
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
public class UpdateOpportunityRequest {
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private OpportunityStage stage;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Estimated value must be greater than 0")
    private BigDecimal estimatedValue;
    
    @Min(value = 0, message = "Probability must be at least 0")
    @Max(value = 100, message = "Probability must not exceed 100")
    private Integer probability;
    
    @Future(message = "Expected close date must be in the future")
    private LocalDate expectedCloseDate;
    
    private Long assignedTo;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
