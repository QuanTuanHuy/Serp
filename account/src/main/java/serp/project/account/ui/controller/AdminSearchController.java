/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.usecase.AdminSearchUseCase;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/search")
public class AdminSearchController {
    private final AdminSearchUseCase adminSearchUseCase;

    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam String q,
            @RequestParam(required = false) Integer limit) {
        var response = adminSearchUseCase.search(q, limit);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
