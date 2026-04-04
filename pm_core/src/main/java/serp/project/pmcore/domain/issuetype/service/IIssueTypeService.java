/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.service;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;

public interface IIssueTypeService {
    IssueTypeEntity getIssueTypeById(Long issueTypeId, Long tenantId);
}
