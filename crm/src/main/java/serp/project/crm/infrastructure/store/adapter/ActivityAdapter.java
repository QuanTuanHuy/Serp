/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.TaskPriority;
import serp.project.crm.core.port.store.IActivityPort;
import serp.project.crm.infrastructure.store.mapper.ActivityMapper;
import serp.project.crm.infrastructure.store.repository.ActivityRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ActivityAdapter implements IActivityPort {

    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;

    @Override
    public ActivityEntity save(ActivityEntity activityEntity) {
        var model = activityMapper.toModel(activityEntity);
        var savedModel = activityRepository.save(model);
        return activityMapper.toEntity(savedModel);
    }

    @Override
    public Optional<ActivityEntity> findById(Long id, Long tenantId) {
        return activityRepository.findByIdAndTenantId(id, tenantId)
                .map(activityMapper::toEntity);
    }

    @Override
    public Pair<List<ActivityEntity>, Long> findAll(Long tenantId, PageRequest pageRequest) {
        var pageable = activityMapper.toPageable(pageRequest);
        var page = activityRepository.findByTenantId(tenantId, pageable)
                .map(activityMapper::toEntity);
        return activityMapper.pageToPair(page);
    }

    @Override
    public Pair<List<ActivityEntity>, Long> findByLeadId(Long leadId, Long tenantId, PageRequest pageRequest) {
        List<ActivityEntity> allActivities = activityRepository.findByTenantIdAndLeadId(tenantId, leadId)
                .stream()
                .map(activityMapper::toEntity)
                .collect(Collectors.toList());

        int start = pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getSize(), allActivities.size());
        List<ActivityEntity> pageContent = allActivities.subList(start, end);

        return Pair.of(pageContent, (long) allActivities.size());
    }

    @Override
    public List<ActivityEntity> findByLeadId(Long leadId, Long tenantId) {
        return activityRepository.findByTenantIdAndLeadId(tenantId, leadId)
                .stream()
                .map(activityMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Pair<List<ActivityEntity>, Long> findByCustomerId(Long customerId, Long tenantId, PageRequest pageRequest) {
        List<ActivityEntity> allActivities = activityRepository.findByTenantIdAndCustomerId(tenantId, customerId)
                .stream()
                .map(activityMapper::toEntity)
                .collect(Collectors.toList());

        int start = pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getSize(), allActivities.size());
        List<ActivityEntity> pageContent = allActivities.subList(start, end);

        return Pair.of(pageContent, (long) allActivities.size());
    }

    @Override
    public Pair<List<ActivityEntity>, Long> findByOpportunityId(Long opportunityId, Long tenantId,
            PageRequest pageRequest) {
        List<ActivityEntity> allActivities = activityRepository.findByTenantIdAndOpportunityId(tenantId, opportunityId)
                .stream()
                .map(activityMapper::toEntity)
                .collect(Collectors.toList());

        int start = pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getSize(), allActivities.size());
        List<ActivityEntity> pageContent = allActivities.subList(start, end);

        return Pair.of(pageContent, (long) allActivities.size());
    }

    @Override
    public Pair<List<ActivityEntity>, Long> findByContactId(Long contactId, Long tenantId, PageRequest pageRequest) {
        List<ActivityEntity> allActivities = activityRepository.findByTenantIdAndContactId(tenantId, contactId)
                .stream()
                .map(activityMapper::toEntity)
                .collect(Collectors.toList());

        int start = pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getSize(), allActivities.size());
        List<ActivityEntity> pageContent = allActivities.subList(start, end);

        return Pair.of(pageContent, (long) allActivities.size());
    }

    @Override
    public Pair<List<ActivityEntity>, Long> findByAssignedTo(Long assignedTo, Long tenantId, PageRequest pageRequest) {
        var pageable = activityMapper.toPageable(pageRequest);
        var page = activityRepository.findByTenantIdAndAssignedTo(tenantId, assignedTo, pageable)
                .map(activityMapper::toEntity);
        return activityMapper.pageToPair(page);
    }

    @Override
    public Pair<List<ActivityEntity>, Long> findByActivityType(ActivityType activityType, Long tenantId,
            PageRequest pageRequest) {
        var pageable = activityMapper.toPageable(pageRequest);
        var page = activityRepository.findByTenantIdAndActivityType(tenantId, activityType.name(), pageable)
                .map(activityMapper::toEntity);
        return activityMapper.pageToPair(page);
    }

    @Override
    public Pair<List<ActivityEntity>, Long> findByStatus(ActivityStatus status, Long tenantId,
            PageRequest pageRequest) {
        var pageable = activityMapper.toPageable(pageRequest);
        var page = activityRepository.findByTenantIdAndStatus(tenantId, status.name(), pageable)
                .map(activityMapper::toEntity);
        return activityMapper.pageToPair(page);
    }

    @Override
    public Pair<List<ActivityEntity>, Long> findByPriority(TaskPriority priority, Long tenantId,
            PageRequest pageRequest) {
        // TODO: Add repository method for priority filtering
        return Pair.of(List.of(), 0L);
    }

    @Override
    public List<ActivityEntity> findOverdueActivities(Long tenantId) {
        Long currentTimestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        // Repository returns Page, get all with large pageable
        var pageable = org.springframework.data.domain.PageRequest.of(0, 1000);
        return activityRepository.findOverdueActivities(tenantId, currentTimestamp, pageable)
                .stream()
                .map(activityMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityEntity> findUpcomingActivities(Long tenantId) {
        Long currentTimestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long nextWeekTimestamp = LocalDateTime.now().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()
                .toEpochMilli();
        return activityRepository.findUpcomingActivities(tenantId, currentTimestamp, nextWeekTimestamp)
                .stream()
                .map(activityMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Long countByStatus(ActivityStatus status, Long tenantId) {
        return activityRepository.countByTenantIdAndStatus(tenantId, status.name());
    }

    @Override
    public void deleteById(Long id, Long tenantId) {
        activityRepository.findByIdAndTenantId(id, tenantId)
                .ifPresent(activityRepository::delete);
    }

    @Override
    public List<ActivityEntity> findByActivityDateBetween(Long startDate, Long endDate, Long tenantId) {
        // TODO: Add repository method for activity date range
        return List.of();
    }
}
