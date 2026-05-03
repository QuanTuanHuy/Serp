/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.issuelinktype;

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
import serp.project.pmcore.application.issuelinktype.IssueLinkTypeView;
import serp.project.pmcore.application.issuelinktype.command.create.CreateIssueLinkTypeCommand;
import serp.project.pmcore.application.issuelinktype.command.create.CreateIssueLinkTypeCommandHandler;
import serp.project.pmcore.application.issuelinktype.command.delete.DeleteIssueLinkTypeCommand;
import serp.project.pmcore.application.issuelinktype.command.delete.DeleteIssueLinkTypeCommandHandler;
import serp.project.pmcore.application.issuelinktype.command.delete.DeleteIssueLinkTypeResult;
import serp.project.pmcore.application.issuelinktype.command.update.UpdateIssueLinkTypeCommand;
import serp.project.pmcore.application.issuelinktype.command.update.UpdateIssueLinkTypeCommandHandler;
import serp.project.pmcore.application.issuelinktype.query.get.GetIssueLinkTypeByIdQuery;
import serp.project.pmcore.application.issuelinktype.query.get.GetIssueLinkTypeByIdQueryHandler;
import serp.project.pmcore.application.issuelinktype.query.list.ListIssueLinkTypesQuery;
import serp.project.pmcore.application.issuelinktype.query.list.ListIssueLinkTypesQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.issuelinktype.dto.request.CreateIssueLinkTypeRequest;
import serp.project.pmcore.ui.rest.issuelinktype.dto.request.UpdateIssueLinkTypeRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.ISSUE_LINK_TYPES)
@RequiredArgsConstructor
public class IssueLinkTypeController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateIssueLinkTypeCommandHandler createIssueLinkTypeCommandHandler;
    private final UpdateIssueLinkTypeCommandHandler updateIssueLinkTypeCommandHandler;
    private final DeleteIssueLinkTypeCommandHandler deleteIssueLinkTypeCommandHandler;
    private final GetIssueLinkTypeByIdQueryHandler getIssueLinkTypeByIdQueryHandler;
    private final ListIssueLinkTypesQueryHandler listIssueLinkTypesQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<IssueLinkTypeView>> createIssueLinkType(
            @Valid @RequestBody CreateIssueLinkTypeRequest request) {
        IssueLinkTypeView response = createIssueLinkTypeCommandHandler.handle(new CreateIssueLinkTypeCommand(
                request.getName(),
                request.getOutwardDescription(),
                request.getInwardDescription(),
                requireCurrentTenantId(),
                requireCurrentUserId()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<IssueLinkTypeView>> updateIssueLinkType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueLinkTypeRequest request) {
        IssueLinkTypeView response = updateIssueLinkTypeCommandHandler.handle(new UpdateIssueLinkTypeCommand(
                id,
                request.getName(),
                request.getOutwardDescription(),
                request.getInwardDescription(),
                requireCurrentTenantId(),
                requireCurrentUserId()
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<IssueLinkTypeView>> getIssueLinkTypeById(@PathVariable Long id) {
        IssueLinkTypeView response = getIssueLinkTypeByIdQueryHandler.handle(
                new GetIssueLinkTypeByIdQuery(id, requireCurrentTenantId())
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<IssueLinkTypeView>>> listIssueLinkTypes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageView<IssueLinkTypeView> response = listIssueLinkTypesQueryHandler.handle(new ListIssueLinkTypesQuery(
                requireCurrentTenantId(),
                search,
                isSystem,
                page,
                pageSize,
                sortBy,
                sortDirection
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<DeleteIssueLinkTypeResult>> deleteIssueLinkType(@PathVariable Long id) {
        DeleteIssueLinkTypeResult response = deleteIssueLinkTypeCommandHandler.handle(new DeleteIssueLinkTypeCommand(
                id,
                requireCurrentTenantId(),
                requireCurrentUserId()
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
