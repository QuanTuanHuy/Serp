/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workitem.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionWorkItemStatusRequest {

    @NotNull(message = "Transition ID is required")
    @Positive(message = "Transition ID must be a positive number")
    private Long transitionId;

    @Positive(message = "Resolution ID must be a positive number")
    private Long resolutionId;

    private Map<String, Object> fields;
}
