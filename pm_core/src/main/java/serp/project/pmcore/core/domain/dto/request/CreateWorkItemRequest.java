/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkItemRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Issue type ID is required")
    private Long issueTypeId;

    @NotBlank(message = "Summary is required")
    @Size(min = 1, max = 500, message = "Summary must be between 1 and 500 characters")
    private String summary;

    @Size(max = 50000, message = "Description must be at most 50000 characters")
    private String description;

    private Long statusId;
    private Long priorityId;
    private Long assigneeId;
    private Long parentId;
    private Long dueDate;
    private Long timeOriginalEstimate;
}
