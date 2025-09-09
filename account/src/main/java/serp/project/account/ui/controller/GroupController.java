/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.AssignRolesToGroupDto;
import serp.project.account.core.domain.dto.request.CreateGroupDto;
import serp.project.account.core.usecase.GroupUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupUseCase groupUseCase;

    @PostMapping
    public ResponseEntity<?> createGroup(@Valid @RequestBody CreateGroupDto request) {
        GeneralResponse<?> response = groupUseCase.createGroup(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupById(@PathVariable Long groupId) {
        GeneralResponse<?> response = groupUseCase.getGroupById(groupId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{groupId}/roles")
    public ResponseEntity<?> assignRolesToGroup(
            @PathVariable Long groupId,
            @RequestBody @Valid AssignRolesToGroupDto request) {
        GeneralResponse<?> response = groupUseCase.assignRolesToGroup(groupId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/name/{groupName}")
    public ResponseEntity<?> getGroupByName(@PathVariable String groupName) {
        GeneralResponse<?> response = groupUseCase.getGroupByName(groupName);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId) {
        GeneralResponse<?> response = groupUseCase.deleteGroup(groupId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{groupId}/users/{userId}")
    public ResponseEntity<?> addUserToGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        GeneralResponse<?> response = groupUseCase.addUserToGroup(userId, groupId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{groupId}/users/{userId}")
    public ResponseEntity<?> removeUserFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        GeneralResponse<?> response = groupUseCase.removeUserFromGroup(userId, groupId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
