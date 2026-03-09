/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.core.port.store.IIssueTypeSchemeItemPort;
import serp.project.pmcore.core.service.IIssueTypeSchemeService;

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
