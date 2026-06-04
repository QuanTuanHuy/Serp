/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.port.store.IUserPort;
import serp.project.account.infrastructure.store.mapper.UserRowMapper;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.infrastructure.store.model.UserModel;
import serp.project.account.infrastructure.store.query.UserQueryBuilder;
import serp.project.account.infrastructure.store.repository.IUserRepository;

@Component
@RequiredArgsConstructor
public class UserAdapter implements IUserPort {
    private final IUserRepository userRepository;
    private final UserMapper userMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final UserQueryBuilder userQueryBuilder;
    private final UserRowMapper userRowMapper;

    @Override
    public UserEntity save(UserEntity user) {
        UserModel userModel = userMapper.toModel(user);
        return userMapper.toEntity(userRepository.save(userModel));
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toEntity)
                .orElse(null);
    }

    @Override
    public UserEntity getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toEntity)
                .orElse(null);
    }

    @Override
    public Pair<Long, List<UserEntity>> getUsers(GetUserParams params) {
        var queryResult = userQueryBuilder.buildGetUsersQuery(params);
        Long totalElements = namedParameterJdbcTemplate.queryForObject(
                queryResult.countSql(),
                queryResult.params(),
                Long.class);
        var users = namedParameterJdbcTemplate.query(
                queryResult.dataSql(),
                queryResult.params(),
                userRowMapper);
        return Pair.of(totalElements != null ? totalElements : 0L, users);
    }

    @Override
    public List<UserEntity> getUsersByIds(List<Long> userIds) {
        return userMapper.toEntityList(userRepository.findByIdIn(userIds));
    }

    @Override
    public List<UserEntity> getUsersByOrganizationId(Long organizationId) {
        return userMapper.toEntityList(userRepository.findByPrimaryOrganizationId(organizationId));
    }

    @Override
    public Integer countUsersByOrganizationId(Long organizationId) {
        return userRepository.countByPrimaryOrganizationId(organizationId);
    }

    @Override
    public Long countUsers() {
        return userRepository.count();
    }

    @Override
    public Long countUsersByStatus(UserStatus status) {
        return userRepository.countByStatus(status);
    }

    @Override
    public Map<Long, Long> countUsersByOrganizationIds(List<Long> organizationIds) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.countUsersByOrganizationIds(organizationIds).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()));
    }

    @Override
    public Integer countUsersByOrganizationIdAndStatus(Long organizationId, UserStatus status) {
        return userRepository.countByPrimaryOrganizationIdAndStatus(organizationId, status);
    }

    @Override
    public Integer countUsersByOrganizationIdAndCreatedBetween(Long organizationId, LocalDateTime from,
            LocalDateTime to) {
        return userRepository.countByOrganizationIdAndCreatedAtBetween(organizationId, from, to);
    }

    @Override
    public Integer countAdminUsersByOrganizationId(Long organizationId) {
        return userRepository.countAdminUsersByOrganizationId(organizationId);
    }

    @Override
    public List<UserEntity> getUsersByOrganizationIdAndIds(Long organizationId, List<Long> userIds) {
        return userMapper.toEntityList(userRepository.findByPrimaryOrganizationIdAndIdIn(organizationId, userIds));
    }

    @Override
    public Map<String, Integer> countUsersByStatusForOrganization(Long organizationId) {
        var counts = userRepository.countUsersByStatusForOrganization(organizationId);
        return counts.stream().collect(Collectors.toMap(
                c -> c[0].toString(),
                c -> Integer.parseInt(c[1].toString())));
    }
}
