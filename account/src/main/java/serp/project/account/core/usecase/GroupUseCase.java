/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.account.core.domain.dto.request.AssignRolesToGroupDto;
import serp.project.account.core.domain.dto.request.CreateGroupDto;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.entity.GroupEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.service.IGroupService;
import serp.project.account.core.service.IKeycloakGroupService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.IUserService;
import serp.project.account.infrastructure.store.mapper.GroupMapper;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupUseCase {

    private final IGroupService groupService;
    private final IKeycloakGroupService keycloakGroupService;
    private final IUserService userService;
    private final IRoleService roleService;

    private final ResponseUtils responseUtils;

    private final GroupMapper groupMapper;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createGroup(CreateGroupDto request) {
        try {
            var createKeycloakGroup = groupMapper.toCreateKeycloakGroupDto(request);
            String keycloakGroupId = keycloakGroupService.createGroup(createKeycloakGroup);

            var groupEntity = groupMapper.createGroupMapper(request, keycloakGroupId);
            groupEntity = groupService.createGroup(request, keycloakGroupId);
            return responseUtils.success(groupEntity);
        } catch (Exception e) {
            log.error("Error creating group: {}", e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> getGroupById(Long groupId) {
        try {
            GroupEntity group = groupService.getGroupById(groupId);
            if (group == null) {
                return responseUtils.notFound(Constants.ErrorMessage.GROUP_NOT_FOUND);
            }
            return responseUtils.success(group);
        } catch (Exception e) {
            log.error("Error getting group by id {}: {}", groupId, e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> getGroupByName(String groupName) {
        try {
            GroupEntity group = groupService.getGroupByName(groupName);
            if (group == null) {
                return responseUtils.notFound(Constants.ErrorMessage.GROUP_NOT_FOUND);
            }
            return responseUtils.success(group);
        } catch (Exception e) {
            log.error("Error getting group by name {}: {}", groupName, e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> deleteGroup(Long groupId) {
        try {
            GroupEntity group = groupService.getGroupById(groupId);
            if (group == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.GROUP_NOT_FOUND);
            }
            keycloakGroupService.deleteGroup(group.getKeycloakGroupId());
            groupService.deleteGroup(groupId);
            return responseUtils.success("Group deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting group {}: {}", groupId, e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> addUserToGroup(Long userId, Long groupId) {
        try {
            var user = userService.getUserById(userId);
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_NOT_FOUND);
            }
            var group = groupService.getGroupById(groupId);
            if (group == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.GROUP_NOT_FOUND);
            }
            keycloakGroupService.addUserToGroup(user.getKeycloakId(), group.getKeycloakGroupId());
            groupService.addUserToGroup(userId, groupId);

            var roles = roleService.getRolesByGroupId(groupId);
            if (!CollectionUtils.isEmpty(roles)) {
                userService.addRolesToUser(userId, roles.stream().map(RoleEntity::getId).toList());
            }

            return responseUtils.success("User added to group successfully");
        } catch (Exception e) {
            log.error("Error adding user {} to group {}: {}", userId, groupId, e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> removeUserFromGroup(Long userId, Long groupId) {
        try {
            var user = userService.getUserById(userId);
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_NOT_FOUND);
            }
            var group = groupService.getGroupById(groupId);
            if (group == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.GROUP_NOT_FOUND);
            }

            keycloakGroupService.removeUserFromGroup(user.getKeycloakId(), group.getKeycloakGroupId());
            groupService.removeUserFromGroup(userId, groupId);

            // Implement later: Remove roles associated with the group from the user

            return responseUtils.success("User removed from group successfully");
        } catch (Exception e) {
            log.error("Error removing user {} from group {}: {}", userId, groupId, e.getMessage());
            throw e;
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> assignRolesToGroup(Long groupId, AssignRolesToGroupDto request) {
        try {
            var group = groupService.getGroupById(groupId);
            if (group == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.GROUP_NOT_FOUND);
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
            keycloakGroupService.assignRolesToGroup(group.getKeycloakGroupId(), realmRoles, clientRoles);

            groupService.assignRolesToGroup(groupId, roles);

            return responseUtils.success("Roles assigned to group successfully");
        } catch (Exception e) {
            log.error("Error assigning roles to group {}: {}", groupId, e.getMessage());
            throw e;
        }
    }
}
