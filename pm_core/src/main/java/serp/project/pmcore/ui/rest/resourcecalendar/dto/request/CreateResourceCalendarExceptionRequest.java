/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.resourcecalendar.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarExceptionType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateResourceCalendarExceptionRequest {
    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "exceptionType is required")
    private ResourceCalendarExceptionType exceptionType;

    @NotNull(message = "startAt is required")
    private Long startAt;

    @NotNull(message = "endAt is required")
    private Long endAt;

    @DecimalMin(value = "0.0", message = "capacityFactor must be at least 0")
    @DecimalMax(value = "2.0", message = "capacityFactor must be no more than 2")
    private BigDecimal capacityFactor;

    @Size(max = 500, message = "reason must be at most 500 characters")
    private String reason;
}
