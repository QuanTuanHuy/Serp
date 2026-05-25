/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuelink.port;

import serp.project.pmcore.domain.issuelink.entity.IssueLinkTypeEntity;

import java.util.List;
import java.util.Optional;

public interface IIssueLinkTypePort {
    Optional<IssueLinkTypeEntity> getById(Long id, Long tenantId);

    Optional<IssueLinkTypeEntity> getByIdIncludingSystem(Long id, Long tenantId);

    Optional<IssueLinkTypeEntity> getByName(Long tenantId, String normalizedName);

    List<IssueLinkTypeEntity> listByTenant(Long tenantId);

    IssueLinkTypeEntity save(IssueLinkTypeEntity issueLinkType);
}
