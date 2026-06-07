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
import serp.project.crm.core.domain.dto.request.ActivityFilterRequest;
import serp.project.crm.core.domain.dto.request.BulkActivityRequest;
import serp.project.crm.core.domain.dto.request.CompleteActivityRequest;
import serp.project.crm.core.domain.dto.request.CreateActivityRequest;
import serp.project.crm.core.domain.dto.request.RescheduleActivityRequest;
import serp.project.crm.core.domain.dto.request.UpdateActivityRequest;
import serp.project.crm.core.domain.dto.response.ActivityResponse;
import serp.project.crm.core.domain.dto.response.ActivityStatsResponse;
import serp.project.crm.core.domain.dto.response.BulkActivityResponse;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.TaskPriority;
import serp.project.crm.core.mapper.ActivityDtoMapper;
import serp.project.crm.core.port.store.IAccountPort;
import serp.project.crm.core.port.store.IContactPort;
import serp.project.crm.core.port.store.ILeadPort;
import serp.project.crm.core.port.store.IOpportunityPort;
import serp.project.crm.core.port.client.IUserProfileClient;
import serp.project.crm.core.service.IActivityService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityUseCase {

    private final IActivityService activityService;
    private final ActivityDtoMapper activityDtoMapper;
    private final ResponseUtils responseUtils;
    private final ILeadPort leadPort;
    private final IAccountPort accountPort;
    private final IOpportunityPort opportunityPort;
    private final IContactPort contactPort;
    private final IUserProfileClient userProfileClient;

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

    @Transactional(readOnly = true)
    public GeneralResponse<?> filterActivities(ActivityFilterRequest filter, Long tenantId) {
        var pageRequest = filter.toPageRequest();
        var result = activityService.filterActivities(filter, tenantId);
        return responseUtils.success(toPageResponse(result.getFirst(), pageRequest, result.getSecond()));
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getActivityStats(Long tenantId) {
        Map<String, Long> statsMap = activityService.getActivityStats(tenantId);
        Map<String, Long> byStatus = new HashMap<>();
        Map<String, Long> byType = new HashMap<>();
        Map<String, Long> byPriority = new HashMap<>();

        for (ActivityStatus status : ActivityStatus.values()) {
            byStatus.put(status.name(), statsMap.getOrDefault("status_" + status.name().toLowerCase(), 0L));
        }
        for (ActivityType type : ActivityType.values()) {
            byType.put(type.name(), statsMap.getOrDefault("type_" + type.name().toLowerCase(), 0L));
        }
        for (TaskPriority priority : TaskPriority.values()) {
            byPriority.put(priority.name(), statsMap.getOrDefault("priority_" + priority.name().toLowerCase(), 0L));
        }

        ActivityStatsResponse stats = ActivityStatsResponse.builder()
                .total(statsMap.getOrDefault("total", 0L))
                .overdue(statsMap.getOrDefault("overdue", 0L))
                .upcoming(statsMap.getOrDefault("upcoming", 0L))
                .byStatus(byStatus)
                .byType(byType)
                .byPriority(byPriority)
                .build();
        return responseUtils.success(stats);
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> bulkCompleteActivities(BulkActivityRequest request, Long userId, Long tenantId) {
        Map<String, Integer> resultMap = activityService.bulkCompleteActivities(request.getActivityIds(), userId, tenantId);
        BulkActivityResponse response = BulkActivityResponse.builder()
                .successCount(resultMap.getOrDefault("success", 0))
                .failedCount(resultMap.getOrDefault("failed", 0))
                .message("Bulk complete operation completed")
                .build();
        log.info("[ActivityUseCase] Bulk complete activities: success={}, failed={}",
                response.getSuccessCount(), response.getFailedCount());
        return responseUtils.success(response);
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> bulkCancelActivities(BulkActivityRequest request, Long userId, Long tenantId) {
        Map<String, Integer> resultMap = activityService.bulkCancelActivities(request.getActivityIds(), userId, tenantId);
        BulkActivityResponse response = BulkActivityResponse.builder()
                .successCount(resultMap.getOrDefault("success", 0))
                .failedCount(resultMap.getOrDefault("failed", 0))
                .message("Bulk cancel operation completed")
                .build();
        log.info("[ActivityUseCase] Bulk cancel activities: success={}, failed={}",
                response.getSuccessCount(), response.getFailedCount());
        return responseUtils.success(response);
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> bulkDeleteActivities(BulkActivityRequest request, Long tenantId) {
        Map<String, Integer> resultMap = activityService.bulkDeleteActivities(request.getActivityIds(), tenantId);
        BulkActivityResponse response = BulkActivityResponse.builder()
                .successCount(resultMap.getOrDefault("success", 0))
                .failedCount(resultMap.getOrDefault("failed", 0))
                .message("Bulk delete operation completed")
                .build();
        log.info("[ActivityUseCase] Bulk delete activities: success={}, failed={}",
                response.getSuccessCount(), response.getFailedCount());
        return responseUtils.success(response);
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> bulkReassignActivities(BulkActivityRequest request, Long userId, Long tenantId) {
        if (request.getAssigneeId() == null) {
            return responseUtils.badRequest("Assignee ID is required for reassignment");
        }
        Map<String, Integer> resultMap = activityService.bulkReassignActivities(
                request.getActivityIds(), request.getAssigneeId(), userId, tenantId);
        BulkActivityResponse response = BulkActivityResponse.builder()
                .successCount(resultMap.getOrDefault("success", 0))
                .failedCount(resultMap.getOrDefault("failed", 0))
                .message("Bulk reassign operation completed")
                .build();
        log.info("[ActivityUseCase] Bulk reassign activities: success={}, failed={}",
                response.getSuccessCount(), response.getFailedCount());
        return responseUtils.success(response);
    }

    private PageResponse<ActivityResponse> toPageResponse(List<ActivityEntity> activities, PageRequest pageRequest,
            Long total) {
        List<ActivityResponse> activityResponses = activities.stream()
                .map(activityDtoMapper::toResponse)
                .toList();

        enrichActivityResponsesWithRelatedNames(activityResponses, activities.isEmpty() ? null : activities.get(0).getTenantId());

        return PageResponse.of(activityResponses, pageRequest, total);
    }

    private void enrichActivityResponsesWithRelatedNames(List<ActivityResponse> responses, Long tenantId) {
        if (responses == null || responses.isEmpty() || tenantId == null) {
            return;
        }

        // Collect all IDs
        Set<Long> userIds = responses.stream()
                .map(ActivityResponse::getAssignedTo)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Set<Long> leadIds = responses.stream()
                .map(ActivityResponse::getLeadId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Set<Long> accountIds = responses.stream()
                .map(ActivityResponse::getAccountId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Set<Long> opportunityIds = responses.stream()
                .map(ActivityResponse::getOpportunityId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Set<Long> contactIds = responses.stream()
                .map(ActivityResponse::getContactId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // Batch fetch names
        Map<Long, String> userNames = new HashMap<>();
        if (!userIds.isEmpty()) {
            userProfileClient.getUserProfilesByIds(List.copyOf(userIds))
                    .forEach(profile -> userNames.put(profile.getId(), profile.getFullName()));
        }

        Map<Long, String> leadNames = new HashMap<>();
        if (!leadIds.isEmpty()) {
            leadPort.findByIds(List.copyOf(leadIds), tenantId)
                    .forEach(lead -> leadNames.put(lead.getId(), lead.getName()));
        }

        Map<Long, String> accountNames = new HashMap<>();
        if (!accountIds.isEmpty()) {
            accountPort.findByIds(List.copyOf(accountIds), tenantId)
                    .forEach(account -> accountNames.put(account.getId(), account.getName()));
        }

        Map<Long, String> opportunityNames = new HashMap<>();
        if (!opportunityIds.isEmpty()) {
            opportunityPort.findByIds(List.copyOf(opportunityIds), tenantId)
                    .forEach(opp -> opportunityNames.put(opp.getId(), opp.getName()));
        }

        Map<Long, String> contactNames = new HashMap<>();
        if (!contactIds.isEmpty()) {
            contactPort.findByIds(List.copyOf(contactIds), tenantId)
                    .forEach(contact -> contactNames.put(contact.getId(), contact.getName()));
        }

        // Enrich responses with names
        for (ActivityResponse response : responses) {
            if (response.getAssignedTo() != null) {
                response.setAssignedToName(userNames.get(response.getAssignedTo()));
            }
            if (response.getLeadId() != null) {
                response.setRelatedLeadName(leadNames.get(response.getLeadId()));
            }
            if (response.getAccountId() != null) {
                response.setRelatedCustomerName(accountNames.get(response.getAccountId()));
            }
            if (response.getOpportunityId() != null) {
                response.setRelatedOpportunityName(opportunityNames.get(response.getOpportunityId()));
            }
            if (response.getContactId() != null) {
                response.setRelatedContactName(contactNames.get(response.getContactId()));
            }
        }
    }
}
