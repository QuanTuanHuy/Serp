/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.MenuDisplayRoleEntity;
import serp.project.account.core.port.store.IMenuDisplayRolePort;
import serp.project.account.infrastructure.store.mapper.MenuDisplayRoleMapper;
import serp.project.account.infrastructure.store.model.MenuDisplayRoleModel;
import serp.project.account.infrastructure.store.repository.IMenuDisplayRoleRepository;

@Component
@RequiredArgsConstructor
public class MenuDisplayRoleAdapter implements IMenuDisplayRolePort {
    private final IMenuDisplayRoleRepository menuDisplayRoleRepository;
    private final MenuDisplayRoleMapper menuDisplayRoleMapper;

    @Override
    public void save(List<MenuDisplayRoleEntity> menuDisplayRoles) {
        List<MenuDisplayRoleModel> models = menuDisplayRoleMapper.toModelList(menuDisplayRoles);
        menuDisplayRoleRepository.saveAll(models);
    }

    @Override
    public List<MenuDisplayRoleEntity> getByRoleIds(List<Long> roleIds) {
        return menuDisplayRoleMapper.toEntityList(menuDisplayRoleRepository.findByRoleIdIn(roleIds));
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        menuDisplayRoleRepository.deleteByIdIn(ids);
    }
}
