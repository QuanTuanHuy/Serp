/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.client;

import serp.project.account.core.domain.dto.request.CreateKeycloakUserDto;

import java.util.List;
import java.util.Map;

public interface IKeycloakPort {
    String createUser(CreateKeycloakUserDto request);

    void assignRealmRoles(String userId, List<String> roleNames);

    void assignClientRoles(String userId, String clientId, List<String> roleNames);

    void revokeClientRoles(String userId, String clientId, List<String> roleNames);

    void deleteUser(String userId);

    boolean isUserExists(String username);

    String getClientSecret(String clientId);

    void updateUserAttributes(String userId, Map<String, List<String>> attributes);

    Map<String, List<String>> getUserAttributes(String userId);

    void resetPassword(String userId, String newPassword);
}
