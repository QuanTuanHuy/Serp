/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.port.store.IRolePort;
import serp.project.account.infrastructure.store.mapper.RoleMapper;
import serp.project.account.infrastructure.store.model.RoleModel;
import serp.project.account.infrastructure.store.repository.IRoleRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleAdapter implements IRolePort {
    private final IRoleRepository roleRepository;
    private final RoleMapper roleMapper;
    
    @Override
    public RoleEntity save(RoleEntity role) {
        RoleModel roleModel = roleMapper.toModel(role);
        return roleMapper.toEntity(roleRepository.save(roleModel));
    }

    @Override
    public RoleEntity getRoleByName(String name) {
        return roleRepository.findByName(name)
                .map(roleMapper::toEntity)
                .orElse(null);
    }

    @Override
    public List<RoleEntity> getAllRoles() {
        return roleMapper.toEntityList(roleRepository.findAll());
    }

    @Override
    public List<RoleEntity> getRolesByIds(List<Long> roleIds) {
        return roleMapper.toEntityList(roleRepository.findByIdIn(roleIds));
    }
}
