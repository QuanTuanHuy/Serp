/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpsertChargeableWeightConfigRequest {
    @NotBlank(message = "INVALID_REQUEST")
    private String serviceCode;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Long minDimensionCm;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Long smallBulkyThresholdCm;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Long baseWeightGram;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Long stepWeightGram;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Long maxWeightGram;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Double volumetricDivisor;
}
