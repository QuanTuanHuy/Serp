/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuelink.service;

import serp.project.pmcore.domain.issuelink.entity.IssueLinkDetailEntity;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkEntity;

import java.util.List;

public interface IIssueLinkService {
    IssueLinkEntity create(IssueLinkEntity draft, Long tenantId, Long userId);

    IssueLinkEntity getById(Long id, Long tenantId);

    IssueLinkEntity delete(IssueLinkEntity issueLink);

    List<IssueLinkDetailEntity> listByWorkItemId(Long tenantId, Long workItemId);
}
