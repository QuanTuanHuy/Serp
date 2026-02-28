/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Pattern(regexp = "^(software|business|service_desk)$",
            message = "Project type must be one of: software, business, service_desk")
    private String projectTypeKey;

    @NotNull(message = "Lead user ID is required")
    private Long leadUserId;

    private Long categoryId;
    private Long blueprintId;
    private String url;
    private Long avatarId;

    // Explicit scheme overrides (optional — if not provided, resolved from blueprint/system defaults)
    private Long issueTypeSchemeId;
    private Long workflowSchemeId;
    private Long fieldConfigSchemeId;
    private Long issueTypeScreenSchemeId;
    private Long permissionSchemeId;
    private Long notificationSchemeId;
    private Long prioritySchemeId;
    private Long issueSecuritySchemeId;
}
