/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.GroupRoleEntity;
import serp.project.account.core.port.store.IGroupRolePort;
import serp.project.account.infrastructure.store.mapper.GroupRoleMapper;
import serp.project.account.infrastructure.store.model.GroupRoleModel;
import serp.project.account.infrastructure.store.repository.IGroupRoleRepository;

@Component
@RequiredArgsConstructor
public class GroupRoleAdapter implements IGroupRolePort {
    private final IGroupRoleRepository groupRoleRepository;
    private final GroupRoleMapper groupRoleMapper;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveAll(List<GroupRoleEntity> groupRoles) {
        List<GroupRoleModel> models = groupRoleMapper.toModelList(groupRoles);
        String sql = """
                INSERT INTO group_roles (group_id, role_id, created_at, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (group_id, role_id) DO NOTHING
                """;
        jdbcTemplate.batchUpdate(sql, models, models.size(), (ps, argument) -> {
            ps.setLong(1, argument.getGroupId());
            ps.setLong(2, argument.getRoleId());
            ps.setObject(3, argument.getCreatedAt());
            ps.setObject(4, argument.getUpdatedAt());
        });
    }

    @Override
    public List<GroupRoleEntity> getByGroupId(Long groupId) {
        return groupRoleMapper.toEntityList(groupRoleRepository.findByGroupId(groupId));
    }
}
