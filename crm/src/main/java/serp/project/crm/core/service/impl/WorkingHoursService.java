/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.entity.WorkingHoursEntity;
import serp.project.crm.core.port.store.IWorkingHoursPort;
import serp.project.crm.core.service.IWorkingHoursService;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkingHoursService implements IWorkingHoursService {

    private final IWorkingHoursPort workingHoursPort;

    @Override
    @Transactional(readOnly = true)
    public List<WorkingHoursEntity> getByTeamMemberId(Long teamMemberId) {
        return workingHoursPort.findByTeamMemberId(teamMemberId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<WorkingHoursEntity>> getByTeamMemberIds(Collection<Long> teamMemberIds) {
        if (teamMemberIds == null || teamMemberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<WorkingHoursEntity>> result = new HashMap<>();
        List<WorkingHoursEntity> workingHoursEntities = workingHoursPort.findByTeamMemberIds(teamMemberIds);
        if (workingHoursEntities == null) {
            return result;
        }

        workingHoursEntities.stream()
                .filter(Objects::nonNull)
                .forEach(entity -> result
                        .computeIfAbsent(entity.getTeamMemberId(), ignored -> new ArrayList<>())
                        .add(entity));
        return result;
    }

    @Override
    @Transactional
    public List<WorkingHoursEntity> replaceByTeamMemberId(Long teamMemberId, List<WorkingHoursEntity> workingHoursEntities) {
        if (workingHoursEntities == null || workingHoursEntities.isEmpty()) {
            workingHoursPort.deleteByTeamMemberId(teamMemberId);
            return Collections.emptyList();
        }

        // Fetch existing working hours to perform upsert instead of delete-then-insert
        List<WorkingHoursEntity> existing = workingHoursPort.findByTeamMemberId(teamMemberId);
        Map<DayOfWeek, WorkingHoursEntity> existingMap = existing.stream()
                .collect(Collectors.toMap(WorkingHoursEntity::getDayOfWeek, Function.identity()));

        // Update existing or create new entities
        List<WorkingHoursEntity> entitiesToSave = workingHoursEntities.stream()
                .map(entity -> {
                    WorkingHoursEntity existingEntity = existingMap.get(entity.getDayOfWeek());
                    if (existingEntity != null) {
                        // Update existing entity
                        existingEntity.setWorkingDay(entity.getWorkingDay());
                        existingEntity.setStartMinute(entity.getStartMinute());
                        existingEntity.setEndMinute(entity.getEndMinute());
                        existingEntity.setUpdatedBy(entity.getUpdatedBy());
                        return existingEntity;
                    } else {
                        // Create new entity
                        entity.setTeamMemberId(teamMemberId);
                        return entity;
                    }
                })
                .toList();

        // Delete removed days (days in existing but not in new list)
        Set<DayOfWeek> newDays = workingHoursEntities.stream()
                .map(WorkingHoursEntity::getDayOfWeek)
                .collect(Collectors.toSet());
        List<Long> idsToDelete = existing.stream()
                .filter(e -> !newDays.contains(e.getDayOfWeek()))
                .map(WorkingHoursEntity::getId)
                .toList();
        if (!idsToDelete.isEmpty()) {
            workingHoursPort.deleteByIds(idsToDelete);
        }

        return workingHoursPort.saveAll(entitiesToSave);
    }

    @Override
    @Transactional
    public void deleteByTeamMemberId(Long teamMemberId) {
        workingHoursPort.deleteByTeamMemberId(teamMemberId);
    }

    @Override
    @Transactional
    public void deleteByTeamMemberIds(Collection<Long> teamMemberIds) {
        if (teamMemberIds == null || teamMemberIds.isEmpty()) {
            return;
        }
        workingHoursPort.deleteByTeamMemberIds(teamMemberIds);
    }
}
