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
public class CreateSubscriptionPlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name must not exceed 100 characters")
    private String planName;

    @NotBlank(message = "Plan code is required")
    @Size(max = 50, message = "Plan code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Plan code must be uppercase alphanumeric with underscores")
    private String planCode;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Monthly price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Monthly price must be >= 0")
    @Digits(integer = 8, fraction = 2, message = "Monthly price must have max 8 integer digits and 2 decimal digits")
    private BigDecimal monthlyPrice;

    @NotNull(message = "Yearly price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Yearly price must be >= 0")
    @Digits(integer = 8, fraction = 2, message = "Yearly price must have max 8 integer digits and 2 decimal digits")
    private BigDecimal yearlyPrice;

    @Min(value = 1, message = "Max users must be >= 1 or null for unlimited")
    private Integer maxUsers;

    @NotNull(message = "Trial days is required")
    @Min(value = 0, message = "Trial days must be >= 0")
    @Max(value = 365, message = "Trial days must be <= 365")
    private Integer trialDays;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isCustom = false;

    /**
     * Organization ID for custom plans only
     * Required when isCustom = true, must be null when isCustom = false
     */
    private Long organizationId;

    @Builder.Default
    private Integer displayOrder = 0;

    @Valid
    private List<PlanModuleDto> modules;

    /**
     * Map of feature codes to values
     * Example: {"MAX_STORAGE_GB": "100", "API_CALLS_PER_DAY": "10000"}
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
