/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.blueprint.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectBlueprintRequest {

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must be at most 255 characters")
    private String name;

    @Size(max = 2000, message = "description must be at most 2000 characters")
    private String description;

    @NotBlank(message = "projectTypeKey is required")
    @Size(max = 50, message = "projectTypeKey must be at most 50 characters")
    private String projectTypeKey;

    @Size(max = 255, message = "avatarUrl must be at most 255 characters")
    private String avatarUrl;
}
