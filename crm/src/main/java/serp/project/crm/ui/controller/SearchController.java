/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.crm.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.crm.core.usecase.SearchUseCase;
import serp.project.crm.kernel.utils.AuthUtils;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchUseCase searchUseCase;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<?> globalSearch(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));

        log.info("GET /api/v1/search - Global search for tenant {} with query '{}'", tenantId, query);
        var response = searchUseCase.globalSearch(query, tenantId, limit);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
