package serp.project.account.core.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.dto.request.CreateClientRoleDto;
import serp.project.account.core.domain.dto.request.CreateRealmRoleDto;
import serp.project.account.core.port.client.IKeycloakRolePort;
import serp.project.account.core.service.IKeycloakRoleService;

@Service
@RequiredArgsConstructor
public class KeycloakRoleService implements IKeycloakRoleService {

    private final IKeycloakRolePort keycloakRolePort;
    
    @Override
    public void createRealmRole(CreateRealmRoleDto request) {
        keycloakRolePort.createRealmRole(request);
    }

    @Override
    public void deleteRealmRole(String roleName) {
        keycloakRolePort.deleteRealmRole(roleName);
    }

    @Override
    public void createClientRole(CreateClientRoleDto request) {
        keycloakRolePort.createClientRole(request);
    }

    @Override
    public void deleteClientRole(String clientId, String roleName) {
        keycloakRolePort.deleteClientRole(clientId, roleName);
    }

}
