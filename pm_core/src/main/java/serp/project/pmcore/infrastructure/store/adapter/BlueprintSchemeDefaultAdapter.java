/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.BlueprintSchemeDefaultEntity;
import serp.project.pmcore.core.port.store.IBlueprintSchemeDefaultPort;
import serp.project.pmcore.infrastructure.store.mapper.BlueprintSchemeDefaultMapper;
import serp.project.pmcore.infrastructure.store.repository.IBlueprintSchemeDefaultRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BlueprintSchemeDefaultAdapter implements IBlueprintSchemeDefaultPort {

    private final IBlueprintSchemeDefaultRepository blueprintSchemeDefaultRepository;
    private final BlueprintSchemeDefaultMapper blueprintSchemeDefaultMapper;

    @Override
    public List<BlueprintSchemeDefaultEntity> getDefaultsByBlueprintId(Long blueprintId, Long tenantId) {
        return blueprintSchemeDefaultMapper.toEntities(
                blueprintSchemeDefaultRepository.findAllByBlueprintIdAndTenantId(blueprintId, tenantId)
        );
    }

    @Override
    public List<BlueprintSchemeDefaultEntity> getDefaultsByBlueprintIdIncludingSystem(Long blueprintId, Long tenantId) {
        return blueprintSchemeDefaultMapper.toEntities(
                blueprintSchemeDefaultRepository.findAllByBlueprintIdAndTenantIdOrSystemTenant(blueprintId, tenantId)
        );
    }
}
