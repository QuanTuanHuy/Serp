/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
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

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(List<MenuDisplayRoleEntity> menuDisplayRoles) {
        List<MenuDisplayRoleModel> models = menuDisplayRoleMapper.toModelList(menuDisplayRoles);
        String sql = """
                INSERT INTO menu_display_roles (menu_display_id, role_id, created_at, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (menu_display_id, role_id) DO NOTHING
                """;
        jdbcTemplate.batchUpdate(sql, models, models.size(), (ps, argument) -> {
            ps.setLong(1, argument.getMenuDisplayId());
            ps.setLong(2, argument.getRoleId());
            ps.setObject(3, argument.getCreatedAt());
            ps.setObject(4, argument.getUpdatedAt());
        });
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
