/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CloseOpportunityWonRequest {
    @DecimalMin(value = "0.0", inclusive = true, message = "Actual value must be greater than or equal to 0")
    private BigDecimal actualValue;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
