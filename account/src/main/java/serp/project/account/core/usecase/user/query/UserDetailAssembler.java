/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.dto.response.UserDetailResponse;
import serp.project.account.core.domain.entity.DepartmentEntity;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserDepartmentEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.entity.UserModuleAccessEntity;
import serp.project.account.core.service.IDepartmentService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IUserDepartmentService;
import serp.project.account.core.service.IUserModuleAccessService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserDetailAssembler {
    private final IOrganizationService organizationService;
    private final IUserDepartmentService userDepartmentService;
    private final IDepartmentService departmentService;
    private final IUserModuleAccessService userModuleAccessService;
    private final IModuleService moduleService;

    public UserDetailResponse assemble(UserEntity user, Long organizationId) {
        var organizationName = getOrganizationName(organizationId);
        var userDepartments = getUserDepartments(user.getId());
        var moduleAccesses = getModuleAccesses(user.getId(), organizationId);
        var departmentMap = loadDepartmentMap(userDepartments);
        var moduleMap = loadModuleMap(user, moduleAccesses);

        var roleDetails = buildRoleDetails(user.getRoles(), moduleMap);
        var departmentDetails = userDepartments.stream()
                .map(userDepartment -> toDepartmentDetail(userDepartment, departmentMap))
                .toList();
        var moduleDetails = moduleAccesses.stream()
                .map(moduleAccess -> toModuleAccessDetail(moduleAccess, moduleMap))
                .toList();

        return UserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .userType(user.getUserType())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .timezone(user.getTimezone())
                .preferredLanguage(user.getPreferredLanguage())
                .organizationId(organizationId)
                .organizationName(organizationName)
                .roles(roleDetails)
                .departments(departmentDetails)
                .moduleAccesses(moduleDetails)
                .build();
    }

    private String getOrganizationName(Long organizationId) {
        var organization = organizationService.getOrganizationById(organizationId);
        return organization != null ? organization.getName() : "";
    }

    private List<UserDepartmentEntity> getUserDepartments(Long userId) {
        var userDepartments = userDepartmentService.getUserDepartmentsByUserId(userId);
        return userDepartments != null ? userDepartments : List.of();
    }

    private List<UserModuleAccessEntity> getModuleAccesses(Long userId, Long organizationId) {
        var moduleAccesses = userModuleAccessService.getUserModuleAccesses(userId, organizationId);
        return moduleAccesses != null ? moduleAccesses : List.of();
    }

    private Map<Long, DepartmentEntity> loadDepartmentMap(List<UserDepartmentEntity> userDepartments) {
        var departmentIds = userDepartments.stream()
                .map(UserDepartmentEntity::getDepartmentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (departmentIds.isEmpty()) {
            return Map.of();
        }

        var departments = departmentService.getDepartmentsByIds(departmentIds);
        if (departments == null || departments.isEmpty()) {
            return Map.of();
        }

        return departments.stream()
                .collect(Collectors.toMap(DepartmentEntity::getId, Function.identity()));
    }

    private Map<Long, ModuleEntity> loadModuleMap(UserEntity user, List<UserModuleAccessEntity> moduleAccesses) {
        var moduleIds = new ArrayList<Long>();
        if (user.getRoles() != null) {
            user.getRoles().stream()
                    .map(RoleEntity::getModuleId)
                    .filter(Objects::nonNull)
                    .forEach(moduleIds::add);
        }
        moduleAccesses.stream()
                .map(UserModuleAccessEntity::getModuleId)
                .filter(Objects::nonNull)
                .forEach(moduleIds::add);

        var distinctModuleIds = moduleIds.stream().distinct().toList();
        if (distinctModuleIds.isEmpty()) {
            return Map.of();
        }

        var modules = moduleService.getModulesByIds(distinctModuleIds);
        if (modules == null || modules.isEmpty()) {
            return Map.of();
        }

        return modules.stream()
                .collect(Collectors.toMap(ModuleEntity::getId, Function.identity()));
    }

    private List<UserDetailResponse.RoleDetail> buildRoleDetails(List<RoleEntity> roles, Map<Long, ModuleEntity> moduleMap) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .map(role -> UserDetailResponse.RoleDetail.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .scope(role.getScope() != null ? role.getScope().name() : null)
                        .description(role.getDescription())
                        .moduleName(role.getModuleId() != null && moduleMap.containsKey(role.getModuleId())
                                ? moduleMap.get(role.getModuleId()).getModuleName()
                                : null)
                        .build())
                .toList();
    }

    private UserDetailResponse.DepartmentDetail toDepartmentDetail(UserDepartmentEntity userDepartment,
            Map<Long, DepartmentEntity> departmentMap) {
        var department = departmentMap.get(userDepartment.getDepartmentId());
        return UserDetailResponse.DepartmentDetail.builder()
                .id(userDepartment.getDepartmentId())
                .name(department != null ? department.getName() : null)
                .isPrimary(userDepartment.getIsPrimary())
                .jobTitle(userDepartment.getJobTitle())
                .build();
    }

    private UserDetailResponse.ModuleAccessDetail toModuleAccessDetail(UserModuleAccessEntity moduleAccess,
            Map<Long, ModuleEntity> moduleMap) {
        var module = moduleMap.get(moduleAccess.getModuleId());
        return UserDetailResponse.ModuleAccessDetail.builder()
                .moduleId(moduleAccess.getModuleId())
                .moduleName(module != null ? module.getModuleName() : null)
                .moduleCode(module != null ? module.getCode() : null)
                .isActive(moduleAccess.getIsActive())
                .grantedAt(moduleAccess.getGrantedAt())
                .build();
    }
}
