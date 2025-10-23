/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.CreateUserForOrgRequest;
import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IKeycloakUserService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.IUserService;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.PaginationUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationUseCase {
    private final IOrganizationService organizationService;
    private final IUserService userService;
    private final IRoleService roleService;
    private final IKeycloakUserService keycloakUserService;

    private final ResponseUtils responseUtils;
    private final PaginationUtils paginationUtils;

    private final UserMapper userMapper;

    public GeneralResponse<?> getOrganizations(GetOrganizationParams params) {
        try {
            var pairOrganizations = organizationService.getOrganizations(params);
            return responseUtils.success(paginationUtils.getResponse(
                    pairOrganizations.getSecond(),
                    params.getPage(),
                    params.getPageSize(),
                    pairOrganizations.getFirst()));
        } catch (Exception e) {
            log.error("Error getting organizations: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getOrganizationById(Long organizationId) {
        try {
            var organization = organizationService.getOrganizationById(organizationId);
            if (organization == null) {
                return responseUtils.notFound(Constants.ErrorMessage.ORGANIZATION_NOT_FOUND);
            }
            return responseUtils.success(organization);
        } catch (Exception e) {
            log.error("Error getting organization by id: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createUserForOrganization(Long tenantId, CreateUserForOrgRequest request) {
        try {
            var organization = organizationService.getOrganizationById(tenantId);
            if (organization == null) {
                return responseUtils.notFound(Constants.ErrorMessage.ORGANIZATION_NOT_FOUND);
            }

            var user = userService.createUser(tenantId, request);
            final Long userId = user.getId();

            var keycloakUser = userMapper.createUserMapper(user, tenantId, request.getPassword());
            var keycloakUserId = keycloakUserService.createUser(keycloakUser);

            user.setKeycloakId(keycloakUserId);
            user.setStatus(UserStatus.ACTIVE);
            userService.updateUser(userId, user);

            List<RoleEntity> roles;
            if (CollectionUtils.isEmpty(request.getRoleIds())) {
                roles = roleService.getRolesByScope(RoleScope.ORGANIZATION).stream()
                        .filter(role -> role.getIsDefault() == true)
                        .toList();
            } else {
                roles = roleService.getRolesByScope(RoleScope.ORGANIZATION).stream()
                        .filter(role -> request.getRoleIds().contains(role.getId()))
                        .toList();
            }
            if (CollectionUtils.isEmpty(roles)) {
                log.error("No roles found to assign to user with id {} in organization with id {}. Check why?", userId,
                        tenantId);
                throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
            }
            userService.addRolesToUser(userId, roles.stream().map(RoleEntity::getId).toList());
            keycloakUserService.assignRealmRoles(keycloakUserId, roles.stream()
                    .filter(r -> r.getKeycloakClientId() == null)
                    .map(RoleEntity::getName)
                    .toList());

            roles.forEach(role -> 
                organizationService.assignOrganizationToUser(tenantId, userId, role.getId(), true)
            );

            return responseUtils.success("User created successfully");

        } catch (AppException e) {
            log.error("Error creating user for organization: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating user for organization: {}", e.getMessage());
            throw e;
        }
    }
}
