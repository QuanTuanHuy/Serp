/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.PermissionSchemeEntryEntity;
import serp.project.pmcore.domain.port.store.IPermissionSchemeEntryPort;
import serp.project.pmcore.infrastructure.store.mapper.PermissionSchemeEntryMapper;
import serp.project.pmcore.infrastructure.store.repository.IPermissionSchemeEntryRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionSchemeEntryAdapter implements IPermissionSchemeEntryPort {

    private final IPermissionSchemeEntryRepository permissionSchemeEntryRepository;
    private final PermissionSchemeEntryMapper permissionSchemeEntryMapper;

    @Override
    public List<PermissionSchemeEntryEntity> createPermissionSchemeEntries(List<PermissionSchemeEntryEntity> entries) {
        if (entries == null || entries.isEmpty()) {
            return new ArrayList<>();
        }
        return permissionSchemeEntryMapper.toEntities(
                permissionSchemeEntryRepository.saveAll(permissionSchemeEntryMapper.toModels(entries))
        );
    }

    @Override
    public List<PermissionSchemeEntryEntity> getPermissionSchemeEntriesBySchemeIdIncludingSystem(Long schemeId, Long tenantId) {
        return permissionSchemeEntryMapper.toEntities(
                permissionSchemeEntryRepository.findAllBySchemeIdAndTenantIdOrSystemTenant(schemeId, tenantId)
        );
    }

    @Override
    public List<PermissionSchemeEntryEntity> getPermissionSchemeEntriesBySchemeId(Long schemeId, Long tenantId) {
        return permissionSchemeEntryMapper.toEntities(
                permissionSchemeEntryRepository.findAllBySchemeIdAndTenantId(schemeId, tenantId)
        );
    }
}
