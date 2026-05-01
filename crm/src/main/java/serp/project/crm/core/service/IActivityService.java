/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.ActivityFilterRequest;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.enums.ActivityOutcome;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;

public interface IActivityService {

    ActivityEntity createActivity(ActivityEntity activity, Long userId, Long tenantId);
    ActivityEntity updateActivity(Long id, ActivityEntity updates, Long userId, Long tenantId);

    Optional<ActivityEntity> getActivityById(Long id, Long tenantId);
    Pair<List<ActivityEntity>, Long> getAllActivities(Long tenantId, PageRequest pageRequest);
    Pair<List<ActivityEntity>, Long> getActivitiesByType(ActivityType type, Long tenantId, PageRequest pageRequest);
    Pair<List<ActivityEntity>, Long> getActivitiesByStatus(ActivityStatus status, Long tenantId,
            PageRequest pageRequest);
    Pair<List<ActivityEntity>, Long> getActivitiesByAssignee(Long userId, Long tenantId, PageRequest pageRequest);

    Pair<List<ActivityEntity>, Long> getActivitiesByLead(Long leadId, Long tenantId, PageRequest pageRequest);
    Pair<List<ActivityEntity>, Long> getActivitiesByAccount(Long accountId, Long tenantId,
            PageRequest pageRequest);
    Pair<List<ActivityEntity>, Long> getActivitiesByOpportunity(Long opportunityId, Long tenantId,
            PageRequest pageRequest);
    Pair<List<ActivityEntity>, Long> getActivitiesByContact(Long contactId, Long tenantId, PageRequest pageRequest);

    List<ActivityEntity> getOverdueActivities(Long tenantId);
    List<ActivityEntity> getUpcomingActivities(LocalDateTime startDate, LocalDateTime endDate, Long tenantId);

    ActivityEntity completeActivity(Long id, ActivityOutcome outcome, String notes, Long userId, Long tenantId);
    ActivityEntity cancelActivity(Long id, Long userId, Long tenantId);
    ActivityEntity rescheduleActivity(Long id, Long dueDate, Long reminderDate, Long userId, Long tenantId);

    void deleteActivity(Long id, Long tenantId);

    void validateRelations(ActivityEntity activity, Long tenantId);

    Pair<List<ActivityEntity>, Long> filterActivities(ActivityFilterRequest filterRequest, Long tenantId);

    Map<String, Long> getActivityStats(Long tenantId);

    Map<String, Integer> bulkCompleteActivities(Set<Long> activityIds, Long userId, Long tenantId);

    Map<String, Integer> bulkCancelActivities(Set<Long> activityIds, Long userId, Long tenantId);

    Map<String, Integer> bulkDeleteActivities(Set<Long> activityIds, Long tenantId);

    Map<String, Integer> bulkReassignActivities(Set<Long> activityIds, Long newAssigneeId, Long userId, Long tenantId);
}
