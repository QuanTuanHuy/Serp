/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.issuetypescheme.dto.request;

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
public class ManageIssueTypeSchemeItemsRequest {

    @NotEmpty(message = "issueTypeIds must not be empty")
    @Size(max = 100, message = "issueTypeIds must contain at most 100 items")
    private List<@NotNull(message = "issueTypeIds must not contain null values")
            @Min(value = 1, message = "issueTypeIds must contain only positive values") Long> issueTypeIds;
}
