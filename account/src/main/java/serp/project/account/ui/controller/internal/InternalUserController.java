package serp.project.account.ui.controller.internal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.usecase.UserUseCase;

@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/api/v1/users")
@Slf4j
public class InternalUserController {

    private final UserUseCase userUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfileById(@PathVariable Long id) {
        var response = userUseCase.getUserProfile(id);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
