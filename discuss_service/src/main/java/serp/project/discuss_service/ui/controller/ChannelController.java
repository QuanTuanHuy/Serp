/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel REST Controller
 */

package serp.project.discuss_service.ui.controller;

import jakarta.validation.Valid;
import io.github.serp.platform.security.context.SerpAuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.discuss_service.core.domain.dto.GeneralResponse;
import serp.project.discuss_service.core.domain.dto.request.*;
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;
import serp.project.discuss_service.core.domain.dto.response.ChannelResponse;
import serp.project.discuss_service.core.domain.dto.response.PaginatedResponse;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.service.IUserInfoService;
import serp.project.discuss_service.core.usecase.ChannelUseCase;
import serp.project.discuss_service.kernel.utils.ResponseUtils;

import java.util.List;

@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
@Slf4j
public class ChannelController {

    private final ChannelUseCase channelUseCase;
    private final SerpAuthContext authContext;
    private final ResponseUtils responseUtils;
    private final IUserInfoService userInfoService;

    @PostMapping("/group")
    public ResponseEntity<GeneralResponse<ChannelResponse>> createGroupChannel(
            @Valid @RequestBody CreateGroupChannelRequest request) {
        Long userId = authContext.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authContext.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_ID_REQUIRED));

        log.info("User {} creating group channel: {}", userId, request.getName());

        ChannelEntity channel = channelUseCase.createGroupChannel(
                tenantId,
                userId,
                request.getName(),
                request.getDescription(),
                Boolean.TRUE.equals(request.getIsPrivate()),
                request.getMemberIds()
        );

        ChannelResponse response = ChannelResponse.fromEntity(channel);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PostMapping("/direct")
    public ResponseEntity<GeneralResponse<ChannelResponse>> createDirectChannel(
            @Valid @RequestBody CreateDirectChannelRequest request) {
        Long userId = authContext.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authContext.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_ID_REQUIRED));

        log.info("User {} creating/getting direct channel with user {}", userId, request.getOtherUserId());

        ChannelEntity channel = channelUseCase.getOrCreateDirectChannel(
                tenantId,
                userId,
                request.getOtherUserId()
        );

        ChannelResponse response = ChannelResponse.fromEntity(channel);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PostMapping("/topic")
    public ResponseEntity<GeneralResponse<ChannelResponse>> createTopicChannel(
            @Valid @RequestBody CreateTopicChannelRequest request) {
        Long userId = authContext.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authContext.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_ID_REQUIRED));

        log.info("User {} creating topic channel: {} for {}/{}", 
                userId, request.getName(), request.getEntityType(), request.getEntityId());

        ChannelEntity channel = channelUseCase.createTopicChannel(
                tenantId,
                userId,
                request.getName(),
                request.getEntityType(),
                request.getEntityId(),
                request.getMemberIds()
        );

        ChannelResponse response = ChannelResponse.fromEntity(channel);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<GeneralResponse<ChannelResponse>> getChannel(
            @PathVariable Long channelId) {
        Long userId = authContext.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        log.debug("User {} getting channel {}", userId, channelId);

        ChannelEntity channel = channelUseCase.getChannelWithMembers(channelId);
        ChannelResponse response = ChannelResponse.fromEntity(channel);
        
        if (channel.getMembers() != null) {
            List<ChannelMemberResponse> memberResponses = userInfoService.enrichMembersWithUserInfo(channel.getMembers());
            response.setMembers(memberResponses);
        }

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{channelId}/members")
    public ResponseEntity<GeneralResponse<List<ChannelMemberResponse>>> getChannelMembers(
            @PathVariable Long channelId) {
        Long userId = authContext.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        log.debug("User {} getting members of channel {}", userId, channelId);

        List<ChannelMemberEntity> members = channelUseCase.getChannelMembers(channelId, userId);
                
        List<ChannelMemberResponse> responses = userInfoService.enrichMembersWithUserInfo(members);
        
        return ResponseEntity.ok(responseUtils.success(responses));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PaginatedResponse<ChannelResponse>>> getMyChannels() {
        Long userId = authContext.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authContext.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_ID_REQUIRED));

        log.debug("User {} getting their channels", userId);

        List<ChannelEntity> channels = channelUseCase.getUserChannels(userId, tenantId);
        List<ChannelResponse> responses = channels.stream()
                .map(ChannelResponse::fromEntity)
                .toList();

        // TODO: Implement proper pagination
        PaginatedResponse<ChannelResponse> paginatedResponse = PaginatedResponse.of(
                responses,
                1,
                responses.size(),
                responses.size()
        );
        return ResponseEntity.ok(responseUtils.success(paginatedResponse));
    }

    @PutMapping("/{channelId}")
    public ResponseEntity<GeneralResponse<ChannelResponse>> updateChannel(
            @PathVariable Long channelId,
            @Valid @RequestBody UpdateChannelRequest request) {
        Long userId = authContext.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        log.info("User {} updating channel {}", userId, channelId);

        ChannelEntity channel = channelUseCase.updateChannel(
                channelId,
                userId,
                request.getName(),
                request.getDescription()
        );

        ChannelResponse response = ChannelResponse.fromEntity(channel);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PostMapping("/{channelId}/archive")
    public ResponseEntity<GeneralResponse<ChannelResponse>> archiveChannel(
            @PathVariable Long channelId) {
        Long userId = authContext.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        log.info("User {} archiving channel {}", userId, channelId);

        ChannelEntity channel = channelUseCase.archiveChannel(channelId, userId);
        ChannelResponse response = ChannelResponse.fromEntity(channel);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<GeneralResponse<?>> deleteChannel(
            @PathVariable Long channelId) {
        Long userId = authContext.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        log.info("User {} deleting channel {}", userId, channelId);

        channelUseCase.deleteChannel(channelId, userId);
        return ResponseEntity.ok(responseUtils.status("Channel deleted successfully"));
    }

    @PostMapping("/{channelId}/members")
    public ResponseEntity<GeneralResponse<ChannelMemberResponse>> addMember(
            @PathVariable Long channelId,
            @RequestParam Long userId) {
        Long currentUserId = authContext.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authContext.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_ID_REQUIRED));

        log.info("User {} adding user {} to channel {}", currentUserId, userId, channelId);

        ChannelMemberEntity member = channelUseCase.addMember(channelId, userId, currentUserId, tenantId);
        ChannelMemberResponse response = ChannelMemberResponse.fromEntity(member);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{channelId}/members/{userId}")
    public ResponseEntity<GeneralResponse<ChannelMemberResponse>> removeMember(
            @PathVariable Long channelId,
            @PathVariable Long userId) {
        Long currentUserId = authContext.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        log.info("User {} removing user {} from channel {}", currentUserId, userId, channelId);

        ChannelMemberEntity member = channelUseCase.removeMember(channelId, userId, currentUserId);
        ChannelMemberResponse response = ChannelMemberResponse.fromEntity(member);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PostMapping("/{channelId}/leave")
    public ResponseEntity<GeneralResponse<ChannelMemberResponse>> leaveChannel(
            @PathVariable Long channelId) {
        Long userId = authContext.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        log.info("User {} leaving channel {}", userId, channelId);

        ChannelMemberEntity member = channelUseCase.leaveChannel(channelId, userId);
        ChannelMemberResponse response = ChannelMemberResponse.fromEntity(member);
        return ResponseEntity.ok(responseUtils.success(response));
    }
}
