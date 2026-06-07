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
import serp.project.pmcore.application.resourcecalendar.command.assignment.ReplaceResourceCalendarAssignmentsCommand;
import serp.project.pmcore.application.resourcecalendar.command.assignment.ReplaceResourceCalendarAssignmentsCommandHandler;
import serp.project.pmcore.application.resourcecalendar.command.block.ReplaceResourceCalendarBlocksCommand;
import serp.project.pmcore.application.resourcecalendar.command.block.ReplaceResourceCalendarBlocksCommandHandler;
import serp.project.pmcore.application.resourcecalendar.command.profile.CreateResourceCalendarProfileCommand;
import serp.project.pmcore.application.resourcecalendar.command.profile.CreateResourceCalendarProfileCommandHandler;
import serp.project.pmcore.application.resourcecalendar.command.profile.DeleteResourceCalendarProfileCommand;
import serp.project.pmcore.application.resourcecalendar.command.profile.DeleteResourceCalendarProfileCommandHandler;
import serp.project.pmcore.application.resourcecalendar.command.profile.UpdateResourceCalendarProfileCommand;
import serp.project.pmcore.application.resourcecalendar.command.profile.UpdateResourceCalendarProfileCommandHandler;
import serp.project.pmcore.application.resourcecalendar.settings.ResourceCalendarSettingsOverviewView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.resourcecalendar.dto.request.CreateResourceCalendarProfileRequest;
import serp.project.pmcore.ui.rest.resourcecalendar.dto.request.ReplaceResourceCalendarAssignmentsRequest;
import serp.project.pmcore.ui.rest.resourcecalendar.dto.request.ReplaceResourceCalendarBlocksRequest;
import serp.project.pmcore.ui.rest.resourcecalendar.dto.request.UpdateResourceCalendarProfileRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

import java.util.List;

@RestController
@RequestMapping(PathConstants.RESOURCE_CALENDAR_PROFILES)
@RequiredArgsConstructor
public class ResourceCalendarProfileController {
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateResourceCalendarProfileCommandHandler createResourceCalendarProfileCommandHandler;
    private final UpdateResourceCalendarProfileCommandHandler updateResourceCalendarProfileCommandHandler;
    private final DeleteResourceCalendarProfileCommandHandler deleteResourceCalendarProfileCommandHandler;
    private final ReplaceResourceCalendarBlocksCommandHandler replaceResourceCalendarBlocksCommandHandler;
    private final ReplaceResourceCalendarAssignmentsCommandHandler replaceResourceCalendarAssignmentsCommandHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<ResourceCalendarSettingsOverviewView.ProfileView>> createProfile(
            @Valid @RequestBody CreateResourceCalendarProfileRequest request) {
        ResourceCalendarSettingsOverviewView.ProfileView response = createResourceCalendarProfileCommandHandler.handle(
                new CreateResourceCalendarProfileCommand(
                        requireCurrentTenantId(),
                        requireCurrentUserId(),
                        request.getName(),
                        request.getDescription(),
                        request.getTimezone(),
                        request.getIsDefault()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<ResourceCalendarSettingsOverviewView.ProfileView>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateResourceCalendarProfileRequest request) {
        ResourceCalendarSettingsOverviewView.ProfileView response = updateResourceCalendarProfileCommandHandler.handle(
                new UpdateResourceCalendarProfileCommand(
                        requireCurrentTenantId(),
                        requireCurrentUserId(),
                        id,
                        request.getName(),
                        request.getDescription(),
                        request.getTimezone(),
                        request.getIsDefault()
                )
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<ResourceCalendarDeleteResult>> deleteProfile(@PathVariable Long id) {
        ResourceCalendarDeleteResult response = deleteResourceCalendarProfileCommandHandler.handle(
                new DeleteResourceCalendarProfileCommand(requireCurrentTenantId(), id)
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PutMapping("/{id}/blocks")
    public ResponseEntity<GeneralResponse<List<ResourceCalendarSettingsOverviewView.BlockView>>> replaceBlocks(
            @PathVariable Long id,
            @Valid @RequestBody ReplaceResourceCalendarBlocksRequest request) {
        List<ResourceCalendarSettingsOverviewView.BlockView> response = replaceResourceCalendarBlocksCommandHandler.handle(
                new ReplaceResourceCalendarBlocksCommand(
                        requireCurrentTenantId(),
                        id,
                        request.getBlocks().stream()
                                .map(block -> new ReplaceResourceCalendarBlocksCommand.Block(
                                        block.getDayOfWeek(),
                                        block.getStartTime(),
                                        block.getEndTime(),
                                        block.getCapacityFactor()
                                ))
                                .toList()
                )
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PutMapping("/{id}/assignments")
    public ResponseEntity<GeneralResponse<List<ResourceCalendarSettingsOverviewView.AssignmentView>>> replaceAssignments(
            @PathVariable Long id,
            @Valid @RequestBody ReplaceResourceCalendarAssignmentsRequest request) {
        List<ResourceCalendarSettingsOverviewView.AssignmentView> response = replaceResourceCalendarAssignmentsCommandHandler.handle(
                new ReplaceResourceCalendarAssignmentsCommand(
                        requireCurrentTenantId(),
                        id,
                        request.getAssignments().stream()
                                .map(assignment -> new ReplaceResourceCalendarAssignmentsCommand.Assignment(
                                        assignment.getUserId(),
                                        assignment.getEffectiveFrom(),
                                        assignment.getEffectiveTo()
                                ))
                                .toList()
                )
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
