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
import serp.project.mailservice.core.domain.dto.request.BulkEmailRequest;
import serp.project.mailservice.core.domain.dto.request.SendEmailRequest;
import serp.project.mailservice.core.domain.dto.response.EmailStatusResponse;
import serp.project.mailservice.core.domain.dto.response.SendEmailResponse;
import serp.project.mailservice.core.usecase.EmailSendingUseCases;
import serp.project.mailservice.kernel.utils.AuthUtils;
import serp.project.mailservice.kernel.utils.ResponseUtils;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/emails")
@Slf4j
public class EmailController {

    private final EmailSendingUseCases emailSendingUseCases;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @PostMapping
    public ResponseEntity<?> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID is required"));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalArgumentException("User ID is required"));
        String userEmail = authUtils.getCurrentUserEmail()
                .orElseThrow(() -> new IllegalArgumentException("User email is required"));

        log.info("Sending email for tenant: {}, user: {}", tenantId, userId);

        request.setFromEmail(userEmail);
        SendEmailResponse response = emailSendingUseCases.sendEmail(request, tenantId, userId);
        return ResponseEntity.status(200).body(responseUtils.success(response));
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> sendBulkEmail(@Valid @RequestBody BulkEmailRequest request) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID is required"));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalArgumentException("User ID is required"));

        log.info("Sending bulk email for tenant: {}, user: {}", tenantId, userId);

        List<SendEmailResponse> responses = emailSendingUseCases.sendBulkEmail(request, tenantId, userId);
        return ResponseEntity.status(200).body(responseUtils.success(responses));
    }

    @GetMapping("/{messageId}/status")
    public ResponseEntity<?> getEmailStatus(@PathVariable String messageId) {
        log.debug("Getting email status for messageId: {}", messageId);

        EmailStatusResponse response = emailSendingUseCases.getEmailStatus(messageId);
        return ResponseEntity.status(200).body(responseUtils.success(response));
    }

    @PostMapping("/{messageId}/resend")
    public ResponseEntity<?> resendFailedEmail(@PathVariable String messageId) {
        log.info("Resending failed email: {}", messageId);

        SendEmailResponse response = emailSendingUseCases.resendFailedEmail(messageId);
        return ResponseEntity.status(200).body(responseUtils.success(response));
    }
}
