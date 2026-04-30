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
import serp.project.crm.core.domain.dto.request.CompleteActivityRequest;
import serp.project.crm.core.domain.dto.request.CreateActivityRequest;
import serp.project.crm.core.domain.dto.request.RescheduleActivityRequest;
import serp.project.crm.core.domain.dto.request.UpdateActivityRequest;
import serp.project.crm.core.domain.dto.response.ActivityResponse;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.mapper.ActivityDtoMapper;
import serp.project.crm.core.service.IActivityService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.time.LocalDateTime;
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
        ActivityEntity activityEntity = activityDtoMapper.toEntity(request);
        ActivityEntity createdActivity = activityService.createActivity(activityEntity, userId, tenantId);
        ActivityResponse response = activityDtoMapper.toResponse(createdActivity);

        log.info("[ActivityUseCase] Activity created successfully with ID: {}", createdActivity.getId());
        return responseUtils.success(response, "Activity created successfully");
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateActivity(Long id, UpdateActivityRequest request, Long userId, Long tenantId) {
        ActivityEntity updates = activityDtoMapper.toEntity(request);
        ActivityEntity updatedActivity = activityService.updateActivity(id, updates, userId, tenantId);
        ActivityResponse response = activityDtoMapper.toResponse(updatedActivity);

        log.info("[ActivityUseCase] Activity updated successfully: {}", id);
        return responseUtils.success(response, "Activity updated successfully");
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> completeActivity(Long id, CompleteActivityRequest request, Long userId, Long tenantId) {
        CompleteActivityRequest safeRequest = request != null ? request : CompleteActivityRequest.builder().build();
        ActivityEntity activity = activityService.completeActivity(id, safeRequest.getOutcome(), safeRequest.getNotes(),
                userId, tenantId);
        ActivityResponse response = activityDtoMapper.toResponse(activity);

        log.info("[ActivityUseCase] Activity completed successfully: {}", id);
        return responseUtils.success(response, "Activity completed successfully");
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> cancelActivity(Long id, Long userId, Long tenantId) {
        ActivityEntity activity = activityService.cancelActivity(id, userId, tenantId);
        ActivityResponse response = activityDtoMapper.toResponse(activity);

        log.info("[ActivityUseCase] Activity cancelled successfully: {}", id);
        return responseUtils.success(response, "Activity cancelled successfully");
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> rescheduleActivity(Long id, RescheduleActivityRequest request, Long userId, Long tenantId) {
        ActivityEntity activity = activityService.rescheduleActivity(id, request.getDueDate(), request.getReminderDate(),
                userId, tenantId);
        ActivityResponse response = activityDtoMapper.toResponse(activity);

        log.info("[ActivityUseCase] Activity rescheduled successfully: {}", id);
        return responseUtils.success(response, "Activity rescheduled successfully");
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getActivityById(Long id, Long tenantId) {
        ActivityEntity activity = activityService.getActivityById(id, tenantId)
                .orElse(null);

        if (activity == null) {
            return responseUtils.notFound(ErrorMessage.ACTIVITY_NOT_FOUND);
        }

        ActivityResponse response = activityDtoMapper.toResponse(activity);
        return responseUtils.success(response);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAllActivities(Long tenantId, PageRequest pageRequest) {
        var result = activityService.getAllActivities(tenantId, pageRequest);
        return responseUtils.success(toPageResponse(result.getFirst(), pageRequest, result.getSecond()));
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getActivitiesByType(ActivityType type, Long tenantId, PageRequest pageRequest) {
        var result = activityService.getActivitiesByType(type, tenantId, pageRequest);
        return responseUtils.success(toPageResponse(result.getFirst(), pageRequest, result.getSecond()));
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getActivitiesByStatus(ActivityStatus status, Long tenantId, PageRequest pageRequest) {
        var result = activityService.getActivitiesByStatus(status, tenantId, pageRequest);
        return responseUtils.success(toPageResponse(result.getFirst(), pageRequest, result.getSecond()));
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getActivitiesByAssignee(Long assigneeId, Long tenantId, PageRequest pageRequest) {
        var result = activityService.getActivitiesByAssignee(assigneeId, tenantId, pageRequest);
        return responseUtils.success(toPageResponse(result.getFirst(), pageRequest, result.getSecond()));
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getOverdueActivities(Long tenantId) {
        List<ActivityResponse> activityResponses = activityService.getOverdueActivities(tenantId).stream()
                .map(activityDtoMapper::toResponse)
                .toList();
        return responseUtils.success(activityResponses);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getUpcomingActivities(LocalDateTime startDate, LocalDateTime endDate, Long tenantId) {
        List<ActivityResponse> activityResponses = activityService.getUpcomingActivities(startDate, endDate, tenantId)
                .stream()
                .map(activityDtoMapper::toResponse)
                .toList();
        return responseUtils.success(activityResponses);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getActivitiesByLead(Long leadId, Long tenantId, PageRequest pageRequest) {
        var result = activityService.getActivitiesByLead(leadId, tenantId, pageRequest);
        return responseUtils.success(toPageResponse(result.getFirst(), pageRequest, result.getSecond()));
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getActivitiesByOpportunity(Long opportunityId, Long tenantId, PageRequest pageRequest) {
        var result = activityService.getActivitiesByOpportunity(opportunityId, tenantId, pageRequest);
        return responseUtils.success(toPageResponse(result.getFirst(), pageRequest, result.getSecond()));
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getActivitiesByAccount(Long accountId, Long tenantId, PageRequest pageRequest) {
        var result = activityService.getActivitiesByAccount(accountId, tenantId, pageRequest);
        return responseUtils.success(toPageResponse(result.getFirst(), pageRequest, result.getSecond()));
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> deleteActivity(Long id, Long tenantId) {
        activityService.deleteActivity(id, tenantId);

        log.info("[ActivityUseCase] Activity deleted successfully: {}", id);
        return responseUtils.status("Activity deleted successfully");
    }

    private PageResponse<ActivityResponse> toPageResponse(List<ActivityEntity> activities, PageRequest pageRequest,
            Long total) {
        List<ActivityResponse> activityResponses = activities.stream()
                .map(activityDtoMapper::toResponse)
                .toList();

        return PageResponse.of(activityResponses, pageRequest, total);
    }
}
