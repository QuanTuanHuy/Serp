/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.account.ui.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.dto.request.CreateMenuDisplayDto;
import serp.project.account.core.domain.dto.request.UpdateMenuDisplayDto;
import serp.project.account.core.usecase.MenuDisplayUseCase;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/menu-displays")
public class MenuDisplayController {
    private final MenuDisplayUseCase menuDisplayUseCase;

    @PostMapping
    public ResponseEntity<?> createMenuDisplay(@Valid @RequestBody CreateMenuDisplayDto request) {
        var response = menuDisplayUseCase.createMenuDisplay(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenuDisplay(@PathVariable Long id,
            @Valid @RequestBody UpdateMenuDisplayDto request) {
        var response = menuDisplayUseCase.updateMenuDisplay(id, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenuDisplay(@PathVariable Long id) {
        var response = menuDisplayUseCase.deleteMenuDisplay(id);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/get-by-module/{moduleId}")
    public ResponseEntity<?> getMenuDisplaysByModuleId(@PathVariable Long moduleId) {
        var response = menuDisplayUseCase.getMenuDisplaysByModuleId(moduleId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

}
