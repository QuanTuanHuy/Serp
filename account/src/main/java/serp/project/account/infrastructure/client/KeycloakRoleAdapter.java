package serp.project.account.infrastructure.client;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateClientRoleDto;
import serp.project.account.core.domain.dto.request.CreateRealmRoleDto;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.client.IKeycloakRolePort;
import serp.project.account.kernel.property.KeycloakProperties;

@Component
@RequiredArgsConstructor
public class KeycloakRoleAdapter implements IKeycloakRolePort {

    private final Keycloak keycloakAdmin;

    private final KeycloakProperties keycloakProperties;

    @Override
    public void createRealmRole(CreateRealmRoleDto request) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        RolesResource rolesResource = realmResource.roles();

        RoleRepresentation role = new RoleRepresentation();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        rolesResource.create(role);
    }

    @Override
    public void deleteRealmRole(String roleName) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        RolesResource rolesResource = realmResource.roles();
        rolesResource.deleteRole(roleName);
    }

    @Override
    public void createClientRole(CreateClientRoleDto request) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        ClientResource clientResource = realmResource.clients().get(getClientUuid(request.getClientId()));
        RolesResource rolesResource = clientResource.roles();

        RoleRepresentation role = new RoleRepresentation();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        rolesResource.create(role);
    }

    @Override
    public void deleteClientRole(String clientId, String roleName) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        ClientResource clientResource = realmResource.clients().get(getClientUuid(clientId));
        RolesResource rolesResource = clientResource.roles();
        rolesResource.deleteRole(roleName);
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
