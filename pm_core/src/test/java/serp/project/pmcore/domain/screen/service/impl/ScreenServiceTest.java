/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.screen.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeScreenSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.screen.entity.ScreenEntity;
import serp.project.pmcore.domain.screen.entity.ScreenSchemeEntity;
import serp.project.pmcore.domain.screen.entity.ScreenSchemeItemEntity;
import serp.project.pmcore.domain.screen.port.IScreenPort;
import serp.project.pmcore.domain.screen.port.IScreenSchemeItemPort;
import serp.project.pmcore.domain.screen.port.IScreenSchemePort;
import serp.project.pmcore.domain.screen.port.IScreenTabFieldPort;
import serp.project.pmcore.domain.screen.port.IScreenTabPort;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScreenServiceTest {

    private static final Long TENANT_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long ISSUE_TYPE_ID = 100L;
    private static final Long ISSUE_TYPE_SCREEN_SCHEME_ID = 200L;
    private static final Long SCREEN_SCHEME_ID = 300L;
    private static final Long SCREEN_ID = 400L;

    @Mock
    private IIssueTypeScreenSchemePort issueTypeScreenSchemePort;
    @Mock
    private IIssueTypeScreenSchemeItemPort issueTypeScreenSchemeItemPort;
    @Mock
    private IScreenSchemePort screenSchemePort;
    @Mock
    private IScreenSchemeItemPort screenSchemeItemPort;
    @Mock
    private IScreenPort screenPort;
    @Mock
    private IScreenTabPort screenTabPort;
    @Mock
    private IScreenTabFieldPort screenTabFieldPort;

    private ScreenService screenService;

    @BeforeEach
    void setUp() {
        screenService = new ScreenService(
                issueTypeScreenSchemePort,
                issueTypeScreenSchemeItemPort,
                screenSchemePort,
                screenSchemeItemPort,
                screenPort,
                screenTabPort,
                screenTabFieldPort
        );
    }

    @Test
    void resolveScreenIdForOperationShouldReturnOperationScreen() {
        ProjectEntity project = ProjectEntity.builder()
                .id(PROJECT_ID)
                .issueTypeScreenSchemeId(ISSUE_TYPE_SCREEN_SCHEME_ID)
                .build();

        when(issueTypeScreenSchemePort.getIssueTypeScreenSchemeById(ISSUE_TYPE_SCREEN_SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(IssueTypeScreenSchemeEntity.builder()
                        .id(ISSUE_TYPE_SCREEN_SCHEME_ID)
                        .defaultScreenSchemeId(SCREEN_SCHEME_ID)
                        .build()));
        when(issueTypeScreenSchemeItemPort.getItemBySchemeIdAndIssueTypeId(
                ISSUE_TYPE_SCREEN_SCHEME_ID,
                ISSUE_TYPE_ID,
                TENANT_ID
        )).thenReturn(Optional.of(IssueTypeScreenSchemeItemEntity.builder()
                        .issueTypeId(ISSUE_TYPE_ID)
                        .screenSchemeId(SCREEN_SCHEME_ID)
                        .build()));
        when(screenSchemePort.getScreenSchemeById(SCREEN_SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(ScreenSchemeEntity.builder()
                        .id(SCREEN_SCHEME_ID)
                        .defaultScreenId(999L)
                        .build()));
        when(screenSchemeItemPort.getScreenSchemeItemsByScreenSchemeId(SCREEN_SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(ScreenSchemeItemEntity.builder()
                        .operationKey(WorkItemFieldConstants.CREATE_OPERATION_KEY)
                        .screenId(SCREEN_ID)
                        .build()));
        when(screenPort.getScreenById(SCREEN_ID, TENANT_ID))
                .thenReturn(Optional.of(ScreenEntity.builder().id(SCREEN_ID).build()));

        Long resolvedScreenId = screenService.resolveScreenIdForOperation(
                project.getId(),
                project.getIssueTypeScreenSchemeId(),
                ISSUE_TYPE_ID,
                WorkItemFieldConstants.CREATE_OPERATION_KEY,
                TENANT_ID
        );

        assertEquals(SCREEN_ID, resolvedScreenId);
    }
}
