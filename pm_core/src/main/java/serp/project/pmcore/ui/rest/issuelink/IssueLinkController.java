/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.issuelink;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.issuelink.IssueLinkView;
import serp.project.pmcore.application.issuelink.command.create.CreateIssueLinkCommand;
import serp.project.pmcore.application.issuelink.command.create.CreateIssueLinkCommandHandler;
import serp.project.pmcore.application.issuelink.command.delete.DeleteIssueLinkCommand;
import serp.project.pmcore.application.issuelink.command.delete.DeleteIssueLinkCommandHandler;
import serp.project.pmcore.application.issuelink.command.delete.DeleteIssueLinkResult;
import serp.project.pmcore.application.workitem.query.links.ListWorkItemLinksQuery;
import serp.project.pmcore.application.workitem.query.links.ListWorkItemLinksQueryHandler;
import serp.project.pmcore.application.workitem.query.links.WorkItemLinkView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.issuelink.dto.request.CreateIssueLinkRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

import java.util.List;

@RestController
@RequestMapping(PathConstants.ISSUE_LINKS)
@RequiredArgsConstructor
public class IssueLinkController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateIssueLinkCommandHandler createIssueLinkCommandHandler;
    private final DeleteIssueLinkCommandHandler deleteIssueLinkCommandHandler;
    private final ListWorkItemLinksQueryHandler listWorkItemLinksQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<IssueLinkView>> createIssueLink(
            @PathVariable Long projectId,
            @PathVariable Long workItemId,
            @Valid @RequestBody CreateIssueLinkRequest request) {
        IssueLinkView response = createIssueLinkCommandHandler.handle(new CreateIssueLinkCommand(
                projectId,
                workItemId,
                request.getTargetId(),
                request.getLinkTypeId(),
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @DeleteMapping("/{linkId}")
    public ResponseEntity<GeneralResponse<DeleteIssueLinkResult>> deleteIssueLink(
            @PathVariable Long projectId,
            @PathVariable Long workItemId,
            @PathVariable Long linkId) {
        DeleteIssueLinkResult response = deleteIssueLinkCommandHandler.handle(new DeleteIssueLinkCommand(
                projectId,
                workItemId,
                linkId,
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<List<WorkItemLinkView>>> listIssueLinks(
            @PathVariable Long projectId,
            @PathVariable Long workItemId) {
        List<WorkItemLinkView> response = listWorkItemLinksQueryHandler.handle(new ListWorkItemLinksQuery(
                projectId,
                workItemId,
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    private Long requireCurrentUserId() {
        return authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
    }

    private Long requireCurrentTenantId() {
        return authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));
    }
}
