/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.domain.enums.UserType;
import serp.project.account.core.port.store.IUserPort;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.infrastructure.store.model.UserModel;
import serp.project.account.infrastructure.store.repository.IUserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserAdapter implements IUserPort {
    
    private static final Map<String, String> USER_SORT_COLUMNS = Map.ofEntries(
            Map.entry("id", "u.id"),
            Map.entry("email", "u.email"),
            Map.entry("firstName", "u.first_name"),
            Map.entry("first_name", "u.first_name"),
            Map.entry("lastName", "u.last_name"),
            Map.entry("last_name", "u.last_name"),
            Map.entry("phoneNumber", "u.phone_number"),
            Map.entry("phone_number", "u.phone_number"),
            Map.entry("primaryOrganizationId", "u.primary_organization_id"),
            Map.entry("primary_organization_id", "u.primary_organization_id"),
            Map.entry("primaryDepartmentId", "u.primary_department_id"),
            Map.entry("primary_department_id", "u.primary_department_id"),
            Map.entry("userType", "u.user_type"),
            Map.entry("user_type", "u.user_type"),
            Map.entry("status", "u.status"),
            Map.entry("lastLoginAt", "u.last_login_at"),
            Map.entry("last_login_at", "u.last_login_at"),
            Map.entry("createdAt", "u.created_at"),
            Map.entry("created_at", "u.created_at"),
            Map.entry("updatedAt", "u.updated_at"),
            Map.entry("updated_at", "u.updated_at"));

    private final IUserRepository userRepository;
    private final UserMapper userMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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
        var whereClause = new StringBuilder(" WHERE 1=1");
        var sqlParams = new MapSqlParameterSource();

        if (params.getOrganizationId() != null) {
            whereClause.append(" AND u.primary_organization_id = :organizationId");
            sqlParams.addValue("organizationId", params.getOrganizationId());
        }

        if (params.getStatus() != null) {
            whereClause.append(" AND u.status = :status");
            sqlParams.addValue("status", params.getStatus());
        }

        if (params.getSearch() != null && !params.getSearch().trim().isEmpty()) {
            whereClause.append(" AND (LOWER(u.email) LIKE :search OR LOWER(u.first_name) LIKE :search)");
            sqlParams.addValue("search", "%" + params.getSearch().toLowerCase() + "%");
        }

        if (params.getUserType() != null) {
            whereClause.append(" AND u.user_type = :userType");
            sqlParams.addValue("userType", params.getUserType());
        }

        if (params.getRoleId() != null) {
            whereClause
                    .append(" AND EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = :roleId)");
            sqlParams.addValue("roleId", params.getRoleId());
        }

        if (params.getDepartmentId() != null) {
            whereClause.append(
                    " AND EXISTS (SELECT 1 FROM user_departments ud WHERE ud.user_id = u.id AND ud.department_id = :departmentId AND ud.is_active = TRUE)");
            sqlParams.addValue("departmentId", params.getDepartmentId());
        }

        String countSql = "SELECT COUNT(*) FROM users u" + whereClause;
        Long totalElements = namedParameterJdbcTemplate.queryForObject(countSql, sqlParams, Long.class);

        int page = params.getPage() != null && params.getPage() >= 0 ? params.getPage() : 0;
        int pageSize = params.getPageSize() != null && params.getPageSize() > 0 ? params.getPageSize() : 10;

        String sortBy = USER_SORT_COLUMNS.getOrDefault(params.getSortBy(), "u.id");
        String sortDirection = "desc".equalsIgnoreCase(params.getSortDirection()) ? "DESC" : "ASC";
        int offset = page * pageSize;

        sqlParams.addValue("limit", pageSize);
        sqlParams.addValue("offset", offset);

        String selectSql = "SELECT u.* FROM users u" + whereClause + " ORDER BY " + sortBy + " " + sortDirection
                + " LIMIT :limit OFFSET :offset";
        log.info("Executing SQL: {}", selectSql);

        var users = namedParameterJdbcTemplate.query(selectSql, sqlParams, (rs, rowNum) -> {
            UserModel userModel = UserModel.builder()
                    .id(rs.getLong("id"))
                    .email(rs.getString("email"))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .phoneNumber(rs.getString("phone_number"))
                    .keycloakId(rs.getString("keycloak_id"))
                    .isSuperAdmin(rs.getObject("is_super_admin", Boolean.class))
                    .primaryOrganizationId(rs.getObject("primary_organization_id", Long.class))
                    .primaryDepartmentId(rs.getObject("primary_department_id", Long.class))
                    .userType(rs.getString("user_type") != null ? UserType.valueOf(rs.getString("user_type")) : null)
                    .status(rs.getString("status") != null ? UserStatus.valueOf(rs.getString("status")) : null)
                    .lastLoginAt(rs.getObject("last_login_at", Long.class))
                    .avatarUrl(rs.getString("avatar_url"))
                    .timezone(rs.getString("timezone"))
                    .preferredLanguage(rs.getString("preferred_language"))
                    .createdAt(toLocalDateTime(rs.getTimestamp("created_at")))
                    .updatedAt(toLocalDateTime(rs.getTimestamp("updated_at")))
                    .build();
            return userMapper.toEntity(userModel);
        });

        return Pair.of(totalElements != null ? totalElements : 0L, users);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
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
