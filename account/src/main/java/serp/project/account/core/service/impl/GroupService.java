/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateGroupDto;
import serp.project.account.core.domain.entity.GroupEntity;
import serp.project.account.core.domain.entity.UserGroupEntity;
import serp.project.account.core.domain.entity.GroupRoleEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IGroupPort;
import serp.project.account.core.port.store.IUserGroupPort;
import serp.project.account.core.port.store.IGroupRolePort;
import serp.project.account.core.service.IGroupService;
import serp.project.account.infrastructure.store.mapper.GroupMapper;
import serp.project.account.kernel.utils.CollectionUtils;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService implements IGroupService {

    private final IGroupPort groupPort;
    private final IUserGroupPort userGroupPort;
    private final IGroupRolePort groupRolePort;

    private final GroupMapper groupMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupEntity createGroup(CreateGroupDto request, String keycloakGroupId) {
        if (groupPort.existsByName(request.getGroupName())) {
            throw new AppException(Constants.ErrorMessage.GROUP_ALREADY_EXISTS);
        }

        try {
            GroupEntity group = groupMapper.createGroupMapper(request, keycloakGroupId);
            return groupPort.save(group);
        } catch (Exception e) {
            log.error("Error creating group: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long groupId) {
        try {
            // Implement later
        } catch (Exception e) {
            log.error("Error deleting group: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public GroupEntity getGroupById(Long groupId) {
        return groupPort.getGroupById(groupId);
    }

    @Override
    public GroupEntity getGroupByName(String groupName) {
        return groupPort.getGroupByName(groupName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserToGroup(Long userId, Long groupId) {
        try {
            UserGroupEntity userGroup = UserGroupEntity.builder()
                    .userId(userId)
                    .groupId(groupId)
                    .build();
            userGroupPort.save(userGroup);
        } catch (Exception e) {
            log.error("Error adding user to group: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserFromGroup(Long userId, Long groupId) {
        try {
            // Implement later
        } catch (Exception e) {
            log.error("Error removing user from group: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToGroup(Long groupId, List<RoleEntity> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return;
        }
        try {
            List<GroupRoleEntity> groupRoles = roles.stream()
                    .map(role -> GroupRoleEntity.builder()
                            .groupId(groupId)
                            .roleId(role.getId())
                            .createdAt(Instant.now().toEpochMilli())
                            .updatedAt(Instant.now().toEpochMilli())
                            .build())
                    .collect(Collectors.toList());
            groupRolePort.saveAll(groupRoles);
        } catch (Exception e) {
            log.error("Error assigning roles to group: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
