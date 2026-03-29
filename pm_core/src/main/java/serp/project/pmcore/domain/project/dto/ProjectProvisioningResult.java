/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.project.entity.ProjectEntity;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectProvisioningResult {
    private ProjectSchemeBindings resolvedSourceBindings;
    private ProjectSchemeBindings effectiveBindings;

    public void applyEffectiveBindings(ProjectEntity project) {
        if (effectiveBindings != null) {
            effectiveBindings.applyTo(project);
        }
    }
}
