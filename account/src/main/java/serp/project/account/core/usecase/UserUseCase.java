/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.AssignRoleToUserDto;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.service.IKeycloakUserService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.PaginationUtils;
import serp.project.account.kernel.utils.ResponseUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUseCase {
    private final IUserService userService;
    private final IKeycloakUserService keycloakUserService;
    private final IRoleService roleService;

    private final ResponseUtils responseUtils;
    private final PaginationUtils paginationUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> assignRolesToUser(AssignRoleToUserDto request) {
        try {
            UserEntity user = userService.getUserById(request.getUserId());
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_NOT_FOUND);
            }

            List<Long> roleIds = request.getRoleIds();
            var roles = roleService.getAllRoles().stream()
                    .filter(r -> roleIds.contains(r.getId()))
                    .toList();
            if (CollectionUtils.isEmpty(roles)) {
                return responseUtils.badRequest(Constants.ErrorMessage.ROLE_NOT_FOUND);
            }

            List<String> realmRoles = roles.stream()
                    .filter(r -> r.getKeycloakClientId() == null)
                    .map(RoleEntity::getName)
                    .toList();
            var clientRoles = roles.stream()
                    .filter(r -> r.getKeycloakClientId() != null)
                    .collect(Collectors.groupingBy(
                            RoleEntity::getKeycloakClientId,
                            Collectors.mapping(RoleEntity::getName, Collectors.toList())));
            keycloakUserService.assignRealmRoles(user.getKeycloakId(), realmRoles);
            for (var entry : clientRoles.entrySet()) {
                keycloakUserService.assignClientRoles(user.getKeycloakId(), entry.getKey(), entry.getValue());
            }
            userService.addRolesToUser(user.getId(), roles.stream().map(RoleEntity::getId).toList());
            return responseUtils.success("Roles assigned successfully");
        } catch (Exception e) {
            log.error("Assign roles to user failed: {}", e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> getUsers(GetUserParams params) {
        try {
            var response = userService.getUsers(params);
            return responseUtils.success(paginationUtils.getResponse(response.getFirst(), response.getSecond()));
        } catch (Exception e) {
            log.error("Get users failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }
}
