/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.client;

import serp.project.account.core.domain.dto.request.CreateKeycloakUserDto;

import java.util.List;

public interface IKeycloakPort {
    String createUser(CreateKeycloakUserDto request);

    void assignRealmRoles(String userId, List<String> roleNames);

    void assignClientRoles(String userId, String clientId, List<String> roleNames);

    void deleteUser(String userId);

    boolean isUserExists(String username);

    String getClientSecret(String clientId);
}
