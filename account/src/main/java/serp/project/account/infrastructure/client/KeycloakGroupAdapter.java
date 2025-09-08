/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.client;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateKeycloakGroupDto;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.client.IKeycloakGroupPort;
import serp.project.account.kernel.property.KeycloakProperties;
import serp.project.account.kernel.utils.CollectionUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakGroupAdapter implements IKeycloakGroupPort {

    private final Keycloak keycloakAdmin;
    private final KeycloakProperties keycloakProperties;

    @Override
    public String createGroup(CreateKeycloakGroupDto request) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        GroupsResource groupsResource = realmResource.groups();

        GroupRepresentation group = new GroupRepresentation();
        group.setName(request.getName());
        group.setAttributes(Map.of("description", List.of(request.getDescription())));

        Response response;
        if (request.getParentGroupId() != null) {
            GroupResource parentGroup = groupsResource.group(request.getParentGroupId());
            response = parentGroup.subGroup(group);
        } else {
            response = groupsResource.add(group);
        }

        if (response.getStatus() == 201) {
            URI location = response.getLocation();
            return extractGroupIdFromLocation(location);
        } else {
            log.error("Failed to create group. Status: {}", response.getStatus());
            throw new AppException(Constants.ErrorMessage.CREATE_GROUP_KEYCLOAK_FAILED);
        }
    }

    @Override
    public void addUserToGroup(String userId, String groupId) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        UserResource userResource = realmResource.users().get(userId);
        userResource.joinGroup(groupId);
    }

    @Override
    public void removeUserFromGroup(String userId, String groupId) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        UserResource userResource = realmResource.users().get(userId);
        userResource.leaveGroup(groupId);
    }

    @Override
    public void deleteGroup(String groupId) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        GroupsResource groupsResource = realmResource.groups();
        groupsResource.group(groupId).remove();
    }

    @Override
    public boolean isGroupExists(String groupName) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        GroupsResource groupsResource = realmResource.groups();

        return groupsResource.groups(groupName, 0, 1).stream()
                .anyMatch(group -> group.getName().equals(groupName));
    }

    @Override
    public List<String> getUserGroups(String userId) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        UserResource userResource = realmResource.users().get(userId);

        return userResource.groups().stream()
                .map(GroupRepresentation::getId)
                .collect(Collectors.toList());
    }

    @Override
    public void assignRolesToGroup(String groupId, List<String> realmRoles, Map<String, List<String>> clientRoles) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        GroupResource groupResource = realmResource.groups().group(groupId);

        if (!CollectionUtils.isEmpty(realmRoles)) {
            assignRealmRolesToGroup(realmResource, groupResource, realmRoles);
        }

        if (!CollectionUtils.isEmpty(clientRoles)) {
            for (var entry : clientRoles.entrySet()) {
                String clientId = entry.getKey();
                List<String> roles = entry.getValue();
                assignClientRolesToGroup(realmResource, groupResource, clientId, roles);
            }
        }
    }

    private void assignRealmRolesToGroup(RealmResource realmResource, GroupResource groupResource,
            List<String> roleNames) {
        List<RoleRepresentation> roles = roleNames.stream()
                .map(roleName -> realmResource.roles().get(roleName).toRepresentation())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        groupResource.roles().realmLevel().add(roles);
    }

    private void assignClientRolesToGroup(RealmResource realmResource, GroupResource groupResource,
            String clientId, List<String> roleNames) {
        var clients = realmResource.clients().findByClientId(clientId);
        if (clients.isEmpty()) {
            log.error("Client not found: {}", clientId);
            throw new AppException("Client not found: " + clientId);
        }
        var client = clients.getFirst();

        List<RoleRepresentation> roles = roleNames.stream()
                .map(roleName -> realmResource.clients().get(client.getId()).roles().get(roleName)
                        .toRepresentation())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        groupResource.roles().clientLevel(client.getId()).add(roles);
    }

    private String extractGroupIdFromLocation(URI location) {
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

}
