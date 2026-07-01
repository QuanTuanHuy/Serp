/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import serp.project.account.core.domain.dto.request.GetUserParams;

class UserQueryBuilderTest {

    private final UserQueryBuilder userQueryBuilder = new UserQueryBuilder();

    @Test
    void buildGetUsersQueryWithModuleIdAndOrganizationIdShouldFilterActiveModuleAccess() {
        GetUserParams params = GetUserParams.builder()
                .organizationId(10L)
                .moduleId(20L)
                .page(1)
                .pageSize(25)
                .sortBy("email")
                .sortDirection("asc")
                .build();

        SqlQueryResult result = userQueryBuilder.buildGetUsersQuery(params);

        assertThat(result.dataSql())
                .contains("FROM users u")
                .contains("EXISTS (SELECT 1 FROM user_module_access uma")
                .contains("uma.user_id = u.id")
                .contains("uma.organization_id = :organizationId")
                .contains("uma.module_id = :moduleId")
                .contains("uma.is_active = TRUE")
                .contains("ORDER BY u.email ASC")
                .contains("LIMIT :limit OFFSET :offset");
        assertThat(result.countSql())
                .contains("COUNT(*)")
                .contains("EXISTS (SELECT 1 FROM user_module_access uma")
                .contains("uma.module_id = :moduleId");
        assertThat(result.params().getValue("organizationId")).isEqualTo(10L);
        assertThat(result.params().getValue("moduleId")).isEqualTo(20L);
        assertThat(result.params().getValue("limit")).isEqualTo(25);
        assertThat(result.params().getValue("offset")).isEqualTo(25);
    }

    @Test
    void buildGetUsersQueryWithModuleIdShouldKeepExistingFilters() {
        GetUserParams params = GetUserParams.builder()
                .organizationId(10L)
                .moduleId(20L)
                .status("ACTIVE")
                .search("alice")
                .roleId(30L)
                .departmentId(40L)
                .build();

        SqlQueryResult result = userQueryBuilder.buildGetUsersQuery(params);

        assertThat(result.dataSql())
                .contains("u.primary_organization_id = :organizationId")
                .contains("u.status = :status")
                .contains("LOWER(u.email) LIKE :search")
                .contains("user_roles ur")
                .contains("user_departments ud")
                .contains("user_module_access uma");
        assertThat(result.params().getValue("status")).isEqualTo("ACTIVE");
        assertThat(result.params().getValue("search")).isEqualTo("%alice%");
        assertThat(result.params().getValue("roleId")).isEqualTo(30L);
        assertThat(result.params().getValue("departmentId")).isEqualTo(40L);
        assertThat(result.params().getValue("moduleId")).isEqualTo(20L);
    }

    @Test
    void buildGetUsersQueryWithoutModuleIdShouldNotJoinModuleAccess() {
        GetUserParams params = GetUserParams.builder()
                .organizationId(10L)
                .build();

        SqlQueryResult result = userQueryBuilder.buildGetUsersQuery(params);

        assertThat(result.dataSql()).doesNotContain("user_module_access");
        assertThat(result.countSql()).doesNotContain("user_module_access");
    }
}
