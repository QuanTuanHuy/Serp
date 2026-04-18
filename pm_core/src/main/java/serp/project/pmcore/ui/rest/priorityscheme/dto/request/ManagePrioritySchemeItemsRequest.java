/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.priorityscheme.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagePrioritySchemeItemsRequest {

    @NotEmpty(message = "priorityIds must not be empty")
    @Size(max = 100, message = "priorityIds must contain at most 100 items")
    private List<@NotNull(message = "priorityIds must not contain null values")
            @Min(value = 1, message = "priorityIds must contain only positive values") Long> priorityIds;
}
