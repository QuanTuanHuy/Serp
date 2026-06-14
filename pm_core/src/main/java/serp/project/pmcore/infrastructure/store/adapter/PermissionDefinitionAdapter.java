/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.permission.entity.PermissionDefinitionEntity;
import serp.project.pmcore.domain.permission.port.IPermissionDefinitionPort;
import serp.project.pmcore.infrastructure.store.mapper.PermissionDefinitionMapper;
import serp.project.pmcore.infrastructure.store.repository.IPermissionDefinitionRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionDefinitionAdapter implements IPermissionDefinitionPort {

    private final IPermissionDefinitionRepository permissionDefinitionRepository;
    private final PermissionDefinitionMapper permissionDefinitionMapper;

    @Override
    public List<PermissionDefinitionEntity> getPermissionDefinitionsIncludingSystem(Long tenantId) {
        return permissionDefinitionMapper.toEntities(
                permissionDefinitionRepository.findAllByTenantIdOrSystemTenant(tenantId)
        );
    }
}
