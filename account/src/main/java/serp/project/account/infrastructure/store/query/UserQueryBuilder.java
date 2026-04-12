/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.query;

import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import serp.project.account.core.domain.dto.request.GetUserParams;

@Component
public class UserQueryBuilder {

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

    public SqlQueryResult buildGetUsersQuery(GetUserParams params) {
        var whereClause = new StringBuilder(" WHERE 1=1");
        var sqlParams = new MapSqlParameterSource();

        appendOrganizationFilter(whereClause, sqlParams, params);
        appendStatusFilter(whereClause, sqlParams, params);
        appendSearchFilter(whereClause, sqlParams, params);
        appendUserTypeFilter(whereClause, sqlParams, params);
        appendRoleFilter(whereClause, sqlParams, params);
        appendDepartmentFilter(whereClause, sqlParams, params);

        int page = params.getPage() != null && params.getPage() >= 0 ? params.getPage() : 0;
        int pageSize = params.getPageSize() != null && params.getPageSize() > 0 ? params.getPageSize() : 10;
        int offset = page * pageSize;

        String sortBy = USER_SORT_COLUMNS.getOrDefault(params.getSortBy(), "u.id");
        String sortDirection = "desc".equalsIgnoreCase(params.getSortDirection()) ? "DESC" : "ASC";

        sqlParams.addValue("limit", pageSize);
        sqlParams.addValue("offset", offset);

        String countSql = "SELECT COUNT(*) FROM users u" + whereClause;
        String dataSql = "SELECT u.* FROM users u" + whereClause
                + " ORDER BY " + sortBy + " " + sortDirection
                + " LIMIT :limit OFFSET :offset";

        return new SqlQueryResult(dataSql, countSql, sqlParams);
    }

    private void appendOrganizationFilter(StringBuilder whereClause, MapSqlParameterSource sqlParams,
            GetUserParams params) {
        if (params.getOrganizationId() != null) {
            whereClause.append(" AND u.primary_organization_id = :organizationId");
            sqlParams.addValue("organizationId", params.getOrganizationId());
        }
    }

    private void appendStatusFilter(StringBuilder whereClause, MapSqlParameterSource sqlParams,
            GetUserParams params) {
        if (params.getStatus() != null) {
            whereClause.append(" AND u.status = :status");
            sqlParams.addValue("status", params.getStatus());
        }
    }

    private void appendSearchFilter(StringBuilder whereClause, MapSqlParameterSource sqlParams,
            GetUserParams params) {
        if (params.getSearch() != null && !params.getSearch().trim().isEmpty()) {
            whereClause.append(" AND (LOWER(u.email) LIKE :search OR LOWER(u.first_name) LIKE :search)");
            sqlParams.addValue("search", "%" + params.getSearch().trim().toLowerCase() + "%");
        }
    }

    private void appendUserTypeFilter(StringBuilder whereClause, MapSqlParameterSource sqlParams,
            GetUserParams params) {
        if (params.getUserType() != null) {
            whereClause.append(" AND u.user_type = :userType");
            sqlParams.addValue("userType", params.getUserType());
        }
    }

    private void appendRoleFilter(StringBuilder whereClause, MapSqlParameterSource sqlParams,
            GetUserParams params) {
        if (params.getRoleId() != null) {
            whereClause
                    .append(" AND EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = :roleId)");
            sqlParams.addValue("roleId", params.getRoleId());
        }
    }

    private void appendDepartmentFilter(StringBuilder whereClause, MapSqlParameterSource sqlParams,
            GetUserParams params) {
        if (params.getDepartmentId() != null) {
            whereClause.append(
                    " AND EXISTS (SELECT 1 FROM user_departments ud WHERE ud.user_id = u.id AND ud.department_id = :departmentId AND ud.is_active = TRUE)");
            sqlParams.addValue("departmentId", params.getDepartmentId());
        }
    }
}
