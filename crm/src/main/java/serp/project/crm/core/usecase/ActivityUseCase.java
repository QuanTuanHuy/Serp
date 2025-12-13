/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.CreateActivityRequest;
import serp.project.crm.core.domain.dto.request.UpdateActivityRequest;
import serp.project.crm.core.domain.dto.response.ActivityResponse;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.exception.AppException;
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

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createActivity(CreateActivityRequest request, Long userId, Long tenantId) {
        try {
            ActivityEntity activityEntity = activityDtoMapper.toEntity(request);
            ActivityEntity createdActivity = activityService.createActivity(activityEntity, userId, tenantId);
            ActivityResponse response = activityDtoMapper.toResponse(createdActivity);

            log.info("[ActivityUseCase] Activity created successfully with ID: {}", createdActivity.getId());
            return responseUtils.success(response, "Activity created successfully");

        } catch (AppException e) {
            log.error("[ActivityUseCase] Error creating activity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[ActivityUseCase] Unexpected error creating activity: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateActivity(Long id, UpdateActivityRequest request, Long tenantId) {
        try {
            ActivityEntity updates = activityDtoMapper.toEntity(request);
            ActivityEntity updatedActivity = activityService.updateActivity(id, updates, tenantId);
            ActivityResponse response = activityDtoMapper.toResponse(updatedActivity);

            log.info("[ActivityUseCase] Activity updated successfully: {}", id);
            return responseUtils.success(response, "Activity updated successfully");

        } catch (AppException e) {
            log.error("[ActivityUseCase] Error updating activity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[ActivityUseCase] Unexpected error updating activity: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> completeActivity(Long id, Long tenantId) {
        try {
            ActivityEntity activity = activityService.completeActivity(id, tenantId);
            ActivityResponse response = activityDtoMapper.toResponse(activity);

            log.info("[ActivityUseCase] Activity completed successfully: {}", id);
            return responseUtils.success(response, "Activity completed successfully");

        } catch (AppException e) {
            log.error("[ActivityUseCase] Error completing activity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[ActivityUseCase] Unexpected error completing activity: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> cancelActivity(Long id, Long tenantId) {
        try {
            ActivityEntity activity = activityService.cancelActivity(id, tenantId);
            ActivityResponse response = activityDtoMapper.toResponse(activity);

            log.info("[ActivityUseCase] Activity cancelled successfully: {}", id);
            return responseUtils.success(response, "Activity cancelled successfully");

        } catch (AppException e) {
            log.error("[ActivityUseCase] Error cancelling activity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[ActivityUseCase] Unexpected error cancelling activity: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getActivityById(Long id, Long tenantId) {
        try {
            ActivityEntity activity = activityService.getActivityById(id, tenantId)
                    .orElse(null);

            if (activity == null) {
                return responseUtils.notFound(ErrorMessage.ACTIVITY_NOT_FOUND);
            }

            ActivityResponse response = activityDtoMapper.toResponse(activity);
            return responseUtils.success(response);

        } catch (Exception e) {
            log.error("[ActivityUseCase] Error fetching activity: {}", e.getMessage());
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

    @Transactional(readOnly = true)
    public GeneralResponse<?> getActivitiesByLead(Long leadId, Long tenantId, PageRequest pageRequest) {
        try {
            var result = activityService.getActivitiesByLead(leadId, tenantId, pageRequest);
            List<ActivityResponse> activityResponses = result.getFirst().stream()
                    .map(activityDtoMapper::toResponse)
                    .toList();
            PageResponse<ActivityResponse> pageResponse = PageResponse.of(
                    activityResponses, pageRequest, result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("[ActivityUseCase] Error fetching activities by lead {}: {}", leadId, e.getMessage());
            return responseUtils.internalServerError("Failed to fetch activities by lead");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getActivitiesByOpportunity(Long opportunityId, Long tenantId, PageRequest pageRequest) {
        try {
            var result = activityService.getActivitiesByOpportunity(opportunityId, tenantId, pageRequest);
            List<ActivityResponse> activityResponses = result.getFirst().stream()
                    .map(activityDtoMapper::toResponse)
                    .toList();
            PageResponse<ActivityResponse> pageResponse = PageResponse.of(
                    activityResponses, pageRequest, result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("[ActivityUseCase] Error fetching activities by opportunity {}: {}", opportunityId,
                    e.getMessage());
            return responseUtils.internalServerError("Failed to fetch activities by opportunity");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getActivitiesByCustomer(Long customerId, Long tenantId, PageRequest pageRequest) {
        try {
            var result = activityService.getActivitiesByCustomer(customerId, tenantId, pageRequest);
            List<ActivityResponse> activityResponses = result.getFirst().stream()
                    .map(activityDtoMapper::toResponse)
                    .toList();
            PageResponse<ActivityResponse> pageResponse = PageResponse.of(
                    activityResponses, pageRequest, result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("[ActivityUseCase] Error fetching activities by customer {}: {}", customerId, e.getMessage());
            return responseUtils.internalServerError("Failed to fetch activities by customer");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> deleteActivity(Long id, Long tenantId) {
        try {
            activityService.deleteActivity(id, tenantId);

            log.info("Activity deleted successfully: {}", id);
            return responseUtils.status("Activity deleted successfully");

        } catch (AppException e) {
            log.error("[ActivityUseCase] Validation error deleting activity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[ActivityUseCase] Unexpected error deleting activity: {}", e.getMessage());
            throw e;
        }
    }

}
