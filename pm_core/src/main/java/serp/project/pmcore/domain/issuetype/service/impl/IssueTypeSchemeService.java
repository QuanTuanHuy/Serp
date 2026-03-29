/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService;
import serp.project.pmcore.domain.shared.exception.AppException;
import serp.project.pmcore.domain.shared.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueTypeSchemeService implements IIssueTypeSchemeService {

    private final IIssueTypeSchemeItemPort issueTypeSchemeItemPort;

    @Override
    public void validateIssueTypeInScheme(Long schemeId, Long issueTypeId, Long tenantId) {
        boolean exists = issueTypeSchemeItemPort.existsIssueTypeInScheme(schemeId, issueTypeId, tenantId);
        if (!exists) {
            throw new AppException(ErrorCode.ISSUE_TYPE_NOT_IN_SCHEME);
        }
    }

}
