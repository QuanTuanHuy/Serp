/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.dto.request.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.enums.ProvisioningMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    private String name;

    @NotBlank(message = "Project key is required")
    @Pattern(regexp = "^[A-Z][A-Z0-9]{1,9}$",
            message = "Project key must be 2-10 uppercase alphanumeric characters starting with a letter")
    private String key;

    @Size(max = 10000, message = "Description must be at most 10000 characters")
    private String description;

    @NotBlank(message = "Project type key is required")
    @Pattern(regexp = "^(software|business)$",
            message = "Project type must be one of: software, business")
    private String projectTypeKey;

    @NotNull(message = "Lead user ID is required")
    private Long leadUserId;

    private Long categoryId;
    private Long blueprintId;
    private String url;
    private Long avatarId;

    // Optional explicit source scheme IDs. These override blueprint/default resolution when provided.
    private Long issueTypeSchemeId;
    private Long workflowSchemeId;
    private Long fieldConfigSchemeId;
    private Long issueTypeScreenSchemeId;
    private Long permissionSchemeId;
    private Long notificationSchemeId;
    private Long prioritySchemeId;
    private Long issueSecuritySchemeId;

    private ProvisioningMode provisioningMode;
}
