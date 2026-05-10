/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workitem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkItemRequest {

    @NotNull(message = "Issue type ID is required")
    private Long issueTypeId;

    @NotBlank(message = "Summary is required")
    @Size(min = 1, max = 512, message = "Summary must be between 1 and 512 characters")
    private String summary;

    @Size(max = 50000, message = "Description must be at most 50000 characters")
    private String description;

    private Long priorityId;
    private Long assigneeId;
    private Long parentId;

    @PositiveOrZero(message = "Start date must be a positive epoch timestamp")
    private Long startDate;

    @PositiveOrZero(message = "Due date must be a positive epoch timestamp")
    private Long dueDate;

    @PositiveOrZero(message = "Original estimate must be greater than or equal to 0")
    private Long timeOriginalEstimate;

    private Long securityLevelId;

    private Map<String, Object> customFields;
}
