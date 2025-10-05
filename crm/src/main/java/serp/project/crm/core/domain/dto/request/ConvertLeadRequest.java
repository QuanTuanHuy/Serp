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

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ConvertLeadRequest {
    private Long leadId;

    @NotBlank(message = "Opportunity name is required")
    @Size(max = 255, message = "Opportunity name must not exceed 255 characters")
    private String opportunityName;

    @DecimalMin(value = "0.0", inclusive = false, message = "Opportunity amount must be greater than 0")
    private BigDecimal opportunityAmount;

    @Size(max = 1000, message = "Opportunity description must not exceed 1000 characters")
    private String opportunityDescription;

    @NotNull(message = "Create new customer flag is required")
    private Boolean createNewCustomer;

    private Long existingCustomerId;
}
