/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message REST Controller
 */

package serp.project.discuss_service.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import serp.project.discuss_service.core.domain.dto.GeneralResponse;
import serp.project.discuss_service.core.domain.dto.request.*;
import serp.project.discuss_service.core.domain.dto.response.MessageResponse;
import serp.project.discuss_service.core.domain.dto.response.PaginatedResponse;
import serp.project.discuss_service.core.domain.dto.response.TypingStatusResponse;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.service.IAttachmentUrlService;
import serp.project.discuss_service.core.usecase.MessageUseCase;
import serp.project.discuss_service.kernel.utils.AuthUtils;
import serp.project.discuss_service.kernel.utils.JsonUtils;
import serp.project.discuss_service.kernel.utils.ResponseUtils;

import java.util.List;
import java.util.Set;

/**
 * REST Controller for message operations
 */
@RestController
@RequestMapping("/api/v1/channels/{channelId}/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageUseCase messageUseCase;
    private final IAttachmentUrlService attachmentUrlService;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final JsonUtils jsonUtils;

    /**
     * Send a new message to channel
     */
    @PostMapping
    public ResponseEntity<GeneralResponse<MessageResponse>> sendMessage(
            @PathVariable Long channelId,
            @Valid @RequestBody SendMessageRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException("Tenant ID not found"));

        log.info("User {} sending message to channel {}", userId, channelId);

        MessageEntity message = messageUseCase.sendMessage(
                channelId,
                userId,
                tenantId,
                request.getContent(),
                request.getMentions()
        );

        MessageResponse response = attachmentUrlService.enrichMessageWithUrls(message);
        response.setIsSentByMe(true);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    /**
     * Send a message with file attachments (multipart upload)
     */
    @PostMapping(value = "/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GeneralResponse<MessageResponse>> sendMessageWithFiles(
            @PathVariable Long channelId,
            @RequestPart(value = "content", required = false) String content,
            @RequestPart(value = "mentions", required = false) String mentionsJson,
            @RequestPart(value = "files") List<MultipartFile> files) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException("Tenant ID not found"));

        log.info("User {} sending message with {} files to channel {}", userId, files.size(), channelId);

        List<Long> mentions = null;
        if (mentionsJson != null && !mentionsJson.isEmpty()) {
            try {
                mentions = jsonUtils.fromJsonToList(mentionsJson, Long.class);
            } catch (Exception e) {
                log.warn("Failed to parse mentions: {}", e.getMessage());
            }
        }

        MessageEntity message = messageUseCase.sendMessageWithAttachments(
                channelId,
                userId,
                tenantId,
                content,
                mentions,
                files
        );

        MessageResponse response = attachmentUrlService.enrichMessageWithUrls(message);
        response.setIsSentByMe(true);
        
        return ResponseEntity.ok(responseUtils.success(response));
    }

    /**
     * Send a reply to a message (threading)
     */
    @PostMapping("/replies")
    public ResponseEntity<GeneralResponse<MessageResponse>> sendReply(
            @PathVariable Long channelId,
            @Valid @RequestBody SendReplyRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException("Tenant ID not found"));

        log.info("User {} sending reply to message {} in channel {}", 
                userId, request.getParentId(), channelId);

        MessageEntity message = messageUseCase.sendReply(
                channelId,
                request.getParentId(),
                userId,
                tenantId,
                request.getContent(),
                request.getMentions()
        );

        MessageResponse response = attachmentUrlService.enrichMessageWithUrls(message);
        response.setIsSentByMe(true);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    /**
     * Get messages in a channel with pagination
     */
    @GetMapping
    public ResponseEntity<GeneralResponse<PaginatedResponse<MessageResponse>>> getMessages(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));

        log.debug("User {} getting messages from channel {}, page {}", userId, channelId, page);

        Pair<Long, List<MessageEntity>> result = messageUseCase.getChannelMessages(
                channelId, userId, page, size);

        List<MessageResponse> messageResponses = result.getSecond().stream()
                .map(msg -> {
                    MessageResponse r = attachmentUrlService.enrichMessageWithUrls(msg);
                    r.setIsSentByMe(msg.getSenderId().equals(userId));
                    return r;
                })
                .toList();

        PaginatedResponse<MessageResponse> paginatedResponse = PaginatedResponse.of(
                messageResponses, page, size, result.getFirst());

        return ResponseEntity.ok(responseUtils.success(paginatedResponse));
    }

    /**
     * Get messages before a specific message ID (for infinite scroll)
     */
    @GetMapping("/before/{beforeId}")
    public ResponseEntity<GeneralResponse<List<MessageResponse>>> getMessagesBefore(
            @PathVariable Long channelId,
            @PathVariable Long beforeId,
            @RequestParam(defaultValue = "50") int limit) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));

        log.debug("User {} getting messages before {} in channel {}", userId, beforeId, channelId);

        List<MessageEntity> messages = messageUseCase.getMessagesBefore(
                channelId, userId, beforeId, limit);

        List<MessageResponse> responses = messages.stream()
                .map(msg -> {
                    MessageResponse r = attachmentUrlService.enrichMessageWithUrls(msg);
                    r.setIsSentByMe(msg.getSenderId().equals(userId));
                    return r;
                })
                .toList();

        return ResponseEntity.ok(responseUtils.success(responses));
    }

    /**
     * Get thread replies for a message
     */
    @GetMapping("/{messageId}/replies")
    public ResponseEntity<GeneralResponse<List<MessageResponse>>> getThreadReplies(
            @PathVariable Long channelId,
            @PathVariable Long messageId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));

        log.debug("User {} getting thread replies for message {} in channel {}", 
                userId, messageId, channelId);

        List<MessageEntity> messages = messageUseCase.getThreadReplies(channelId, messageId, userId);

        List<MessageResponse> responses = messages.stream()
                .map(msg -> {
                    MessageResponse r = attachmentUrlService.enrichMessageWithUrls(msg);
                    r.setIsSentByMe(msg.getSenderId().equals(userId));
                    return r;
                })
                .toList();

        return ResponseEntity.ok(responseUtils.success(responses));
    }

    /**
     * Search messages in a channel
     */
    @GetMapping("/search")
    public ResponseEntity<GeneralResponse<List<MessageResponse>>> searchMessages(
            @PathVariable Long channelId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));

        log.debug("User {} searching messages in channel {} with query: {}", 
                userId, channelId, query);

        List<MessageEntity> messages = messageUseCase.searchMessages(
                channelId, userId, query, page, size);

        List<MessageResponse> responses = messages.stream()
                .map(msg -> {
                    MessageResponse r = attachmentUrlService.enrichMessageWithUrls(msg);
                    r.setIsSentByMe(msg.getSenderId().equals(userId));
                    return r;
                })
                .toList();

        return ResponseEntity.ok(responseUtils.success(responses));
    }

    /**
     * Edit a message
     */
    @PutMapping("/{messageId}")
    public ResponseEntity<GeneralResponse<MessageResponse>> editMessage(
            @PathVariable Long channelId,
            @PathVariable Long messageId,
            @Valid @RequestBody EditMessageRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));

        log.info("User {} editing message {} in channel {}", userId, messageId, channelId);

        MessageEntity message = messageUseCase.editMessage(messageId, userId, request.getContent());
        MessageResponse response = attachmentUrlService.enrichMessageWithUrls(message);
        response.setIsSentByMe(true);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    /**
     * Delete a message
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<GeneralResponse<MessageResponse>> deleteMessage(
            @PathVariable Long channelId,
            @PathVariable Long messageId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));

        log.info("User {} deleting message {} in channel {}", userId, messageId, channelId);

        MessageEntity message = messageUseCase.deleteMessage(messageId, userId);
        MessageResponse response = attachmentUrlService.enrichMessageWithUrls(message);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    /**
     * Add reaction to a message
     */
    @PostMapping("/{messageId}/reactions")
    public ResponseEntity<GeneralResponse<MessageResponse>> addReaction(
            @PathVariable Long channelId,
            @PathVariable Long messageId,
            @Valid @RequestBody ReactionRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));

        log.debug("User {} adding reaction {} to message {} in channel {}", 
                userId, request.getEmoji(), messageId, channelId);

        MessageEntity message = messageUseCase.addReaction(messageId, userId, request.getEmoji());
        MessageResponse response = attachmentUrlService.enrichMessageWithUrls(message);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    /**
     * Remove reaction from a message
     */
    @DeleteMapping("/{messageId}/reactions")
    public ResponseEntity<GeneralResponse<MessageResponse>> removeReaction(
            @PathVariable Long channelId,
            @PathVariable Long messageId,
            @RequestParam String emoji) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));

        log.debug("User {} removing reaction {} from message {} in channel {}", 
                userId, emoji, messageId, channelId);

        MessageEntity message = messageUseCase.removeReaction(messageId, userId, emoji);
        MessageResponse response = attachmentUrlService.enrichMessageWithUrls(message);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    /**
     * Mark messages as read up to a specific message
     */
    @PostMapping("/{messageId}/read")
    public ResponseEntity<GeneralResponse<?>> markAsRead(
            @PathVariable Long channelId,
            @PathVariable Long messageId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));

        log.debug("User {} marking messages as read in channel {} up to message {}", 
                userId, channelId, messageId);

        messageUseCase.markAsRead(channelId, userId, messageId);
        return ResponseEntity.ok(responseUtils.status("Marked as read"));
    }

    /**
     * Get unread count for current user in this channel
     */
    @GetMapping("/unread/count")
    public ResponseEntity<GeneralResponse<Long>> getUnreadCount(
            @PathVariable Long channelId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));

        long count = messageUseCase.getUnreadCount(channelId, userId);
        return ResponseEntity.ok(responseUtils.success(count));
    }

    /**
     * Send typing indicator
     */
    @PostMapping("/typing")
    public ResponseEntity<GeneralResponse<?>> sendTypingIndicator(
            @PathVariable Long channelId,
            @Valid @RequestBody TypingIndicatorRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));

        messageUseCase.sendTypingIndicator(channelId, userId, request.getIsTyping());
        return ResponseEntity.ok(responseUtils.status("OK"));
    }

    /**
     * Get users currently typing in this channel
     */
    @GetMapping("/typing")
    public ResponseEntity<GeneralResponse<TypingStatusResponse>> getTypingUsers(
            @PathVariable Long channelId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException("Unauthorized"));

        Set<Long> typingUsers = messageUseCase.getTypingUsers(channelId, userId);
        
        TypingStatusResponse response = TypingStatusResponse.builder()
                .channelId(channelId)
                .typingUserIds(typingUsers)
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.ok(responseUtils.success(response));
    }
}
