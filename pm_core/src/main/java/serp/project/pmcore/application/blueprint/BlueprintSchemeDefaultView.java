/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint;

import serp.project.pmcore.domain.blueprint.entity.BlueprintSchemeDefaultEntity;

public record BlueprintSchemeDefaultView(
        Long id,
        String schemeType,
        Long schemeId
) {
    public static BlueprintSchemeDefaultView from(BlueprintSchemeDefaultEntity entity) {
        return new BlueprintSchemeDefaultView(
                entity.getId(),
                entity.getSchemeType().name(),
                entity.getSchemeId()
        );
    }
}
