package serp.project.account.ui.controller.internal;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.usecase.UserUseCase;

@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/api/v1/users")
@Slf4j
public class InternalUserController {

    private final UserUseCase userUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfileById(@PathVariable Long id) {
        log.info("Received request to get user profile for id={}", id);
        var response = userUseCase.getUserProfile(id);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Long organizationId) {
        GetUserParams params = GetUserParams.builder()
                .page(page).pageSize(pageSize).sortBy(sortBy).sortDirection(sortDir)
                .search(search)
                .status(status)
                .roleId(roleId)
                .organizationId(organizationId)
                .build();
        log.info("Received request to get users with params: {}", params);
        var response = userUseCase.getUsers(params);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/batch")
    public ResponseEntity<?> getUsersByIds(@RequestParam List<Long> ids) {
        log.info("Received request to get users by ids={}", ids);
        var response = userUseCase.getUsersByIds(ids);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
