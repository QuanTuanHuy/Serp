/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.CreateNoteRequest;
import serp.project.crm.core.domain.dto.request.UpdateNoteRequest;
import serp.project.crm.core.usecase.NoteUseCase;
import serp.project.crm.kernel.utils.AuthUtils;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteUseCase noteUseCase;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> createNote(@Valid @RequestBody CreateNoteRequest request) {
        Long userId = authUtils.getCurrentUserId().orElse(null);
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (userId == null || tenantId == null) {
            return ResponseEntity.status(401).build();
        }

        var response = noteUseCase.createNote(request, userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNoteRequest request) {
        Long userId = authUtils.getCurrentUserId().orElse(null);
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (userId == null || tenantId == null) {
            return ResponseEntity.status(401).build();
        }

        var response = noteUseCase.updateNote(id, request.getContent(), userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getNotes(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return ResponseEntity.status(401).build();
        }

        PageRequest pageRequest = PageRequest.builder().page(page).size(size).build();
        var response = noteUseCase.getNotesByEntity(entityType, entityId, tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return ResponseEntity.status(401).build();
        }

        var response = noteUseCase.deleteNote(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
