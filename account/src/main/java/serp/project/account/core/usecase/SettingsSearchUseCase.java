/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetDepartmentParams;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.SettingsGlobalSearchGroupResponse;
import serp.project.account.core.domain.dto.response.SettingsGlobalSearchItemResponse;
import serp.project.account.core.domain.dto.response.SettingsGlobalSearchResponse;
import serp.project.account.core.domain.entity.DepartmentEntity;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.SettingsGlobalSearchType;
import serp.project.account.core.service.IDepartmentService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
public class SettingsSearchUseCase {
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 10;

    private final IUserService userService;
    private final IDepartmentService departmentService;
    private final ISubscriptionService subscriptionService;
    private final ISubscriptionPlanService subscriptionPlanService;
    private final IModuleService moduleService;
    private final ResponseUtils responseUtils;

    public GeneralResponse<?> search(Long organizationId, String query, Integer limit) {
        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery.isEmpty()) {
            return responseUtils.badRequest(Constants.ErrorMessage.SEARCH_QUERY_REQUIRED);
        }

        int normalizedLimit = normalizeLimit(limit);
        String encodedQuery = URLEncoder.encode(normalizedQuery, StandardCharsets.UTF_8);

        Pair<Long, List<UserEntity>> users = userService.getUsers(
                GetUserParams.builder()
                        .organizationId(organizationId)
                        .search(normalizedQuery)
                        .page(0)
                        .pageSize(normalizedLimit)
                        .sortBy("email")
                        .sortDirection("asc")
                        .build());
        Pair<List<DepartmentEntity>, Long> departments = departmentService.getDepartments(
                GetDepartmentParams.builder()
                        .organizationId(organizationId)
                        .search(normalizedQuery)
                        .page(0)
                        .pageSize(normalizedLimit)
                        .sortBy("name")
                        .sortDirection("asc")
                        .build());
        List<ModuleEntity> matchingModules = searchAccessibleModules(
                organizationId,
                normalizedQuery);

        SettingsGlobalSearchResponse response = SettingsGlobalSearchResponse.builder()
                .query(normalizedQuery)
                .limit(normalizedLimit)
                .groups(List.of(
                        userGroup(users, encodedQuery),
                        departmentGroup(departments, encodedQuery),
                        moduleGroup(matchingModules, normalizedLimit, encodedQuery)))
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

    private List<ModuleEntity> searchAccessibleModules(Long organizationId, String query) {
        var subscription = subscriptionService.getActiveOrPendingUpgrade(organizationId);
        var includedModuleIds = subscriptionPlanService.getPlanModules(subscription.getSubscriptionPlanId())
                .stream()
                .filter(planModule -> Boolean.TRUE.equals(planModule.getIsIncluded()))
                .map(SubscriptionPlanModuleEntity::getModuleId)
                .toList();
        String lowerQuery = query.toLowerCase();

        return moduleService.getModulesByIds(includedModuleIds)
                .stream()
                .filter(module -> matches(module.getModuleName(), lowerQuery)
                        || matches(module.getCode(), lowerQuery)
                        || matches(module.getDescription(), lowerQuery))
                .sorted(Comparator.comparing(module -> valueOrFallback(module.getModuleName(), "")))
                .toList();
    }

    private SettingsGlobalSearchGroupResponse userGroup(Pair<Long, List<UserEntity>> users, String encodedQuery) {
        return SettingsGlobalSearchGroupResponse.builder()
                .type(SettingsGlobalSearchType.USER)
                .title("Users")
                .total(users.getFirst())
                .items(users.getSecond().stream()
                        .map(user -> SettingsGlobalSearchItemResponse.builder()
                                .id(String.valueOf(user.getId()))
                                .title(valueOrFallback(user.getFullName(), user.getEmail()))
                                .subtitle(joinNonBlank(" - ", user.getEmail(), enumName(user.getStatus())))
                                .url("/settings/users?search=" + encodedQuery)
                                .build())
                        .toList())
                .build();
    }

    private SettingsGlobalSearchGroupResponse departmentGroup(
            Pair<List<DepartmentEntity>, Long> departments,
            String encodedQuery) {
        return SettingsGlobalSearchGroupResponse.builder()
                .type(SettingsGlobalSearchType.DEPARTMENT)
                .title("Departments")
                .total(departments.getSecond())
                .items(departments.getFirst().stream()
                        .map(department -> SettingsGlobalSearchItemResponse.builder()
                                .id(String.valueOf(department.getId()))
                                .title(valueOrFallback(department.getName(), "Unnamed department"))
                                .subtitle(joinNonBlank(" - ", department.getCode(), activeLabel(department)))
                                .url("/settings/departments?search=" + encodedQuery)
                                .build())
                        .toList())
                .build();
    }

    private SettingsGlobalSearchGroupResponse moduleGroup(
            List<ModuleEntity> modules,
            int limit,
            String encodedQuery) {
        return SettingsGlobalSearchGroupResponse.builder()
                .type(SettingsGlobalSearchType.MODULE)
                .title("Modules")
                .total((long) modules.size())
                .items(modules.stream()
                        .limit(limit)
                        .map(module -> SettingsGlobalSearchItemResponse.builder()
                                .id(String.valueOf(module.getId()))
                                .title(valueOrFallback(module.getModuleName(), "Unnamed module"))
                                .subtitle(joinNonBlank(" - ", module.getCode(), enumName(module.getStatus())))
                                .url("/settings/modules?search=" + encodedQuery)
                                .build())
                        .toList())
                .build();
    }

    private boolean matches(String value, String lowerQuery) {
        return value != null && value.toLowerCase().contains(lowerQuery);
    }

    private String activeLabel(DepartmentEntity department) {
        return department.getIsActive() == null ? "" : Boolean.TRUE.equals(department.getIsActive()) ? "ACTIVE" : "INACTIVE";
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
