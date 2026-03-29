/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.shared.enums.ProvisioningMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectSchemesRequest {

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
