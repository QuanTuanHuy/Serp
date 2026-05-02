/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import serp.project.crm.core.domain.entity.WorkingHoursEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IWorkingHoursService {

    List<WorkingHoursEntity> getByTeamMemberId(Long teamMemberId);

    Map<Long, List<WorkingHoursEntity>> getByTeamMemberIds(Collection<Long> teamMemberIds);

    List<WorkingHoursEntity> replaceByTeamMemberId(Long teamMemberId, List<WorkingHoursEntity> workingHoursEntities);

    void deleteByTeamMemberId(Long teamMemberId);

    void deleteByTeamMemberIds(Collection<Long> teamMemberIds);
}
