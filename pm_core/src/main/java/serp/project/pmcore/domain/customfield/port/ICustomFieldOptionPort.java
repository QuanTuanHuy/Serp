/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.customfield.port;

import java.util.List;

import serp.project.pmcore.domain.customfield.entity.CustomFieldOptionEntity;

public interface ICustomFieldOptionPort {
    List<CustomFieldOptionEntity> getCustomFieldOptionsByContextId(Long contextId);
}
