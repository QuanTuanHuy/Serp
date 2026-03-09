/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.BlueprintSchemeDefaultEntity;

import java.util.List;

public interface IBlueprintSchemeDefaultPort {
    List<BlueprintSchemeDefaultEntity> getDefaultsByBlueprintId(Long blueprintId, Long tenantId);
    
    List<BlueprintSchemeDefaultEntity> getDefaultsByBlueprintIdIncludingSystem(Long blueprintId, Long tenantId);
}
