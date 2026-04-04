/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueTypeService implements IIssueTypeService {

    private final IIssueTypePort issueTypePort;

    @Override
    public IssueTypeEntity getIssueTypeById(Long issueTypeId, Long tenantId) {
        return issueTypePort.getIssueTypeById(issueTypeId, tenantId)
                .orElseThrow(() -> {
                    log.error("Issue type not found: id={}", issueTypeId);
                    return ResourceNotFoundException.issueType(issueTypeId);
                });
    }
}
