/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuelink.service;

import serp.project.pmcore.domain.issuelink.entity.IssueLinkTypeEntity;
import serp.project.pmcore.domain.issuelink.query.IssueLinkTypeListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

public interface IIssueLinkTypeService {
    IssueLinkTypeEntity create(IssueLinkTypeEntity draft, Long tenantId, Long userId);

    IssueLinkTypeEntity update(Long id, IssueLinkTypeEntity changes, Long tenantId, Long userId);

    IssueLinkTypeEntity getById(Long id, Long tenantId);

    IssueLinkTypeEntity getVisibleById(Long id, Long tenantId);

    PageResult<IssueLinkTypeEntity> listVisible(Long tenantId, IssueLinkTypeListCriteria criteria);

    IssueLinkTypeEntity delete(Long id, Long tenantId, Long userId);
}
