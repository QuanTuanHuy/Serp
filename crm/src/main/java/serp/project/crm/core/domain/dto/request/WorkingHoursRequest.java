/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WorkingHoursRequest {
    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Working day flag is required")
    private Boolean workingDay;

    @Min(value = 0, message = "Start minute must be at least 0")
    @Max(value = 1439, message = "Start minute must not exceed 1439")
    private Integer startMinute;

    @Min(value = 0, message = "End minute must be at least 0")
    @Max(value = 1440, message = "End minute must not exceed 1440")
    private Integer endMinute;
}
