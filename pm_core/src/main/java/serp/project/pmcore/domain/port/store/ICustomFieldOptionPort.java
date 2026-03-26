/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.CustomFieldOptionEntity;

import java.util.List;

public interface ICustomFieldOptionPort {
    List<CustomFieldOptionEntity> getCustomFieldOptionsByContextId(Long contextId);
}
