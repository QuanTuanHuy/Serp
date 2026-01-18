/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message REST Controller
 */

package serp.project.discuss_service.ui.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.discuss_service.core.domain.dto.GeneralResponse;
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse.UserInfo;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.service.IUserInfoService;
import serp.project.discuss_service.kernel.utils.AuthUtils;
import serp.project.discuss_service.kernel.utils.ResponseUtils;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final IUserInfoService userInfoService;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @GetMapping
    public ResponseEntity<GeneralResponse<List<UserInfo>>> getUsersForTenant(
            @RequestParam(required = false) String query) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException("Tenant ID not found"));
        List<UserInfo> users = userInfoService.getUsersForTenant(tenantId, query);
        return ResponseEntity.ok(responseUtils.success(users));
    }
}
