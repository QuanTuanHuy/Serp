/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.domain.enums.UserType;
import serp.project.account.infrastructure.store.model.UserModel;

@Component
public class UserRowMapper implements RowMapper<UserEntity> {

    private final UserMapper userMapper;

    public UserRowMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
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
                .userType(toUserType(rs.getString("user_type")))
                .status(toUserStatus(rs.getString("status")))
                .lastLoginAt(rs.getObject("last_login_at", Long.class))
                .avatarUrl(rs.getString("avatar_url"))
                .timezone(rs.getString("timezone"))
                .preferredLanguage(rs.getString("preferred_language"))
                .createdAt(toLocalDateTime(rs.getTimestamp("created_at")))
                .updatedAt(toLocalDateTime(rs.getTimestamp("updated_at")))
                .build();
        return userMapper.toEntity(userModel);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private UserType toUserType(String userType) {
        return userType != null ? UserType.valueOf(userType) : null;
    }

    private UserStatus toUserStatus(String userStatus) {
        return userStatus != null ? UserStatus.valueOf(userStatus) : null;
    }
}
