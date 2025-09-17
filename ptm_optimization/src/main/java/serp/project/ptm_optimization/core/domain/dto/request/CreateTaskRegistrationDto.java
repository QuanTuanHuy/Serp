/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.ptm_optimization.core.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTaskRegistrationDto {
    @NotNull
    @Min(0)
    private Double sleepDuration;
    @NotBlank
    private String startSleepTime;
    @NotBlank
    private String endSleepTime;
    @Min(0)
    @NotNull
    private Double relaxTime;
    @Min(0)
    @NotNull
    private Double travelTime;
    @Min(0)
    @NotNull
    private Double eatTime;
    @Min(0)
    @NotNull
    private Double workTime;
}
