/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import serp.project.account.core.domain.dto.request.CreateKeycloakGroupDto;

import java.util.List;
import java.util.Map;

public interface IKeycloakGroupService {
    String createGroup(CreateKeycloakGroupDto request);

    void addUserToGroup(String userId, String groupId);

    void removeUserFromGroup(String userId, String groupId);

    void deleteGroup(String groupId);

    List<String> getUserGroups(String userId);

    void assignRolesToGroup(String groupId, List<String> realmRoles, Map<String, List<String>> clientRoles);
}
