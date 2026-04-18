/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.issuetypescheme.command.create.CreateIssueTypeSchemeCommand;
import serp.project.pmcore.application.issuetypescheme.command.create.CreateIssueTypeSchemeCommandHandler;
import serp.project.pmcore.application.issuetypescheme.command.delete.DeleteIssueTypeSchemeCommand;
import serp.project.pmcore.application.issuetypescheme.command.delete.DeleteIssueTypeSchemeCommandHandler;
import serp.project.pmcore.application.issuetypescheme.command.delete.DeleteIssueTypeSchemeResult;
import serp.project.pmcore.application.issuetypescheme.command.manageitems.ManageIssueTypeSchemeItemsCommand;
import serp.project.pmcore.application.issuetypescheme.command.manageitems.ManageIssueTypeSchemeItemsCommandHandler;
import serp.project.pmcore.application.issuetypescheme.command.update.UpdateIssueTypeSchemeCommand;
import serp.project.pmcore.application.issuetypescheme.command.update.UpdateIssueTypeSchemeCommandHandler;
import serp.project.pmcore.application.issuetypescheme.query.get.GetIssueTypeSchemeByIdQuery;
import serp.project.pmcore.application.issuetypescheme.query.get.GetIssueTypeSchemeByIdQueryHandler;
import serp.project.pmcore.application.issuetypescheme.query.list.ListIssueTypeSchemesQuery;
import serp.project.pmcore.application.issuetypescheme.query.list.ListIssueTypeSchemesQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.issuetype.dto.IssueTypeSchemeUpdateData;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.query.IssueTypeSchemeListCriteria;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueTypeSchemeHandlersTest {

    private static final Long SCHEME_ID = 10L;
    private static final Long ISSUE_TYPE_ID = 11L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IIssueTypeSchemeService issueTypeSchemeService;
    @Mock
    private IIssueTypeService issueTypeService;

    private CreateIssueTypeSchemeCommandHandler createHandler;
    private UpdateIssueTypeSchemeCommandHandler updateHandler;
    private DeleteIssueTypeSchemeCommandHandler deleteHandler;
    private ManageIssueTypeSchemeItemsCommandHandler manageItemsHandler;
    private GetIssueTypeSchemeByIdQueryHandler getHandler;
    private ListIssueTypeSchemesQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreateIssueTypeSchemeCommandHandler(issueTypeSchemeService);
        updateHandler = new UpdateIssueTypeSchemeCommandHandler(issueTypeSchemeService);
        deleteHandler = new DeleteIssueTypeSchemeCommandHandler(issueTypeSchemeService);
        manageItemsHandler = new ManageIssueTypeSchemeItemsCommandHandler(issueTypeSchemeService, issueTypeService);
        getHandler = new GetIssueTypeSchemeByIdQueryHandler(issueTypeSchemeService, issueTypeService);
        listHandler = new ListIssueTypeSchemesQueryHandler(issueTypeSchemeService);
    }

    @Test
    void createHandlerShouldReturnCreatedSchemeView() {
        IssueTypeSchemeEntity created = scheme(false);
        when(issueTypeSchemeService.createIssueTypeScheme(any(IssueTypeSchemeEntity.class), eq(TENANT_ID), eq(USER_ID)))
                .thenReturn(created);

        IssueTypeSchemeView result = createHandler.handle(new CreateIssueTypeSchemeCommand(
                "Team Managed",
                "Default scheme",
                ISSUE_TYPE_ID,
                TENANT_ID,
                USER_ID
        ));

        ArgumentCaptor<IssueTypeSchemeEntity> captor = ArgumentCaptor.forClass(IssueTypeSchemeEntity.class);
        verify(issueTypeSchemeService).createIssueTypeScheme(captor.capture(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Team Managed", captor.getValue().getName());
        assertEquals(SCHEME_ID, result.id());
        assertFalse(result.readOnly());
    }

    @Test
    void updateHandlerShouldReturnUpdatedSchemeView() {
        IssueTypeSchemeEntity updated = scheme(false);
        updated.setName("Updated Scheme");
        when(issueTypeSchemeService.updateIssueTypeScheme(any(), any(), any(), any())).thenReturn(updated);

        IssueTypeSchemeView result = updateHandler.handle(new UpdateIssueTypeSchemeCommand(
                SCHEME_ID,
                new IssueTypeSchemeUpdateData("Updated Scheme", true, null, false, null, false),
                TENANT_ID,
                USER_ID
        ));

        verify(issueTypeSchemeService).updateIssueTypeScheme(eq(SCHEME_ID), any(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Updated Scheme", result.name());
    }

    @Test
    void getHandlerShouldReturnDetailWithItemsAndReadOnlyFlag() {
        IssueTypeSchemeEntity systemScheme = scheme(true);
        systemScheme.setItems(List.of(item(1L, ISSUE_TYPE_ID, 1)));
        when(issueTypeSchemeService.getVisibleIssueTypeSchemeDetailById(SCHEME_ID, TENANT_ID)).thenReturn(systemScheme);
        when(issueTypeService.getVisibleIssueTypeById(ISSUE_TYPE_ID, TENANT_ID)).thenReturn(issueType());

        IssueTypeSchemeDetailView result = getHandler.handle(new GetIssueTypeSchemeByIdQuery(SCHEME_ID, TENANT_ID));

        assertTrue(result.readOnly());
        assertEquals(1, result.items().size());
        assertEquals("task", result.items().getFirst().issueType().typeKey());
    }

    @Test
    void listHandlerShouldBuildCriteriaAndPaginateSchemes() {
        when(issueTypeSchemeService.listVisibleIssueTypeSchemes(eq(TENANT_ID), any(IssueTypeSchemeListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(scheme(false)), 2L));

        PageView<IssueTypeSchemeView> result = listHandler.handle(new ListIssueTypeSchemesQuery(
                TENANT_ID,
                "team",
                false,
                0,
                1,
                "name",
                "ASC"
        ));

        ArgumentCaptor<IssueTypeSchemeListCriteria> criteriaCaptor = ArgumentCaptor.forClass(IssueTypeSchemeListCriteria.class);
        verify(issueTypeSchemeService).listVisibleIssueTypeSchemes(eq(TENANT_ID), criteriaCaptor.capture());
        assertEquals("team", criteriaCaptor.getValue().getSearch());
        assertEquals(false, criteriaCaptor.getValue().getIsSystem());
        assertEquals(2, result.totalItems());
        assertEquals(2, result.totalPages());
    }

    @Test
    void manageItemsHandlerShouldReturnUpdatedDetail() {
        IssueTypeSchemeEntity updated = scheme(false);
        updated.setItems(List.of(item(1L, ISSUE_TYPE_ID, 1)));
        when(issueTypeSchemeService.replaceIssueTypeSchemeItems(SCHEME_ID, List.of(ISSUE_TYPE_ID), TENANT_ID, USER_ID))
                .thenReturn(updated);
        when(issueTypeService.getVisibleIssueTypeById(ISSUE_TYPE_ID, TENANT_ID)).thenReturn(issueType());

        IssueTypeSchemeDetailView result = manageItemsHandler.handle(new ManageIssueTypeSchemeItemsCommand(
                SCHEME_ID,
                List.of(ISSUE_TYPE_ID),
                TENANT_ID,
                USER_ID
        ));

        assertEquals(1, result.items().size());
        assertEquals(ISSUE_TYPE_ID, result.items().getFirst().issueTypeId());
    }

    @Test
    void deleteHandlerShouldReturnDeleteConfirmation() {
        IssueTypeSchemeEntity deleted = scheme(false);
        deleted.setDeletedAt(500L);
        deleted.setUpdatedBy(USER_ID);
        when(issueTypeSchemeService.deleteIssueTypeScheme(SCHEME_ID, TENANT_ID, USER_ID)).thenReturn(deleted);

        DeleteIssueTypeSchemeResult result = deleteHandler.handle(new DeleteIssueTypeSchemeCommand(
                SCHEME_ID,
                TENANT_ID,
                USER_ID
        ));

        assertTrue(result.deleted());
        assertEquals(500L, result.deletedAt());
    }

    private IssueTypeSchemeEntity scheme(boolean system) {
        return IssueTypeSchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(system ? 0L : TENANT_ID)
                .name("Team Managed")
                .description("Default scheme")
                .defaultIssueTypeId(ISSUE_TYPE_ID)
                .build();
    }

    private IssueTypeSchemeItemEntity item(Long id, Long issueTypeId, Integer sequence) {
        return IssueTypeSchemeItemEntity.builder()
                .id(id)
                .schemeId(SCHEME_ID)
                .issueTypeId(issueTypeId)
                .sequence(sequence)
                .build();
    }

    private IssueTypeEntity issueType() {
        return IssueTypeEntity.builder()
                .id(ISSUE_TYPE_ID)
                .tenantId(TENANT_ID)
                .typeKey("task")
                .name("Task")
                .hierarchyLevel(1)
                .isSystem(false)
                .build();
    }
}
