/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.CustomFieldEntity;

import java.util.List;

public interface ICustomFieldPort {
    List<CustomFieldEntity> getCustomFieldsByFieldKeys(List<String> fieldKeys);
}
