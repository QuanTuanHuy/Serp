/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Presence REST Controller
 */

package serp.project.discuss_service.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.serp.platform.security.context.SerpAuthContext;
import serp.project.discuss_service.core.domain.dto.GeneralResponse;
import serp.project.discuss_service.core.domain.dto.request.UpdatePresenceStatusRequest;
import serp.project.discuss_service.core.domain.dto.response.ChannelPresenceResponse;
import serp.project.discuss_service.core.domain.dto.response.UserPresenceResponse;
import serp.project.discuss_service.core.domain.entity.UserPresenceEntity;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.usecase.PresenceUseCase;
import serp.project.discuss_service.kernel.utils.ResponseUtils;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PresenceController {

    private final PresenceUseCase presenceUseCase;

    private final SerpAuthContext authContext;
    private final ResponseUtils responseUtils;

    @GetMapping("/channels/{channelId}/presence")
    public ResponseEntity<GeneralResponse<ChannelPresenceResponse>> getChannelPresence(
            @PathVariable Long channelId
    ) {
        long userId = authContext.getCurrentUserId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        ChannelPresenceResponse response = presenceUseCase.getChannelPresence(channelId, userId);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/users/{usersId}/presence")
    public ResponseEntity<GeneralResponse<UserPresenceResponse>> getUserPresence(
            @PathVariable Long usersId
    ){
        long tenantId = authContext.getCurrentTenantId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        UserPresenceResponse response = presenceUseCase.getUserPresence(usersId, tenantId);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/users/me/presence")
    public ResponseEntity<GeneralResponse<UserPresenceResponse>> getMyPresence(){
        long userId = authContext.getCurrentUserId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        long tenantId = authContext.getCurrentTenantId().orElseThrow(() -> new AppException(ErrorCode.TENANT_ID_REQUIRED));

        UserPresenceResponse response = presenceUseCase.getUserPresence(userId, tenantId);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PatchMapping("/users/me/presence")
    public ResponseEntity<GeneralResponse<UserPresenceEntity>> updatePresenceStatus(
            @RequestBody @Valid UpdatePresenceStatusRequest request
    ){
        long userId = authContext.getCurrentUserId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        long tenantId = authContext.getCurrentTenantId().orElseThrow(() -> new AppException(ErrorCode.TENANT_ID_REQUIRED));

        UserPresenceEntity newPresence = presenceUseCase.updatePresenceStatus(userId, tenantId, request);
        return ResponseEntity.ok(responseUtils.success(newPresence));
    }
}
