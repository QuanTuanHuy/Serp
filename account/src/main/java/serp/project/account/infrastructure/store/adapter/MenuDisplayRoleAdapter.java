/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<MenuDisplayRoleEntity> getByMenuDisplayIds(List<Long> menuDisplayIds) {
        return menuDisplayRoleMapper.toEntityList(
                menuDisplayRoleRepository.findByMenuDisplayIdIn(menuDisplayIds));
    }

    @Override
    public List<MenuDisplayRoleEntity> getByRoleIdAndMenuDisplayIds(Long roleId, List<Long> menuDisplayIds) {
        return menuDisplayRoleMapper.toEntityList(
                menuDisplayRoleRepository.findByRoleIdAndMenuDisplayIdIn(roleId, menuDisplayIds));
    }

    @Override
    public void deleteByRoleIdAndMenuDisplayIds(Long roleId, List<Long> menuDisplayIds) {
        if (menuDisplayIds == null || menuDisplayIds.isEmpty()) {
            return;
        }

        String placeholders = menuDisplayIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "DELETE FROM menu_display_roles WHERE role_id = ? AND menu_display_id IN (" + placeholders + ")";

        Object[] params = new Object[1 + menuDisplayIds.size()];
        params[0] = roleId;
        for (int i = 0; i < menuDisplayIds.size(); i++) {
            params[i + 1] = menuDisplayIds.get(i);
        }

        jdbcTemplate.update(sql, params);
    }
}
