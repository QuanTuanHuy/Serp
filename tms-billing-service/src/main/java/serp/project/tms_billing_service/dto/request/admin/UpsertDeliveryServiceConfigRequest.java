/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpsertDeliveryServiceConfigRequest {
    @NotBlank(message = "INVALID_REQUEST")
    private String serviceCode;

    @NotBlank(message = "INVALID_REQUEST")
    private String name;

    private String description;

    @NotNull(message = "INVALID_REQUEST")
    private Boolean active;

    @NotNull(message = "INVALID_REQUEST")
    @PositiveOrZero(message = "INVALID_REQUEST")
    private Integer sortOrder;
}
