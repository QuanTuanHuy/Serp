/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull private Long tenantId;
    @NotNull private Long userId;
    @NotNull private Long projectId;
    @NotBlank private String projectKey;
    private Long blueprintId;
    @NotNull private ProvisioningMode provisioningMode;
    private ProjectSchemeBindings requestedSchemeBindings;

    public ProvisioningMode getEffectiveProvisioningMode() {
        return ProvisioningMode.defaultForCreate(provisioningMode);
    }
}
