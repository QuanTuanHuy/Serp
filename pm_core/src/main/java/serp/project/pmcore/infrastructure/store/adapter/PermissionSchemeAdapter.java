/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.PermissionSchemeEntity;
import serp.project.pmcore.domain.port.store.IPermissionSchemePort;
import serp.project.pmcore.infrastructure.store.mapper.PermissionSchemeMapper;
import serp.project.pmcore.infrastructure.store.repository.IPermissionSchemeRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PermissionSchemeAdapter implements IPermissionSchemePort {

    private final IPermissionSchemeRepository permissionSchemeRepository;
    private final PermissionSchemeMapper permissionSchemeMapper;

    @Override
    public Optional<PermissionSchemeEntity> getPermissionSchemeById(Long schemeId, Long tenantId) {
        return permissionSchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(permissionSchemeMapper::toEntity);
    }

    @Override
    public Optional<PermissionSchemeEntity> getPermissionSchemeByIdIncludingSystem(Long schemeId, Long tenantId) {
        return permissionSchemeRepository.findByIdAndTenantIdOrSystemTenant(schemeId, tenantId)
                .map(permissionSchemeMapper::toEntity);
    }
}
