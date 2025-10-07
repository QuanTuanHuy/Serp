/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.CreateActivityRequest;
import serp.project.crm.core.domain.dto.request.UpdateActivityRequest;
import serp.project.crm.core.domain.dto.response.ActivityResponse;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.mapper.ActivityDtoMapper;
import serp.project.crm.core.service.IActivityService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityUseCase {

    private final IActivityService activityService;
    private final ActivityDtoMapper activityDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional
    public GeneralResponse<?> createActivity(CreateActivityRequest request, Long tenantId) {
        try {
            ActivityEntity activityEntity = activityDtoMapper.toEntity(request);
            ActivityEntity createdActivity = activityService.createActivity(activityEntity, tenantId);
            ActivityResponse response = activityDtoMapper.toResponse(createdActivity);

            log.info("Activity created successfully with ID: {}", createdActivity.getId());
            return responseUtils.success(response, "Activity created successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error creating activity: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating activity: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to create activity");
        }
    }

    @Transactional
    public GeneralResponse<?> updateActivity(Long id, UpdateActivityRequest request, Long tenantId) {
        try {
            ActivityEntity updates = activityDtoMapper.toEntity(request);
            ActivityEntity updatedActivity = activityService.updateActivity(id, updates, tenantId);
            ActivityResponse response = activityDtoMapper.toResponse(updatedActivity);

            log.info("Activity updated successfully: {}", id);
            return responseUtils.success(response, "Activity updated successfully");

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error updating activity: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating activity: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to update activity");
        }
    }

    @Transactional
    public GeneralResponse<?> completeActivity(Long id, Long tenantId) {
        try {
            ActivityEntity activity = activityService.completeActivity(id, tenantId);
            ActivityResponse response = activityDtoMapper.toResponse(activity);

            log.info("Activity completed successfully: {}", id);
            return responseUtils.success(response, "Activity completed successfully");

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error completing activity: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error completing activity: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to complete activity");
        }
    }

    @Transactional
    public GeneralResponse<?> cancelActivity(Long id, Long tenantId) {
        try {
            ActivityEntity activity = activityService.cancelActivity(id, tenantId);
            ActivityResponse response = activityDtoMapper.toResponse(activity);

            log.info("Activity cancelled successfully: {}", id);
            return responseUtils.success(response, "Activity cancelled successfully");

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error cancelling activity: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error cancelling activity: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to cancel activity");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getActivityById(Long id, Long tenantId) {
        try {
            ActivityEntity activity = activityService.getActivityById(id, tenantId)
                    .orElse(null);

            if (activity == null) {
                return responseUtils.notFound("Activity not found");
            }

            ActivityResponse response = activityDtoMapper.toResponse(activity);
            return responseUtils.success(response);

        } catch (Exception e) {
            log.error("Error fetching activity: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch activity");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAllActivities(Long tenantId, PageRequest pageRequest) {
        try {
            var result = activityService.getAllActivities(tenantId, pageRequest);

            List<ActivityResponse> activityResponses = result.getFirst().stream()
                    .map(activityDtoMapper::toResponse)
                    .toList();

            PageResponse<ActivityResponse> pageResponse = PageResponse.of(
                    activityResponses, pageRequest, result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("Error fetching activities: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch activities");
        }
    }

    @Transactional
    public GeneralResponse<?> deleteActivity(Long id, Long tenantId) {
        try {
            activityService.deleteActivity(id, tenantId);

            log.info("Activity deleted successfully: {}", id);
            return responseUtils.status("Activity deleted successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error deleting activity: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error deleting activity: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to delete activity");
        }
    }
}
