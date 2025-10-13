/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import serp.project.account.core.domain.dto.request.CreateKeycloakUserDto;

import java.util.List;
import java.util.Map;

public interface IKeycloakUserService {
    String createUser(CreateKeycloakUserDto request);

    void assignRealmRoles(String userId, List<String> roleNames);

    void assignClientRoles(String userId, String clientId, List<String> roleNames);

    void deleteUser(String userId);

    void updateUserAttributes(String userId, Map<String, List<String>> attributes);

    Map<String, List<String>> getUserAttributes(String userId);
}
