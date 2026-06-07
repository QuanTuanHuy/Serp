/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.resourcecalendar;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.resourcecalendar.ResourceCalendarDeleteResult;
import serp.project.pmcore.application.resourcecalendar.command.exception.CreateResourceCalendarExceptionCommand;
import serp.project.pmcore.application.resourcecalendar.command.exception.CreateResourceCalendarExceptionCommandHandler;
import serp.project.pmcore.application.resourcecalendar.command.exception.DeleteResourceCalendarExceptionCommand;
import serp.project.pmcore.application.resourcecalendar.command.exception.DeleteResourceCalendarExceptionCommandHandler;
import serp.project.pmcore.application.resourcecalendar.command.exception.UpdateResourceCalendarExceptionCommand;
import serp.project.pmcore.application.resourcecalendar.command.exception.UpdateResourceCalendarExceptionCommandHandler;
import serp.project.pmcore.application.resourcecalendar.settings.ResourceCalendarSettingsOverviewView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.resourcecalendar.dto.request.CreateResourceCalendarExceptionRequest;
import serp.project.pmcore.ui.rest.resourcecalendar.dto.request.UpdateResourceCalendarExceptionRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@RestController
@RequestMapping(PathConstants.RESOURCE_CALENDAR_EXCEPTIONS)
@RequiredArgsConstructor
public class ResourceCalendarExceptionController {
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateResourceCalendarExceptionCommandHandler createResourceCalendarExceptionCommandHandler;
    private final UpdateResourceCalendarExceptionCommandHandler updateResourceCalendarExceptionCommandHandler;
    private final DeleteResourceCalendarExceptionCommandHandler deleteResourceCalendarExceptionCommandHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<ResourceCalendarSettingsOverviewView.ExceptionView>> createException(
            @Valid @RequestBody CreateResourceCalendarExceptionRequest request) {
        ResourceCalendarSettingsOverviewView.ExceptionView response = createResourceCalendarExceptionCommandHandler.handle(
                new CreateResourceCalendarExceptionCommand(
                        requireCurrentTenantId(),
                        request.getUserId(),
                        request.getExceptionType(),
                        toLocalDateTime(request.getStartAt()),
                        toLocalDateTime(request.getEndAt()),
                        request.getCapacityFactor(),
                        request.getReason()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<ResourceCalendarSettingsOverviewView.ExceptionView>> updateException(
            @PathVariable Long id,
            @Valid @RequestBody UpdateResourceCalendarExceptionRequest request) {
        ResourceCalendarSettingsOverviewView.ExceptionView response = updateResourceCalendarExceptionCommandHandler.handle(
                new UpdateResourceCalendarExceptionCommand(
                        requireCurrentTenantId(),
                        id,
                        request.getUserId(),
                        request.getExceptionType(),
                        toLocalDateTime(request.getStartAt()),
                        toLocalDateTime(request.getEndAt()),
                        request.getCapacityFactor(),
                        request.getReason()
                )
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<ResourceCalendarDeleteResult>> deleteException(@PathVariable Long id) {
        ResourceCalendarDeleteResult response = deleteResourceCalendarExceptionCommandHandler.handle(
                new DeleteResourceCalendarExceptionCommand(requireCurrentTenantId(), id)
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    private Long requireCurrentTenantId() {
        return authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));
    }

    private LocalDateTime toLocalDateTime(Long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }
}
