/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import serp.project.crm.core.domain.entity.WorkingHoursEntity;

import java.util.Collection;
import java.util.List;

public interface IWorkingHoursPort {

    List<WorkingHoursEntity> findByTeamMemberId(Long teamMemberId);

    List<WorkingHoursEntity> findByTeamMemberIds(Collection<Long> teamMemberIds);

    List<WorkingHoursEntity> saveAll(List<WorkingHoursEntity> workingHoursEntities);

    void deleteByTeamMemberId(Long teamMemberId);

    void deleteByTeamMemberIds(Collection<Long> teamMemberIds);

    void deleteByIds(Collection<Long> ids);
}
