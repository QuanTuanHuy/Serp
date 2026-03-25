/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import serp.project.first_mile.enums.PostOfficeStatus;

public record UpdatePostOfficeRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String provinceCode,
        @NotBlank String wardCode,
        @NotBlank String addressDetail,
        @Size(max = 15) String phoneNumber,
        @NotNull @Min(1) Integer serviceRadiusM,
        @NotNull @Min(0) Integer dailyCapacity,
        @NotNull @Min(0) Integer currentLoad,
        @NotNull @Min(0) Integer priority,
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double latitude,
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double longitude,
        @NotNull PostOfficeStatus status,
        Long tenantId
) {
}
