/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.account.core.domain.dto.request.GetModulesParams;
import serp.project.account.core.usecase.ModuleUseCase;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/modules")
public class ModuleV2Controller {
    private final ModuleUseCase moduleUseCase;

    @GetMapping
    public ResponseEntity<?> getModulesPaginated(@Valid GetModulesParams params) {
        var response = moduleUseCase.getModulesPaginated(params);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
