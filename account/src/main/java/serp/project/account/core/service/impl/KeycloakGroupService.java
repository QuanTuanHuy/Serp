/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateKeycloakGroupDto;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.client.IKeycloakGroupPort;
import serp.project.account.core.service.IKeycloakGroupService;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakGroupService implements IKeycloakGroupService {

    private final IKeycloakGroupPort keycloakGroupPort;

    @Override
    public String createGroup(CreateKeycloakGroupDto request) {
        try {
            if (keycloakGroupPort.isGroupExists(request.getName())) {
                throw new AppException(Constants.ErrorMessage.GROUP_ALREADY_EXISTS);
            }
            return keycloakGroupPort.createGroup(request);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating group {}: {}", request.getName(), e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void addUserToGroup(String userId, String groupId) {
        try {
            keycloakGroupPort.addUserToGroup(userId, groupId);
        } catch (Exception e) {
            log.error("Unexpected error adding user to group {}: {}", groupId, e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void removeUserFromGroup(String userId, String groupId) {
        try {
            keycloakGroupPort.removeUserFromGroup(userId, groupId);
        } catch (Exception e) {
            log.error("Unexpected error removing user from group {}: {}", groupId, e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteGroup(String groupId) {
        try {
            keycloakGroupPort.deleteGroup(groupId);
        } catch (Exception e) {
            log.error("Unexpected error deleting group {}: {}", groupId, e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<String> getUserGroups(String userId) {
        try {
            return keycloakGroupPort.getUserGroups(userId);
        } catch (Exception e) {
            log.error("Error getting user groups for user {}: {}", userId, e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void assignRolesToGroup(String groupId, List<String> realmRoles, Map<String, List<String>> clientRoles) {
        try {
            keycloakGroupPort.assignRolesToGroup(groupId, realmRoles, clientRoles);
        } catch (Exception e) {
            log.error("Unexpected error assigning roles to group {}: {}", groupId, e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
