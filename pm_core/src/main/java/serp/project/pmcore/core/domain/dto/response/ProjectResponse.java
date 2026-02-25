/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String key;
    private String name;
    private String description;
    private String url;
    private Long leadUserId;
    private Long avatarId;
    private Long categoryId;
    private String projectTypeKey;
    private Boolean isArchived;
    private Long archivedAt;

    // Scheme bindings
    private Long issueTypeSchemeId;
    private Long workflowSchemeId;
    private Long fieldConfigSchemeId;
    private Long issueTypeScreenSchemeId;
    private Long permissionSchemeId;
    private Long notificationSchemeId;
    private Long prioritySchemeId;
    private Long issueSecuritySchemeId;

    private Long createdAt;
    private Long createdBy;
    private Long updatedAt;
    private Long updatedBy;
}
