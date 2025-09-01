/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.PermissionEntity;
import serp.project.account.core.port.store.IPermissionPort;
import serp.project.account.infrastructure.store.mapper.PermissionMapper;
import serp.project.account.infrastructure.store.model.PermissionModel;
import serp.project.account.infrastructure.store.repository.IPermissionRepository;

@Component
@RequiredArgsConstructor
public class PermissionAdapter implements IPermissionPort {
    private final IPermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    
    @Override
    public PermissionEntity save(PermissionEntity permission) {
        PermissionModel permissionModel = permissionMapper.toModel(permission);
        return permissionMapper.toEntity(permissionRepository.save(permissionModel));
    }

    @Override
    public PermissionEntity getPermissionByName(String name) {
        return permissionRepository.findByName(name)
                .map(permissionMapper::toEntity)
                .orElse(null);
    }

    @Override
    public List<PermissionEntity> getAllPermissions() {
        return permissionMapper.toEntityList(permissionRepository.findAll());
    }

    @Override
    public List<PermissionEntity> getPermissionsByIds(List<Long> ids) {
        return permissionMapper.toEntityList(permissionRepository.findByIdIn(ids));
    }
}
