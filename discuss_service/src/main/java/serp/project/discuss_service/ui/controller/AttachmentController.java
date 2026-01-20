/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Attachment REST Controller
 */

package serp.project.discuss_service.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.discuss_service.core.domain.dto.GeneralResponse;
import serp.project.discuss_service.core.domain.dto.response.AttachmentResponse;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.usecase.AttachmentUseCase;
import serp.project.discuss_service.kernel.utils.AuthUtils;
import serp.project.discuss_service.kernel.utils.ResponseUtils;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for attachment operations.
 * Provides endpoints for managing file attachments in messages.
 */
@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
@Slf4j
public class AttachmentController {

    private final AttachmentUseCase attachmentUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    /**
     * Get attachment metadata by ID
     */
    @GetMapping("/{attachmentId}")
    public ResponseEntity<GeneralResponse<AttachmentResponse>> getAttachment(
            @PathVariable Long attachmentId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_ID_REQUIRED));

        log.debug("Getting attachment metadata: {}", attachmentId);

        AttachmentResponse response = attachmentUseCase.getAttachment(attachmentId, tenantId);

        return ResponseEntity.ok(responseUtils.success(response));
    }

    /**
     * Get all attachments for a message
     */
    @GetMapping("/message/{messageId}")
    public ResponseEntity<GeneralResponse<List<AttachmentResponse>>> getAttachmentsByMessage(
            @PathVariable Long messageId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_ID_REQUIRED));

        log.debug("Getting attachments for message: {}", messageId);

        List<AttachmentResponse> responses = attachmentUseCase.getAttachmentsByMessage(messageId, tenantId);

        return ResponseEntity.ok(responseUtils.success(responses));
    }

    /**
     * Generate a presigned download URL for an attachment
     */
    @GetMapping("/{attachmentId}/download-url")
    public ResponseEntity<GeneralResponse<Map<String, String>>> getDownloadUrl(
            @PathVariable Long attachmentId,
            @RequestParam(defaultValue = "60") int expirationMinutes) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_ID_REQUIRED));

        log.info("Generating download URL for attachment: {}", attachmentId);

        String downloadUrl = attachmentUseCase.getDownloadUrl(attachmentId, tenantId, expirationMinutes);

        Map<String, String> response = Map.of(
                "attachmentId", attachmentId.toString(),
                "downloadUrl", downloadUrl,
                "expiresInMinutes", String.valueOf(expirationMinutes)
        );

        return ResponseEntity.ok(responseUtils.success(response));
    }

    /**
     * Delete an attachment
     */
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<GeneralResponse<?>> deleteAttachment(
            @PathVariable Long attachmentId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_ID_REQUIRED));

        log.info("User {} deleting attachment: {}", userId, attachmentId);

        attachmentUseCase.deleteAttachment(attachmentId, tenantId, userId);

        return ResponseEntity.ok(responseUtils.status("Attachment deleted successfully"));
    }
}
