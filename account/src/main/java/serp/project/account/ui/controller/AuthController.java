/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.account.core.domain.dto.request.CreateUserDto;
import serp.project.account.core.domain.dto.request.LoginRequest;
import serp.project.account.core.domain.dto.request.RefreshTokenRequest;
import serp.project.account.core.domain.dto.request.RevokeTokenRequest;
import serp.project.account.core.usecase.AuthUseCase;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthUseCase authUseCase;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserDto request) {
        var response = authUseCase.registerUser(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        var response = authUseCase.login(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/get-token")
    public ResponseEntity<?> getToken(@Valid @RequestBody LoginRequest request) {
        var response = authUseCase.getUserToken(request.getEmail(), request.getPassword());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        var response = authUseCase.refreshToken(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/revoke-token")
    public ResponseEntity<?> revokeToken(@Valid @RequestBody RevokeTokenRequest request) {
        var response = authUseCase.revokeToken(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
