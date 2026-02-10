/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.mailservice.core.domain.dto.request.EmailTemplateRequest;
import serp.project.mailservice.core.domain.dto.response.EmailTemplateResponse;
import serp.project.mailservice.core.usecase.EmailTemplateUseCases;
import serp.project.mailservice.kernel.utils.AuthUtils;
import serp.project.mailservice.kernel.utils.ResponseUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/email-templates")
@Slf4j
public class EmailTemplateController {

    private final EmailTemplateUseCases emailTemplateUseCases;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @PostMapping
    public ResponseEntity<?> createTemplate(@Valid @RequestBody EmailTemplateRequest request) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID is required"));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalArgumentException("User ID is required"));

        log.info("Creating email template for tenant: {}", tenantId);

        EmailTemplateResponse response = emailTemplateUseCases.createTemplate(request, tenantId, userId);
        return ResponseEntity.status(200).body(responseUtils.success(response));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<?> getTemplate(@PathVariable Long templateId) {
        log.debug("Getting email template: {}", templateId);

        EmailTemplateResponse response = emailTemplateUseCases.getTemplate(templateId);
        return ResponseEntity.status(200).body(responseUtils.success(response));
    }

    @PutMapping("/{templateId}")
    public ResponseEntity<?> updateTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody EmailTemplateRequest request) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID is required"));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalArgumentException("User ID is required"));

        log.info("Updating email template: {} for tenant: {}", templateId, tenantId);

        EmailTemplateResponse response = emailTemplateUseCases.updateTemplate(templateId, request, tenantId, userId);
        return ResponseEntity.status(200).body(responseUtils.success(response));
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long templateId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID is required"));

        log.info("Deleting email template: {} for tenant: {}", templateId, tenantId);

        emailTemplateUseCases.deleteTemplate(templateId, tenantId);
        return ResponseEntity.status(200).body(responseUtils.status("Template deleted successfully"));
    }
}
