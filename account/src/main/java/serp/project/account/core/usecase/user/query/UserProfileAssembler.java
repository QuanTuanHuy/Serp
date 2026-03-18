/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.dto.response.UserProfileResponse;
import serp.project.account.core.domain.entity.DepartmentEntity;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.service.IDepartmentService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IUserModuleAccessService;
import serp.project.account.infrastructure.store.mapper.UserMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserProfileAssembler {
    private final UserMapper userMapper;
    private final IOrganizationService organizationService;
    private final IDepartmentService departmentService;
    private final IUserModuleAccessService userModuleAccessService;

    public UserProfileResponse assembleBasic(UserEntity user) {
        if (user == null) {
            return null;
        }

        var profile = userMapper.toProfileResponse(user);
        enrichOrganizationNames(List.of(profile));
        return profile;
    }

    public List<UserProfileResponse> assembleBasic(List<UserEntity> users) {
        var profiles = mapProfiles(users);
        enrichOrganizationNames(profiles);
        return profiles;
    }

    public List<UserProfileResponse> assembleListView(List<UserEntity> users) {
        var profiles = mapProfiles(users);
        if (profiles.isEmpty()) {
            return profiles;
        }

        var organizationMap = loadOrganizationMap(profiles.stream()
                .map(UserProfileResponse::getOrganizationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        var departmentMap = loadDepartmentMap(users.stream()
                .map(UserEntity::getPrimaryDepartmentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        var moduleCountMap = loadModuleCountMap(users.stream()
                .map(UserEntity::getId)
                .toList());

        for (int i = 0; i < profiles.size(); i++) {
            var profile = profiles.get(i);
            var user = users.get(i);

            var organization = organizationMap.get(profile.getOrganizationId());
            if (organization != null) {
                profile.setOrganizationName(organization.getName());
            }

            if (user.getPrimaryDepartmentId() != null) {
                profile.setPrimaryDepartmentId(user.getPrimaryDepartmentId());
                var department = departmentMap.get(user.getPrimaryDepartmentId());
                if (department != null) {
                    profile.setPrimaryDepartmentName(department.getName());
                }
            }

            profile.setModuleAccessCount(moduleCountMap.getOrDefault(user.getId(), 0));
        }

        return profiles;
    }

    private List<UserProfileResponse> mapProfiles(List<UserEntity> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }

        return users.stream()
                .map(userMapper::toProfileResponse)
                .toList();
    }

    private void enrichOrganizationNames(List<UserProfileResponse> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            return;
        }

        var organizationMap = loadOrganizationMap(profiles.stream()
                .map(UserProfileResponse::getOrganizationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());

        profiles.forEach(profile -> {
            var organization = organizationMap.get(profile.getOrganizationId());
            if (organization != null) {
                profile.setOrganizationName(organization.getName());
            }
        });
    }

    private Map<Long, OrganizationEntity> loadOrganizationMap(List<Long> organizationIds) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Map.of();
        }

        var organizations = organizationService.getOrganizationsByIds(organizationIds);
        if (organizations == null || organizations.isEmpty()) {
            return Map.of();
        }

        return organizations.stream()
                .collect(Collectors.toMap(OrganizationEntity::getId, Function.identity()));
    }

    private Map<Long, DepartmentEntity> loadDepartmentMap(List<Long> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return Map.of();
        }

        var departments = departmentService.getDepartmentsByIds(departmentIds);
        if (departments == null || departments.isEmpty()) {
            return Map.of();
        }

        return departments.stream()
                .collect(Collectors.toMap(DepartmentEntity::getId, Function.identity()));
    }

    private Map<Long, Integer> loadModuleCountMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        var moduleCountMap = userModuleAccessService.countActiveModulesByUserIds(userIds);
        return moduleCountMap != null ? moduleCountMap : Map.of();
    }
}
