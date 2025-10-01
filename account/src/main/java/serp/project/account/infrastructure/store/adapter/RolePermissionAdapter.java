/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.RolePermissionEntity;
import serp.project.account.core.port.store.IRolePermissionPort;
import serp.project.account.infrastructure.store.mapper.RolePermissionMapper;
import serp.project.account.infrastructure.store.model.RolePermissionModel;
import serp.project.account.infrastructure.store.repository.IRolePermissionRepository;

@Component
@RequiredArgsConstructor
public class RolePermissionAdapter implements IRolePermissionPort {
    private final IRolePermissionRepository rolePermissionRepository;
    private final RolePermissionMapper rolePermissionMapper;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveAll(List<RolePermissionEntity> rolePermissions) {
        List<RolePermissionModel> models = rolePermissionMapper.toModelList(rolePermissions);
        String sql = """
                INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (role_id, permission_id) DO NOTHING
                """;
        jdbcTemplate.batchUpdate(sql, models, models.size(), (ps, argument) -> {
            ps.setLong(1, argument.getRoleId());
            ps.setLong(2, argument.getPermissionId());
            ps.setObject(3, argument.getCreatedAt());
            ps.setObject(4, argument.getUpdatedAt());
        });
    }

    @Override
    public List<RolePermissionEntity> getRolePermissionsByRoleId(Long roleId) {
        return rolePermissionMapper.toEntityList(rolePermissionRepository.findByRoleId(roleId));
    }
}
