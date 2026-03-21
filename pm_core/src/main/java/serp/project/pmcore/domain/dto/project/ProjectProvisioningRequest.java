/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.entity.project.ProjectSchemeBindings;
import serp.project.pmcore.domain.enums.ProvisioningMode;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectProvisioningRequest {
    private Long tenantId;
    private Long userId;
    private Long projectId;
    private String projectKey;
    private Long blueprintId;
    private ProvisioningMode provisioningMode;
    private ProjectSchemeBindings requestedSchemeBindings;

    public ProvisioningMode getEffectiveProvisioningMode() {
        return ProvisioningMode.defaultForCreate(provisioningMode);
    }
}
