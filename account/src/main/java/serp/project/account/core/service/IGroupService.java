/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import serp.project.account.core.domain.dto.request.CreateGroupDto;
import serp.project.account.core.domain.entity.GroupEntity;
import serp.project.account.core.domain.entity.RoleEntity;

import java.util.List;

public interface IGroupService {
    GroupEntity createGroup(CreateGroupDto request, String keycloakGroupId);

    void deleteGroup(Long groupId);

    GroupEntity getGroupById(Long groupId);

    GroupEntity getGroupByName(String groupName);

    void addUserToGroup(Long userId, Long groupId);

    void removeUserFromGroup(Long userId, Long groupId);

    void assignRolesToGroup(Long groupId, List<RoleEntity> roles);
}
