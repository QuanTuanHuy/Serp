/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.RolePermissionEntity;
import serp.project.account.core.port.store.IRolePermission;
import serp.project.account.infrastructure.store.mapper.RolePermissionMapper;
import serp.project.account.infrastructure.store.model.RolePermissionModel;
import serp.project.account.infrastructure.store.repository.IRolePermissionRepository;

@Component
@RequiredArgsConstructor
public class RolePermissionAdapter implements IRolePermission {
    private final IRolePermissionRepository rolePermissionRepository;
    private final RolePermissionMapper rolePermissionMapper;
    
    @Override
    public List<RolePermissionEntity> saveAll(List<RolePermissionEntity> rolePermissions) {
        List<RolePermissionModel> models = rolePermissionMapper.toModelList(rolePermissions);
        return rolePermissionMapper.toEntityList(rolePermissionRepository.saveAll(models));
    }

    @Override
    public List<RolePermissionEntity> getRolePermissionsByRoleId(Long roleId) {
        return rolePermissionMapper.toEntityList(rolePermissionRepository.findByRoleId(roleId));
    }
}
