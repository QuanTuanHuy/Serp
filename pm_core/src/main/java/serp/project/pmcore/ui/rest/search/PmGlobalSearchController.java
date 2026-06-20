/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.search;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.search.query.global.PmGlobalSearchQuery;
import serp.project.pmcore.application.search.query.global.PmGlobalSearchQueryHandler;
import serp.project.pmcore.application.search.query.global.PmGlobalSearchResponseView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.SEARCH)
@RequiredArgsConstructor
public class PmGlobalSearchController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final PmGlobalSearchQueryHandler handler;

    @GetMapping
    public ResponseEntity<GeneralResponse<PmGlobalSearchResponseView>> search(
            @RequestParam String q,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Long currentProjectId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        PmGlobalSearchResponseView response = handler.handle(new PmGlobalSearchQuery(
                tenantId,
                userId,
                authUtils.getCurrentGroups(),
                q,
                limit,
                currentProjectId
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }
}
