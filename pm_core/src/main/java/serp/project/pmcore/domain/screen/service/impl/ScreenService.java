/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.screen.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeScreenSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.screen.entity.ScreenEntity;
import serp.project.pmcore.domain.screen.entity.ScreenSchemeEntity;
import serp.project.pmcore.domain.screen.entity.ScreenSchemeItemEntity;
import serp.project.pmcore.domain.screen.entity.ScreenTabEntity;
import serp.project.pmcore.domain.screen.entity.ScreenTabFieldEntity;
import serp.project.pmcore.domain.screen.port.IScreenPort;
import serp.project.pmcore.domain.screen.port.IScreenSchemeItemPort;
import serp.project.pmcore.domain.screen.port.IScreenSchemePort;
import serp.project.pmcore.domain.screen.port.IScreenTabFieldPort;
import serp.project.pmcore.domain.screen.port.IScreenTabPort;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.screen.service.IScreenService;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreenService implements IScreenService {

    private final IIssueTypeScreenSchemePort issueTypeScreenSchemePort;
    private final IIssueTypeScreenSchemeItemPort issueTypeScreenSchemeItemPort;
    private final IScreenSchemePort screenSchemePort;
    private final IScreenSchemeItemPort screenSchemeItemPort;
    private final IScreenPort screenPort;
    private final IScreenTabPort screenTabPort;
    private final IScreenTabFieldPort screenTabFieldPort;

    @Override
    public List<ScreenTabFieldEntity> getScreenTabFieldsByScreenId(Long screenId, Long tenantId) {
        getScreenById(screenId, tenantId);

        List<ScreenTabEntity> tabs = screenTabPort.getScreenTabsByScreenId(screenId, tenantId);
        if (tabs.isEmpty()) {
            log.debug("[ScreenService] No tabs found for screen: id={}, tenantId={}", screenId, tenantId);
            return Collections.emptyList();
        }
        List<Long> tabIds = tabs.stream().map(ScreenTabEntity::getId).toList();
        return screenTabFieldPort.getScreenTabFieldsByScreenTabIds(tabIds, tenantId);
    }

    @Override
    public ScreenEntity getScreenById(Long screenId, Long tenantId) {
        return screenPort.getScreenById(screenId, tenantId).orElseThrow(() -> {
            log.warn("[ScreenService] Screen not found: id={}, tenantId={}", screenId, tenantId);
            return new ResourceNotFoundException(
                    DomainErrorCode.SCREEN_NOT_FOUND,
                    String.format("Screen not found: id=%d, tenantId=%d", screenId, tenantId)
            );
        });
    }

    @Override
    public Long resolveScreenIdForOperation(Long projectId,
                                            Long issueTypeScreenSchemeId,
                                            Long issueTypeId,
                                            String operationKey,
                                            Long tenantId) {
        if (issueTypeScreenSchemeId == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_NOT_FOUND,
                    "Project has no issue type screen scheme binding: projectId=" + projectId
            );
        }

        IssueTypeScreenSchemeEntity issueTypeScreenScheme = issueTypeScreenSchemePort
                .getIssueTypeScreenSchemeById(issueTypeScreenSchemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_NOT_FOUND,
                        "Issue type screen scheme not found: id=" + issueTypeScreenSchemeId
                ));

        Long screenSchemeId = issueTypeScreenSchemeItemPort
                .getIssueTypeScreenSchemeItemsBySchemeId(issueTypeScreenSchemeId, tenantId)
                .stream()
                .filter(item -> issueTypeId.equals(item.getIssueTypeId()))
                .map(IssueTypeScreenSchemeItemEntity::getScreenSchemeId)
                .findFirst()
                .orElse(issueTypeScreenScheme.getDefaultScreenSchemeId());

        if (screenSchemeId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_COVERAGE_MISSING,
                    "Issue type screen scheme does not cover issueTypeId=" + issueTypeId + " for projectId=" + projectId
            );
        }

        ScreenSchemeEntity screenScheme = screenSchemePort.getScreenSchemeById(screenSchemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.SCREEN_SCHEME_NOT_FOUND,
                        "Screen scheme not found: id=" + screenSchemeId
                ));

        Long screenId = screenSchemeItemPort.getScreenSchemeItemsByScreenSchemeId(screenSchemeId, tenantId)
                .stream()
                .filter(item -> operationKey.equalsIgnoreCase(item.getOperationKey()))
                .map(ScreenSchemeItemEntity::getScreenId)
                .findFirst()
                .orElse(screenScheme.getDefaultScreenId());

        if (screenId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.SCREEN_SCHEME_OPERATION_COVERAGE_MISSING,
                    operationKey + " screen is not resolvable for screenSchemeId=" + screenSchemeId
            );
        }

        getScreenById(screenId, tenantId);
        return screenId;
    }

}
