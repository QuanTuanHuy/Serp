/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.AdminGlobalSearchGroupResponse;
import serp.project.account.core.domain.dto.response.AdminGlobalSearchItemResponse;
import serp.project.account.core.domain.dto.response.AdminGlobalSearchResponse;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.AdminGlobalSearchType;
import serp.project.account.core.port.store.IModulePort;
import serp.project.account.core.port.store.IOrganizationPort;
import serp.project.account.core.port.store.IRolePort;
import serp.project.account.core.port.store.IUserPort;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
public class AdminSearchUseCase {
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 10;

    private final IOrganizationPort organizationPort;
    private final IUserPort userPort;
    private final IRolePort rolePort;
    private final IModulePort modulePort;
    private final ResponseUtils responseUtils;

    public GeneralResponse<?> search(String query, Integer limit) {
        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery.isEmpty()) {
            return responseUtils.badRequest(Constants.ErrorMessage.SEARCH_QUERY_REQUIRED);
        }

        int normalizedLimit = normalizeLimit(limit);
        String encodedQuery = URLEncoder.encode(normalizedQuery, StandardCharsets.UTF_8);

        Pair<List<OrganizationEntity>, Long> organizations = organizationPort.getOrganizations(
                GetOrganizationParams.builder()
                        .search(normalizedQuery)
                        .page(0)
                        .pageSize(normalizedLimit)
                        .sortBy("name")
                        .sortDirection("asc")
                        .build());
        Pair<Long, List<UserEntity>> users = userPort.getUsers(
                GetUserParams.builder()
                        .search(normalizedQuery)
                        .page(0)
                        .pageSize(normalizedLimit)
                        .sortBy("email")
                        .sortDirection("asc")
                        .build());
        Pair<List<RoleEntity>, Long> roles = rolePort.searchRoles(normalizedQuery, normalizedLimit);
        Pair<List<ModuleEntity>, Long> modules = modulePort.searchModules(normalizedQuery, normalizedLimit);

        AdminGlobalSearchResponse response = AdminGlobalSearchResponse.builder()
                .query(normalizedQuery)
                .limit(normalizedLimit)
                .groups(List.of(
                        organizationGroup(organizations, encodedQuery),
                        userGroup(users, encodedQuery),
                        roleGroup(roles, encodedQuery),
                        moduleGroup(modules, encodedQuery)))
                .build();

        return responseUtils.success(response);
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private AdminGlobalSearchGroupResponse organizationGroup(
            Pair<List<OrganizationEntity>, Long> organizations,
            String encodedQuery) {
        return AdminGlobalSearchGroupResponse.builder()
                .type(AdminGlobalSearchType.ORGANIZATION)
                .title("Organizations")
                .total(organizations.getSecond())
                .items(organizations.getFirst().stream()
                        .map(organization -> AdminGlobalSearchItemResponse.builder()
                                .id(String.valueOf(organization.getId()))
                                .title(valueOrFallback(organization.getName(), "Unnamed organization"))
                                .subtitle(joinNonBlank(" - ", organization.getCode(), enumName(organization.getStatus())))
                                .url("/admin/organizations?search=" + encodedQuery)
                                .build())
                        .toList())
                .build();
    }

    private AdminGlobalSearchGroupResponse userGroup(Pair<Long, List<UserEntity>> users, String encodedQuery) {
        return AdminGlobalSearchGroupResponse.builder()
                .type(AdminGlobalSearchType.USER)
                .title("Users")
                .total(users.getFirst())
                .items(users.getSecond().stream()
                        .map(user -> AdminGlobalSearchItemResponse.builder()
                                .id(String.valueOf(user.getId()))
                                .title(valueOrFallback(user.getFullName(), user.getEmail()))
                                .subtitle(joinNonBlank(" - ", user.getEmail(), enumName(user.getStatus())))
                                .url("/admin/users?search=" + encodedQuery)
                                .build())
                        .toList())
                .build();
    }

    private AdminGlobalSearchGroupResponse roleGroup(Pair<List<RoleEntity>, Long> roles, String encodedQuery) {
        return AdminGlobalSearchGroupResponse.builder()
                .type(AdminGlobalSearchType.ROLE)
                .title("Roles")
                .total(roles.getSecond())
                .items(roles.getFirst().stream()
                        .map(role -> AdminGlobalSearchItemResponse.builder()
                                .id(String.valueOf(role.getId()))
                                .title(valueOrFallback(role.getName(), "Unnamed role"))
                                .subtitle(joinNonBlank(" - ", enumName(role.getScope()), enumName(role.getRoleType())))
                                .url("/admin/roles?search=" + encodedQuery)
                                .build())
                        .toList())
                .build();
    }

    private AdminGlobalSearchGroupResponse moduleGroup(Pair<List<ModuleEntity>, Long> modules, String encodedQuery) {
        return AdminGlobalSearchGroupResponse.builder()
                .type(AdminGlobalSearchType.MODULE)
                .title("Modules")
                .total(modules.getSecond())
                .items(modules.getFirst().stream()
                        .map(module -> AdminGlobalSearchItemResponse.builder()
                                .id(String.valueOf(module.getId()))
                                .title(valueOrFallback(module.getModuleName(), "Unnamed module"))
                                .subtitle(joinNonBlank(" - ", module.getCode(), enumName(module.getStatus())))
                                .url("/admin/modules?search=" + encodedQuery)
                                .build())
                        .toList())
                .build();
    }

    private String enumName(Enum<?> value) {
        return value == null ? "" : value.name();
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String joinNonBlank(String delimiter, String first, String second) {
        boolean hasFirst = first != null && !first.isBlank();
        boolean hasSecond = second != null && !second.isBlank();
        if (hasFirst && hasSecond) {
            return first + delimiter + second;
        }
        if (hasFirst) {
            return first;
        }
        if (hasSecond) {
            return second;
        }
        return "";
    }
}
