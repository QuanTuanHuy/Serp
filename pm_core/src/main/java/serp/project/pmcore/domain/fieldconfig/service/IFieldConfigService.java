/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.fieldconfig.service;

public interface IFieldConfigService {
    Long resolveFieldConfigId(Long fieldConfigSchemeId, Long issueTypeId, Long tenantId);
}
