/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.customfield.port;

import java.util.List;

import serp.project.pmcore.domain.customfield.entity.CustomFieldContextEntity;

public interface ICustomFieldContextPort {
    List<CustomFieldContextEntity> getApplicableCustomFieldContexts(Long customFieldId,
                                                                    String issueTypeKey);
}
