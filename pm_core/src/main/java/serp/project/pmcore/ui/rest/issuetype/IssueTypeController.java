/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.issuetype;

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
import serp.project.pmcore.application.issuetype.IssueTypeView;
import serp.project.pmcore.application.issuetype.command.create.CreateIssueTypeCommand;
import serp.project.pmcore.application.issuetype.command.create.CreateIssueTypeCommandHandler;
import serp.project.pmcore.application.issuetype.command.delete.DeleteIssueTypeCommand;
import serp.project.pmcore.application.issuetype.command.delete.DeleteIssueTypeCommandHandler;
import serp.project.pmcore.application.issuetype.command.delete.DeleteIssueTypeResult;
import serp.project.pmcore.application.issuetype.command.update.UpdateIssueTypeCommand;
import serp.project.pmcore.application.issuetype.command.update.UpdateIssueTypeCommandHandler;
import serp.project.pmcore.application.issuetype.query.get.GetIssueTypeByIdQuery;
import serp.project.pmcore.application.issuetype.query.get.GetIssueTypeByIdQueryHandler;
import serp.project.pmcore.application.issuetype.query.list.ListIssueTypesQuery;
import serp.project.pmcore.application.issuetype.query.list.ListIssueTypesQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.issuetype.dto.request.CreateIssueTypeRequest;
import serp.project.pmcore.ui.rest.issuetype.dto.request.UpdateIssueTypeRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.ISSUE_TYPES)
@RequiredArgsConstructor
public class IssueTypeController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateIssueTypeCommandHandler createIssueTypeCommandHandler;
    private final UpdateIssueTypeCommandHandler updateIssueTypeCommandHandler;
    private final DeleteIssueTypeCommandHandler deleteIssueTypeCommandHandler;
    private final GetIssueTypeByIdQueryHandler getIssueTypeByIdQueryHandler;
    private final ListIssueTypesQueryHandler listIssueTypesQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<IssueTypeView>> createIssueType(
            @Valid @RequestBody CreateIssueTypeRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        IssueTypeView response = createIssueTypeCommandHandler.handle(new CreateIssueTypeCommand(
                request.getTypeKey(),
                request.getName(),
                request.getDescription(),
                request.getIconUrl(),
                request.getHierarchyLevel(),
                tenantId,
                userId
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<IssueTypeView>> updateIssueType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueTypeRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        IssueTypeView response = updateIssueTypeCommandHandler.handle(new UpdateIssueTypeCommand(
                id,
                request.toData(),
                tenantId,
                userId
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<IssueTypeView>> getIssueTypeById(@PathVariable Long id) {
        Long tenantId = requireCurrentTenantId();
        IssueTypeView response = getIssueTypeByIdQueryHandler.handle(new GetIssueTypeByIdQuery(id, tenantId));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<IssueTypeView>>> listIssueTypes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer hierarchyLevel,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Long tenantId = requireCurrentTenantId();
        PageView<IssueTypeView> response = listIssueTypesQueryHandler.handle(new ListIssueTypesQuery(
                tenantId,
                search,
                hierarchyLevel,
                isSystem,
                projectId,
                page,
                pageSize,
                sortBy,
                sortDirection
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<DeleteIssueTypeResult>> deleteIssueType(@PathVariable Long id) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();
        DeleteIssueTypeResult response = deleteIssueTypeCommandHandler.handle(new DeleteIssueTypeCommand(
                id,
                tenantId,
                userId
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
