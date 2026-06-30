/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.ActivityFilterRequest;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.TaskPriority;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IActivityPort {

    ActivityEntity save(ActivityEntity activityEntity);

    Optional<ActivityEntity> findById(Long id, Long tenantId);

    Pair<List<ActivityEntity>, Long> findAll(Long tenantId, PageRequest pageRequest);
    Pair<List<ActivityEntity>, Long> findByLeadId(Long leadId, Long tenantId, PageRequest pageRequest);
    Pair<List<ActivityEntity>, Long> findByAccountId(Long accountId, Long tenantId, PageRequest pageRequest);
    Pair<List<ActivityEntity>, Long> findByOpportunityId(Long opportunityId, Long tenantId, PageRequest pageRequest);
    List<ActivityEntity> findByOpportunityIds(List<Long> opportunityIds, Long tenantId);
    Pair<List<ActivityEntity>, Long> findByContactId(Long contactId, Long tenantId, PageRequest pageRequest);
    Pair<List<ActivityEntity>, Long> findByAssignedTo(Long assignedTo, Long tenantId, PageRequest pageRequest);

    List<ActivityEntity> findAllByAssignedTo(Long assignedTo, Long tenantId);
    Pair<List<ActivityEntity>, Long> findByActivityType(ActivityType activityType, Long tenantId,
            PageRequest pageRequest);
    Pair<List<ActivityEntity>, Long> findByStatus(ActivityStatus status, Long tenantId, PageRequest pageRequest);
    Pair<List<ActivityEntity>, Long> findByPriority(TaskPriority priority, Long tenantId, PageRequest pageRequest);

    List<ActivityEntity> findOverdueActivities(Long tenantId);
    List<ActivityEntity> findUpcomingActivities(Long tenantId);
    List<ActivityEntity> findUpcomingActivities(Long tenantId, Long startTime, Long endTime);
    List<ActivityEntity> findByActivityDateBetween(Long startDate, Long endDate, Long tenantId);
    List<ActivityEntity> findAllByLeadId(Long leadId, Long tenantId);

    Long countByStatus(ActivityStatus status, Long tenantId);

    void deleteById(Long id, Long tenantId);

    Pair<List<ActivityEntity>, Long> filter(ActivityFilterRequest filterRequest, Long tenantId);

    List<ActivityEntity> findByIds(Set<Long> ids, Long tenantId);

    long countByTenantId(Long tenantId);

    long countByActivityType(ActivityType type, Long tenantId);

    long countByPriority(TaskPriority priority, Long tenantId);

}
