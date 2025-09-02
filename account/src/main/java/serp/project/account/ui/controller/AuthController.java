/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.account.core.domain.dto.request.CreateUserDto;
import serp.project.account.core.domain.dto.request.LoginRequest;
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
}
