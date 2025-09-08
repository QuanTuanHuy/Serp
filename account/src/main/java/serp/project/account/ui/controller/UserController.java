/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.account.core.domain.dto.request.AssignRoleToUserDto;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.usecase.UserUseCase;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserUseCase userUseCase;

    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) String search
    ) {
        GetUserParams params = GetUserParams.builder()
                .page(page).pageSize(pageSize).sortBy(sortBy).sortDirection(sortDir).search(search)
                .build();
        var response = userUseCase.getUsers(params);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/assign-roles")
    public ResponseEntity<?> assignRoles(@Valid @RequestBody AssignRoleToUserDto request) {
        var response = userUseCase.assignRolesToUser(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

}
