/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ConvertLeadRequest {
    private Long leadId;

    @Builder.Default
    private Boolean createOpportunity = true;

    @Builder.Default
    private Boolean createAccount = true;

    private Long existingAccountId;

    @Valid
    private OpportunityData opportunityData;

    @Valid
    private AccountData accountData;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class OpportunityData {
        @Size(max = 255, message = "Opportunity name must not exceed 255 characters")
        private String name;

        @DecimalMin(value = "0.0", inclusive = false, message = "Opportunity amount must be greater than 0")
        private BigDecimal amount;

        private LocalDate expectedCloseDate;

        @Size(max = 1000, message = "Opportunity notes must not exceed 1000 characters")
        private String notes;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class AccountData {
        @Size(max = 255, message = "Account name must not exceed 255 characters")
        private String name;

        @DecimalMin(value = "0.0", message = "Credit limit must be greater than or equal to 0")
        private BigDecimal creditLimit;

        @Size(max = 2000, message = "Notes must not exceed 2000 characters")
        private String notes;
    }
}
