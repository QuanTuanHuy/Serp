package serp.project.account.infrastructure.client;

import io.micrometer.common.util.StringUtils;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateKeycloakUserDto;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.client.IKeycloakPort;
import serp.project.account.kernel.property.KeycloakProperties;
import serp.project.account.kernel.utils.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeycloakAdapter implements IKeycloakPort {

    private final Keycloak keycloakAdmin;

    private final KeycloakProperties keycloakProperties;

    @Override
    public String createUser(CreateKeycloakUserDto request) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        UsersResource usersResource = realmResource.users();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(request.isEmailVerified());

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("uid", List.of(request.getUid().toString()));
        user.setAttributes(attributes);

        if (!StringUtils.isEmpty(request.getPassword())) {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(request.isTemporaryPassword());
            user.setCredentials(List.of(credential));
        }

        Response response = usersResource.create(user);

        if (response.getStatus() != 201) {
            throw new AppException(Constants.ErrorMessage.CREATE_USER_FAILED);
        }

        return extractUserIdFromLocation(response.getLocation());
    }

    @Override
    public void assignRoles(String userId, List<String> roleNames) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        UserResource user = realmResource.users().get(userId);

        List<RoleRepresentation> rolesToAssign = roleNames.stream()
                .map(roleName -> {
                    try {
                        return realmResource.roles().get(roleName).toRepresentation();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        if (!CollectionUtils.isEmpty(rolesToAssign)) {
            user.roles().realmLevel().add(rolesToAssign);
        }
    }

    @Override
    public void assignClientRoles(String userId, String clientId, List<String> roleNames) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        UserResource user = realmResource.users().get(userId);

        String clientUuid = getClientUuid(clientId);
        ClientResource clientResource = realmResource.clients().get(clientUuid);

        List<RoleRepresentation> rolesToAssign = roleNames.stream()
                .map(roleName -> {
                    try {
                        return clientResource.roles().get(roleName).toRepresentation();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        if (!CollectionUtils.isEmpty(rolesToAssign)) {
            user.roles().clientLevel(clientUuid).add(rolesToAssign);
        }
    }

    @Override
    public void deleteUser(String userId) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        realmResource.users().delete(userId);
    }

    @Override
    public boolean isUserExists(String username) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        var users = realmResource.users().search(username, true);
        return !CollectionUtils.isEmpty(users);
    }

    private String extractUserIdFromLocation(java.net.URI location) {
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private String getClientUuid(String clientId) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
            return realmResource.clients().findByClientId(clientId).getFirst().getId();
        } catch (Exception e) {
            throw new AppException(Constants.ErrorMessage.CLIENT_NOT_FOUND);
        }
    }
}
