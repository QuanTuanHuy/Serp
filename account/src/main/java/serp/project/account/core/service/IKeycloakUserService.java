package serp.project.account.core.service;

import serp.project.account.core.domain.dto.request.CreateKeycloakUserDto;

import java.util.List;

public interface IKeycloakUserService {
    String createUser(CreateKeycloakUserDto request);
    void assignRoles(String userId, List<String> roleNames);
    void assignClientRoles(String userId, String clientId, List<String> roleNames);
    void deleteUser(String userId);
}
