package serp.project.account.core.port.client;

import serp.project.account.core.domain.dto.request.CreateClientRoleDto;
import serp.project.account.core.domain.dto.request.CreateRealmRoleDto;

public interface IKeycloakRolePort {
    void createRealmRole(CreateRealmRoleDto request);
    void deleteRealmRole(String roleName);
    void createClientRole(CreateClientRoleDto request);
    void deleteClientRole(String clientId, String roleName);
}
