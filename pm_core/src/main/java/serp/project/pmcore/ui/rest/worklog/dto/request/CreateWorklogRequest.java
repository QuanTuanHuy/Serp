/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.worklog.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWorklogRequest {

    @NotNull
    @Min(60)
    private Long timeSpent;

    @NotNull
    private Long startDate;

    @Size(max = 5000)
    private String comment;
}
