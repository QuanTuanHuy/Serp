/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.status.dto.request;

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
public class CreateStatusRequest {

    @NotBlank(message = "statusKey is required")
    @Size(max = 100, message = "statusKey must be at most 100 characters")
    private String statusKey;

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must be at most 255 characters")
    private String name;

    @Size(max = 2000, message = "description must be at most 2000 characters")
    private String description;

    @Size(max = 255, message = "iconUrl must be at most 255 characters")
    private String iconUrl;

    @NotNull(message = "statusCategoryId is required")
    private Long statusCategoryId;
}
