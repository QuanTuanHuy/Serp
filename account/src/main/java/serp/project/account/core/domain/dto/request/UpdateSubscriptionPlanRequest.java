/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateSubscriptionPlanRequest {

    @Size(max = 100, message = "Plan name must not exceed 100 characters")
    private String planName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Monthly price must be >= 0")
    @Digits(integer = 8, fraction = 2, message = "Monthly price must have max 8 integer digits and 2 decimal digits")
    private BigDecimal monthlyPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Yearly price must be >= 0")
    @Digits(integer = 8, fraction = 2, message = "Yearly price must have max 8 integer digits and 2 decimal digits")
    private BigDecimal yearlyPrice;

    @Min(value = 1, message = "Max users must be >= 1 or null for unlimited")
    private Integer maxUsers;

    @Min(value = 0, message = "Trial days must be >= 0")
    @Max(value = 365, message = "Trial days must be <= 365")
    private Integer trialDays;

    private Boolean isActive;

    private Integer displayOrder;

    /**
     * List of modules to include in this plan
     * If provided, will replace existing module mappings
     */
    @Valid
    private List<PlanModuleDto> modules;

    /**
     * Map of feature codes to values
     * If provided, will merge with existing features
     */
    private Map<String, String> features;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class PlanModuleDto {
        @NotNull(message = "Module ID is required")
        private Long moduleId;

        @Builder.Default
        private Boolean isIncluded = true;

        @NotBlank(message = "License type is required")
        private String licenseType;

        private Integer maxUsersPerModule;
    }
}
