/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.port.client.IKeycloakPort;
import serp.project.account.core.service.IKeycloakService;

@Service
@RequiredArgsConstructor
public class KeycloakService implements IKeycloakService {
    private final IKeycloakPort keycloakPort;

    @Override
    public String getClientSecret(String clientId) {
        return keycloakPort.getClientSecret(clientId);
    }

}
