/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import serp.project.crm.core.domain.entity.RepTimeBlockEntity;

import java.util.List;
import java.util.Optional;

public interface IRepTimeBlockPort {

    RepTimeBlockEntity save(RepTimeBlockEntity entity);

    Optional<RepTimeBlockEntity> findByActivityId(Long activityId, Long tenantId);

    void deleteByActivityId(Long activityId, Long tenantId);

    long countConflicts(Long teamMemberId, Long tenantId, Long startTime, Long endTime);

    List<RepTimeBlockEntity> findUpcomingByTeamMemberId(Long teamMemberId, Long tenantId, Long fromTime);
}
