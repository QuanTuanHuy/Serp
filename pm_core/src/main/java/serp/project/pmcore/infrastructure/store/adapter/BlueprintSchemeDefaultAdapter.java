/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.BlueprintSchemeDefaultEntity;
import serp.project.pmcore.core.port.store.IBlueprintSchemeDefaultPort;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlueprintSchemeDefaultAdapter implements IBlueprintSchemeDefaultPort {

    @Override
    public List<BlueprintSchemeDefaultEntity> getDefaultsByBlueprintId(Long blueprintId, Long tenantId) {
        log.warn("BlueprintSchemeDefaultAdapter.getDefaultsByBlueprintId() is a stub — returning empty list. " +
                "Implement when blueprint infrastructure is built.");
        return Collections.emptyList();
    }

    @Override
    public List<BlueprintSchemeDefaultEntity> getDefaultsByBlueprintIdIncludingSystem(Long blueprintId, Long tenantId) {
        log.warn("BlueprintSchemeDefaultAdapter.getDefaultsByBlueprintIdIncludingSystem() is a stub — returning empty list. " +
                "Implement when blueprint infrastructure is built.");
        return Collections.emptyList();
    }
}
