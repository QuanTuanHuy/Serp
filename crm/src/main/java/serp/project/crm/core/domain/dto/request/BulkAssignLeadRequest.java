/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BulkAssignLeadRequest {
    @NotEmpty(message = "Lead IDs are required")
    private List<@NotNull(message = "Lead ID is required") Long> leadIds;

    @NotNull(message = "Assigned user is required")
    private Long assignedTo;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
