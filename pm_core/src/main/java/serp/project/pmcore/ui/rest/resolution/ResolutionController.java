/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.resolution;

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
import serp.project.pmcore.application.resolution.ResolutionView;
import serp.project.pmcore.application.resolution.command.create.CreateResolutionCommand;
import serp.project.pmcore.application.resolution.command.create.CreateResolutionCommandHandler;
import serp.project.pmcore.application.resolution.command.delete.DeleteResolutionCommand;
import serp.project.pmcore.application.resolution.command.delete.DeleteResolutionCommandHandler;
import serp.project.pmcore.application.resolution.command.delete.DeleteResolutionResult;
import serp.project.pmcore.application.resolution.command.update.UpdateResolutionCommand;
import serp.project.pmcore.application.resolution.command.update.UpdateResolutionCommandHandler;
import serp.project.pmcore.application.resolution.query.get.GetResolutionByIdQuery;
import serp.project.pmcore.application.resolution.query.get.GetResolutionByIdQueryHandler;
import serp.project.pmcore.application.resolution.query.list.ListResolutionsQuery;
import serp.project.pmcore.application.resolution.query.list.ListResolutionsQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.resolution.dto.request.CreateResolutionRequest;
import serp.project.pmcore.ui.rest.resolution.dto.request.UpdateResolutionRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.RESOLUTIONS)
@RequiredArgsConstructor
public class ResolutionController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateResolutionCommandHandler createResolutionCommandHandler;
    private final UpdateResolutionCommandHandler updateResolutionCommandHandler;
    private final DeleteResolutionCommandHandler deleteResolutionCommandHandler;
    private final GetResolutionByIdQueryHandler getResolutionByIdQueryHandler;
    private final ListResolutionsQueryHandler listResolutionsQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<ResolutionView>> createResolution(
            @Valid @RequestBody CreateResolutionRequest request) {
        ResolutionView response = createResolutionCommandHandler.handle(new CreateResolutionCommand(
                request.getName(),
                request.getDescription(),
                request.getSequence(),
                requireCurrentTenantId(),
                requireCurrentUserId()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<ResolutionView>> updateResolution(
            @PathVariable Long id,
            @Valid @RequestBody UpdateResolutionRequest request) {
        ResolutionView response = updateResolutionCommandHandler.handle(new UpdateResolutionCommand(
                id,
                request.toData(),
                requireCurrentTenantId(),
                requireCurrentUserId()
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<ResolutionView>> getResolutionById(@PathVariable Long id) {
        ResolutionView response = getResolutionByIdQueryHandler.handle(
                new GetResolutionByIdQuery(id, requireCurrentTenantId())
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<ResolutionView>>> listResolutions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageView<ResolutionView> response = listResolutionsQueryHandler.handle(new ListResolutionsQuery(
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
    public ResponseEntity<GeneralResponse<DeleteResolutionResult>> deleteResolution(@PathVariable Long id) {
        DeleteResolutionResult response = deleteResolutionCommandHandler.handle(new DeleteResolutionCommand(
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
