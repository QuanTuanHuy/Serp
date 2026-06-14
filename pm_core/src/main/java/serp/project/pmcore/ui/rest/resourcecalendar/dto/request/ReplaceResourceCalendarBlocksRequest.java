/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.resourcecalendar.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplaceResourceCalendarBlocksRequest {
    @NotNull(message = "blocks is required")
    private List<@Valid BlockRequest> blocks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockRequest {
        @NotNull(message = "dayOfWeek is required")
        @Min(value = 1, message = "dayOfWeek must be between 1 and 7")
        @Max(value = 7, message = "dayOfWeek must be between 1 and 7")
        private Integer dayOfWeek;

        @NotNull(message = "startTime is required")
        private LocalTime startTime;

        @NotNull(message = "endTime is required")
        private LocalTime endTime;

        @NotNull(message = "capacityFactor is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "capacityFactor must be greater than 0")
        @DecimalMax(value = "1.0", message = "capacityFactor must be no more than 1")
        private BigDecimal capacityFactor;
    }
}
