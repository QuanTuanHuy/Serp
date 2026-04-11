/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.customfield.service;

import serp.project.pmcore.domain.customfield.dto.ResolvedCustomFields;

import java.util.Map;

public interface IWorkItemCustomFieldResolver {
    ResolvedCustomFields resolveCustomFields(String issueTypeKey,
                                            Map<String, Object> requestCustomFields,
                                            Map<String, Boolean> requiredByFieldKey);
}
