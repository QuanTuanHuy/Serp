/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.CustomFieldContextEntity;

import java.util.List;

public interface ICustomFieldContextPort {
    List<CustomFieldContextEntity> getApplicableCustomFieldContexts(Long customFieldId,
                                                                    String issueTypeKey);
}
