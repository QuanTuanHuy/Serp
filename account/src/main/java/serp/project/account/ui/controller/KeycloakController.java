/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.usecase.KeycloakUseCase;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/keycloak")
public class KeycloakController {
    private final KeycloakUseCase keycloakUseCase;

    @GetMapping("/clients/{clientId}/client-secret")
    public ResponseEntity<?> getClientSecret(@PathVariable String clientId) {
        var response = keycloakUseCase.getClientSecret(clientId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
