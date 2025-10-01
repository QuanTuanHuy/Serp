/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.TeamEntity;

import java.util.List;
import java.util.Optional;

public interface ITeamPort {

    TeamEntity save(TeamEntity teamEntity);

    Optional<TeamEntity> findById(Long id, Long tenantId);

    Pair<List<TeamEntity>, Long> findAll(Long tenantId, PageRequest pageRequest);

    Pair<List<TeamEntity>, Long> findByLeaderId(Long leaderId, Long tenantId, PageRequest pageRequest);

    Boolean existsByName(String name, Long tenantId);

    void deleteById(Long id, Long tenantId);

    List<TeamEntity> findAllByTenantId(Long tenantId);
}
