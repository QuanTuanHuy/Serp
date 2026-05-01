/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.WorkingHoursEntity;
import serp.project.crm.core.port.store.IWorkingHoursPort;
import serp.project.crm.infrastructure.store.mapper.WorkingHoursMapper;
import serp.project.crm.infrastructure.store.repository.WorkingHoursRepository;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkingHoursAdapter implements IWorkingHoursPort {

    private final WorkingHoursRepository workingHoursRepository;
    private final WorkingHoursMapper workingHoursMapper;

    @Override
    public List<WorkingHoursEntity> findByTeamMemberId(Long teamMemberId) {
        return workingHoursMapper.toEntityList(workingHoursRepository.findByTeamMemberId(teamMemberId));
    }

    @Override
    public List<WorkingHoursEntity> findByTeamMemberIds(Collection<Long> teamMemberIds) {
        return workingHoursMapper.toEntityList(workingHoursRepository.findByTeamMemberIdIn(teamMemberIds));
    }

    @Override
    public List<WorkingHoursEntity> saveAll(List<WorkingHoursEntity> workingHoursEntities) {
        return workingHoursMapper.toEntityList(
                workingHoursRepository.saveAll(workingHoursMapper.toModelList(workingHoursEntities)));
    }

    @Override
    public void deleteByTeamMemberId(Long teamMemberId) {
        workingHoursRepository.deleteByTeamMemberId(teamMemberId);
    }

    @Override
    public void deleteByTeamMemberIds(Collection<Long> teamMemberIds) {
        workingHoursRepository.deleteByTeamMemberIdIn(teamMemberIds);
    }

    @Override
    public void deleteByIds(Collection<Long> ids) {
        workingHoursRepository.deleteAllById(ids);
    }
}
