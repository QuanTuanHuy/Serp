/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.crm.infrastructure.store.model.WorkingHoursModel;

import java.util.Collection;
import java.util.List;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHoursModel, Long> {

    List<WorkingHoursModel> findByTeamMemberId(Long teamMemberId);

    List<WorkingHoursModel> findByTeamMemberIdIn(Collection<Long> teamMemberIds);

    void deleteByTeamMemberId(Long teamMemberId);

    void deleteByTeamMemberIdIn(Collection<Long> teamMemberIds);
}
