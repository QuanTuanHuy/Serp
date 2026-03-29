/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.blueprint.port;

import java.util.List;

import serp.project.pmcore.domain.blueprint.entity.BlueprintSchemeDefaultEntity;

public interface IBlueprintSchemeDefaultPort {
    List<BlueprintSchemeDefaultEntity> getDefaultsByBlueprintId(Long blueprintId, Long tenantId);
    
    List<BlueprintSchemeDefaultEntity> getDefaultsByBlueprintIdIncludingSystem(Long blueprintId, Long tenantId);
}
