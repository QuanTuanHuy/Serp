/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.customfield.port;

import java.util.List;

import serp.project.pmcore.domain.customfield.entity.CustomFieldContextDefaultValueEntity;

public interface ICustomFieldContextDefaultValuePort {
    List<CustomFieldContextDefaultValueEntity> getCustomFieldContextDefaultValuesByContextId(Long contextId);
}
