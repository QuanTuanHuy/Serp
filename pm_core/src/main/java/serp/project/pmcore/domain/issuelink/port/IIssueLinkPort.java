/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuelink.port;

import serp.project.pmcore.domain.issuelink.entity.IssueLinkDetailEntity;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkEntity;

import java.util.List;
import java.util.Optional;

public interface IIssueLinkPort {
    IssueLinkEntity save(IssueLinkEntity issueLink);

    Optional<IssueLinkEntity> getById(Long id, Long tenantId);

    Optional<IssueLinkEntity> getActiveDuplicate(Long tenantId, Long sourceId, Long targetId, Long linkTypeId);

    List<IssueLinkDetailEntity> listByWorkItemId(Long tenantId, Long workItemId);
}
