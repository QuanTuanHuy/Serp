/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.usecase.SettingsSearchUseCase;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/settings/search")
public class SettingsSearchController {
    private final SettingsSearchUseCase settingsSearchUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @GetMapping
    public ResponseEntity<?> search(
            @PathVariable Long organizationId,
            @RequestParam String q,
            @RequestParam(required = false) Integer limit) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        var response = settingsSearchUseCase.search(organizationId, q, limit);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
