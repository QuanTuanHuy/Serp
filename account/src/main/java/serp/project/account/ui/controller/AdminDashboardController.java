/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.usecase.AdminDashboardUseCase;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Slf4j
public class AdminDashboardController {
    private final AdminDashboardUseCase adminDashboardUseCase;

    @GetMapping
    public ResponseEntity<?> getDashboard() {
        log.info("GET /api/v1/admin/dashboard - Getting admin dashboard");
        var response = adminDashboardUseCase.getDashboard();
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
