/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.issuetypescheme;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.issuetypescheme.IssueTypeSchemeDetailView;
import serp.project.pmcore.application.issuetypescheme.IssueTypeSchemeView;
import serp.project.pmcore.application.issuetypescheme.command.create.CreateIssueTypeSchemeCommand;
import serp.project.pmcore.application.issuetypescheme.command.create.CreateIssueTypeSchemeCommandHandler;
import serp.project.pmcore.application.issuetypescheme.command.delete.DeleteIssueTypeSchemeCommand;
import serp.project.pmcore.application.issuetypescheme.command.delete.DeleteIssueTypeSchemeCommandHandler;
import serp.project.pmcore.application.issuetypescheme.command.delete.DeleteIssueTypeSchemeResult;
import serp.project.pmcore.application.issuetypescheme.command.manageitems.ManageIssueTypeSchemeItemsCommand;
import serp.project.pmcore.application.issuetypescheme.command.manageitems.ManageIssueTypeSchemeItemsCommandHandler;
import serp.project.pmcore.application.issuetypescheme.command.update.UpdateIssueTypeSchemeCommand;
import serp.project.pmcore.application.issuetypescheme.command.update.UpdateIssueTypeSchemeCommandHandler;
import serp.project.pmcore.application.issuetypescheme.query.get.GetIssueTypeSchemeByIdQuery;
import serp.project.pmcore.application.issuetypescheme.query.get.GetIssueTypeSchemeByIdQueryHandler;
import serp.project.pmcore.application.issuetypescheme.query.list.ListIssueTypeSchemesQuery;
import serp.project.pmcore.application.issuetypescheme.query.list.ListIssueTypeSchemesQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.issuetypescheme.dto.request.CreateIssueTypeSchemeRequest;
import serp.project.pmcore.ui.rest.issuetypescheme.dto.request.ManageIssueTypeSchemeItemsRequest;
import serp.project.pmcore.ui.rest.issuetypescheme.dto.request.UpdateIssueTypeSchemeRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.ISSUE_TYPE_SCHEMES)
@RequiredArgsConstructor
public class IssueTypeSchemeController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateIssueTypeSchemeCommandHandler createIssueTypeSchemeCommandHandler;
    private final UpdateIssueTypeSchemeCommandHandler updateIssueTypeSchemeCommandHandler;
    private final DeleteIssueTypeSchemeCommandHandler deleteIssueTypeSchemeCommandHandler;
    private final ManageIssueTypeSchemeItemsCommandHandler manageIssueTypeSchemeItemsCommandHandler;
    private final GetIssueTypeSchemeByIdQueryHandler getIssueTypeSchemeByIdQueryHandler;
    private final ListIssueTypeSchemesQueryHandler listIssueTypeSchemesQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<IssueTypeSchemeView>> createIssueTypeScheme(
            @Valid @RequestBody CreateIssueTypeSchemeRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        IssueTypeSchemeView response = createIssueTypeSchemeCommandHandler.handle(new CreateIssueTypeSchemeCommand(
                request.getName(),
                request.getDescription(),
                request.getDefaultIssueTypeId(),
                tenantId,
                userId
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<IssueTypeSchemeView>> updateIssueTypeScheme(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueTypeSchemeRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        IssueTypeSchemeView response = updateIssueTypeSchemeCommandHandler.handle(new UpdateIssueTypeSchemeCommand(
                id,
                request.toData(),
                tenantId,
                userId
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<IssueTypeSchemeDetailView>> getIssueTypeSchemeById(@PathVariable Long id) {
        Long tenantId = requireCurrentTenantId();
        IssueTypeSchemeDetailView response = getIssueTypeSchemeByIdQueryHandler.handle(
                new GetIssueTypeSchemeByIdQuery(id, tenantId)
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<IssueTypeSchemeView>>> listIssueTypeSchemes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Long tenantId = requireCurrentTenantId();
        PageView<IssueTypeSchemeView> response = listIssueTypeSchemesQueryHandler.handle(new ListIssueTypeSchemesQuery(
                tenantId,
                search,
                isSystem,
                page,
                pageSize,
                sortBy,
                sortDirection
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PutMapping("/{id}/items")
    public ResponseEntity<GeneralResponse<IssueTypeSchemeDetailView>> manageIssueTypeSchemeItems(
            @PathVariable Long id,
            @Valid @RequestBody ManageIssueTypeSchemeItemsRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        IssueTypeSchemeDetailView response = manageIssueTypeSchemeItemsCommandHandler.handle(
                new ManageIssueTypeSchemeItemsCommand(
                        id,
                        request.getIssueTypeIds(),
                        tenantId,
                        userId
                )
        );

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<DeleteIssueTypeSchemeResult>> deleteIssueTypeScheme(@PathVariable Long id) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();
        DeleteIssueTypeSchemeResult response = deleteIssueTypeSchemeCommandHandler.handle(
                new DeleteIssueTypeSchemeCommand(id, tenantId, userId)
        );
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
