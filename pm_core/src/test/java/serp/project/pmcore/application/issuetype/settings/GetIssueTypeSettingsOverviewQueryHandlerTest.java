/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.query.IssueTypeListCriteria;
import serp.project.pmcore.domain.issuetype.query.IssueTypeSchemeListCriteria;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetIssueTypeSettingsOverviewQueryHandlerTest {

    private static final Long TENANT_ID = 20L;
    private static final Long TASK_ID = 100L;
    private static final Long BUG_ID = 101L;
    private static final Long SCHEME_ID = 200L;

    @Mock
    private IIssueTypeService issueTypeService;
    @Mock
    private IIssueTypeSchemeService issueTypeSchemeService;
    @Mock
    private IProjectReadPort projectReadPort;

    private GetIssueTypeSettingsOverviewQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetIssueTypeSettingsOverviewQueryHandler(
                issueTypeService,
                issueTypeSchemeService,
                projectReadPort
        );
    }

    @Test
    void handleShouldReturnWorkTypesSchemesAndBoundProjects() {
        IssueTypeEntity task = issueType(TASK_ID, "task", "Task", false);
        IssueTypeEntity bug = issueType(BUG_ID, "bug", "Bug", true);
        IssueTypeSchemeEntity scheme = scheme(List.of(
                item(TASK_ID, 1),
                item(BUG_ID, 2)
        ));

        when(issueTypeService.listVisibleIssueTypes(eq(TENANT_ID), any(IssueTypeListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(task, bug), 2));
        when(issueTypeSchemeService.listVisibleIssueTypeSchemes(eq(TENANT_ID), any(IssueTypeSchemeListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(scheme), 1));
        when(issueTypeSchemeService.getVisibleIssueTypeSchemeDetailById(SCHEME_ID, TENANT_ID)).thenReturn(scheme);
        when(projectReadPort.getActiveProjectsByIssueTypeSchemeIds(List.of(SCHEME_ID), TENANT_ID))
                .thenReturn(List.of(project()));

        IssueTypeSettingsOverviewView result = handler.handle(new GetIssueTypeSettingsOverviewQuery(TENANT_ID));

        assertEquals(2, result.workTypes().size());
        assertEquals(1, result.workTypes().getFirst().relatedSchemes().size());
        assertFalse(result.workTypes().getFirst().readOnly());
        assertTrue(result.workTypes().get(1).readOnly());

        IssueTypeSettingsOverviewView.WorkTypeSchemeView schemeView = result.workTypeSchemes().getFirst();
        assertEquals(SCHEME_ID, schemeView.id());
        assertEquals(TASK_ID, schemeView.defaultIssueTypeId());
        assertEquals(2, schemeView.workTypes().size());
        assertTrue(schemeView.workTypes().getFirst().isDefault());
        assertEquals("PM", schemeView.spaces().getFirst().key());
    }

    private IssueTypeEntity issueType(Long id, String key, String name, boolean system) {
        return IssueTypeEntity.builder()
                .id(id)
                .tenantId(system ? 0L : TENANT_ID)
                .typeKey(key)
                .name(name)
                .description(name + " description")
                .hierarchyLevel(0)
                .isSystem(system)
                .build();
    }

    private IssueTypeSchemeEntity scheme(List<IssueTypeSchemeItemEntity> items) {
        return IssueTypeSchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(TENANT_ID)
                .name("Default Work Type Scheme")
                .description("Default scheme")
                .defaultIssueTypeId(TASK_ID)
                .items(items)
                .build();
    }

    private IssueTypeSchemeItemEntity item(Long issueTypeId, Integer sequence) {
        return IssueTypeSchemeItemEntity.builder()
                .schemeId(SCHEME_ID)
                .issueTypeId(issueTypeId)
                .sequence(sequence)
                .build();
    }

    private ProjectEntity project() {
        return ProjectEntity.builder()
                .id(300L)
                .tenantId(TENANT_ID)
                .key("PM")
                .name("PM Core")
                .issueTypeSchemeId(SCHEME_ID)
                .isArchived(false)
                .build();
    }
}
