/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.RepTimeBlockEntity;
import serp.project.crm.core.port.store.IRepTimeBlockPort;
import serp.project.crm.infrastructure.store.mapper.RepTimeBlockMapper;
import serp.project.crm.infrastructure.store.repository.RepTimeBlockRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RepTimeBlockAdapter implements IRepTimeBlockPort {

    private final RepTimeBlockRepository repTimeBlockRepository;
    private final RepTimeBlockMapper repTimeBlockMapper;

    @Override
    public RepTimeBlockEntity save(RepTimeBlockEntity entity) {
        return repTimeBlockMapper.toEntity(repTimeBlockRepository.save(repTimeBlockMapper.toModel(entity)));
    }

    @Override
    public Optional<RepTimeBlockEntity> findByActivityId(Long activityId, Long tenantId) {
        return repTimeBlockRepository.findByActivityIdAndTenantId(activityId, tenantId)
                .map(repTimeBlockMapper::toEntity);
    }

    @Override
    public void deleteByActivityId(Long activityId, Long tenantId) {
        repTimeBlockRepository.deleteByActivityIdAndTenantId(activityId, tenantId);
    }

    @Override
    public long countConflicts(Long teamMemberId, Long tenantId, Long startTime, Long endTime) {
        return repTimeBlockRepository.countConflicts(teamMemberId, tenantId, startTime, endTime);
    }

    @Override
    public List<RepTimeBlockEntity> findUpcomingByTeamMemberId(Long teamMemberId, Long tenantId, Long fromTime) {
        return repTimeBlockRepository.findByTeamMemberIdAndTenantIdAndEndTimeGreaterThan(teamMemberId, tenantId, fromTime)
                .stream()
                .map(repTimeBlockMapper::toEntity)
                .toList();
    }
}
