/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.project.ProjectSchemeBindings;

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
