/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.entity.UserRoleEntity;
import serp.project.account.core.port.store.IUserRolePort;
import serp.project.account.infrastructure.store.mapper.UserRoleMapper;
import serp.project.account.infrastructure.store.model.UserRoleModel;
import serp.project.account.infrastructure.store.repository.IUserRoleRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRoleAdapter implements IUserRolePort {
    private final IUserRoleRepository userRoleRepository;
    private final UserRoleMapper userRoleMapper;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveAll(List<UserRoleEntity> userRoles) {
        List<UserRoleModel> models = userRoleMapper.toModelList(userRoles);
        String sql = """
                INSERT INTO user_roles (user_id, role_id, created_at, updated_at)
                VALUES (?, ?, ?, ?)
                    """;
        jdbcTemplate.batchUpdate(sql, models, models.size(), (ps, argument) -> {
            ps.setLong(1, argument.getUserId());
            ps.setLong(2, argument.getRoleId());
            ps.setObject(3, argument.getCreatedAt());
            ps.setObject(4, argument.getUpdatedAt());
        });
    }

    @Override
    public List<UserRoleEntity> getUserRolesByUserId(Long userId) {
        return userRoleMapper.toEntityList(userRoleRepository.findByUserId(userId));
    }

    @Override
    public List<UserRoleEntity> getUserRolesByUserIds(List<Long> userIds) {
        return userRoleMapper.toEntityList(userRoleRepository.findByUserIdIn(userIds));
    }

    public void testBatchInsert() {
        Long start = System.currentTimeMillis();
        String sql = """
                INSERT INTO tests(name, created_at, updated_at)
                VALUES (?, ?, ?)
                """;
        List<Integer> testList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            testList.add(i);
        }
        jdbcTemplate.batchUpdate(sql, testList, 1000, (ps, argument) -> {
            ps.setString(1, "Name " + argument);
            ps.setObject(2, System.currentTimeMillis());
            ps.setObject(3, System.currentTimeMillis());
        });
        Long end = System.currentTimeMillis();
        log.info("Time taken to insert 10000 records: {} ms", (end - start));
    }
}
