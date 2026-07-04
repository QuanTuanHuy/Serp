/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import serp.project.tms_billing_service.enums.RouteType;

import java.time.LocalDate;

@Data
public class UpsertTariffRequest {
    @NotBlank(message = "INVALID_REQUEST")
    private String serviceCode;

    @NotNull(message = "INVALID_REQUEST")
    private RouteType routeTypeCode;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Double baseWeight;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Double basePrice;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Double stepWeight;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Double stepPrice;

    @NotNull(message = "INVALID_REQUEST")
    private LocalDate effectiveDate;

    private LocalDate expirationDate;
}
