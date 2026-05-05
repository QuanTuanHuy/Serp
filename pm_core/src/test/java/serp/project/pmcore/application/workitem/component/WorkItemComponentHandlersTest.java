/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.workitem.WorkItemComponentView;
import serp.project.pmcore.application.workitem.command.component.ManageWorkItemComponentsCommand;
import serp.project.pmcore.application.workitem.command.component.ManageWorkItemComponentsCommandHandler;
import serp.project.pmcore.application.workitem.command.component.RemoveWorkItemComponentCommand;
import serp.project.pmcore.application.workitem.command.component.RemoveWorkItemComponentCommandHandler;
import serp.project.pmcore.application.workitem.command.component.RemoveWorkItemComponentResult;
import serp.project.pmcore.application.workitem.query.component.ListWorkItemComponentsQuery;
import serp.project.pmcore.application.workitem.query.component.ListWorkItemComponentsQueryHandler;
import serp.project.pmcore.application.workitem.support.WorkItemComponentAccessHelper;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.service.IProjectComponentService;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemWritePort;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemComponentHandlersTest {

    private static final Long PROJECT_ID = 10L;
    private static final Long WORK_ITEM_ID = 20L;
    private static final Long COMPONENT_ID = 30L;
    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;

    @Mock
    private WorkItemComponentAccessHelper accessHelper;
    @Mock
    private IProjectComponentService projectComponentService;
    @Mock
    private IWorkItemWritePort workItemWritePort;
    @Mock
    private IWorkItemReadPort workItemReadPort;

    private ManageWorkItemComponentsCommandHandler manageHandler;
    private RemoveWorkItemComponentCommandHandler removeHandler;
    private ListWorkItemComponentsQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        manageHandler = new ManageWorkItemComponentsCommandHandler(
                accessHelper,
                projectComponentService,
                workItemWritePort,
                workItemReadPort
        );
        removeHandler = new RemoveWorkItemComponentCommandHandler(
                accessHelper,
                projectComponentService,
                workItemWritePort
        );
        listHandler = new ListWorkItemComponentsQueryHandler(
                accessHelper,
                workItemReadPort
        );
    }

    @Test
    void manageHandlerShouldAddComponentsAndReturnActiveList() {
        ProjectComponentEntity backend = component(COMPONENT_ID, "Backend");
        ProjectComponentEntity frontend = component(31L, "Frontend");
        when(projectComponentService.getComponentById(COMPONENT_ID, PROJECT_ID, TENANT_ID)).thenReturn(backend);
        when(projectComponentService.getComponentById(31L, PROJECT_ID, TENANT_ID)).thenReturn(frontend);
        when(workItemReadPort.getActiveComponentsByWorkItemId(WORK_ITEM_ID, TENANT_ID))
                .thenReturn(List.of(backend, frontend));

        List<WorkItemComponentView> result = manageHandler.handle(new ManageWorkItemComponentsCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                List.of(COMPONENT_ID, 31L, COMPONENT_ID),
                TENANT_ID,
                USER_ID,
                Set.of("devs")
        ));

        verify(accessHelper).requireEditableWorkItem(PROJECT_ID, WORK_ITEM_ID, TENANT_ID, USER_ID, Set.of("devs"));
        verify(workItemWritePort).addWorkItemComponents(WORK_ITEM_ID, TENANT_ID, USER_ID, List.of(COMPONENT_ID, 31L));
        assertEquals(2, result.size());
        assertEquals("Backend", result.getFirst().name());
    }

    @Test
    void removeHandlerShouldSoftDeleteExistingLink() {
        when(projectComponentService.getComponentById(COMPONENT_ID, PROJECT_ID, TENANT_ID))
                .thenReturn(component(COMPONENT_ID, "Backend"));
        when(workItemWritePort.removeWorkItemComponent(eq(WORK_ITEM_ID), eq(COMPONENT_ID), eq(TENANT_ID), eq(USER_ID), any()))
                .thenReturn(true);

        RemoveWorkItemComponentResult result = removeHandler.handle(new RemoveWorkItemComponentCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                COMPONENT_ID,
                TENANT_ID,
                USER_ID,
                Set.of("devs")
        ));

        verify(accessHelper).requireEditableWorkItem(PROJECT_ID, WORK_ITEM_ID, TENANT_ID, USER_ID, Set.of("devs"));
        assertEquals(true, result.removed());
        assertEquals(COMPONENT_ID, result.componentId());
    }

    @Test
    void removeHandlerShouldFailWhenLinkMissing() {
        when(projectComponentService.getComponentById(COMPONENT_ID, PROJECT_ID, TENANT_ID))
                .thenReturn(component(COMPONENT_ID, "Backend"));
        when(workItemWritePort.removeWorkItemComponent(eq(WORK_ITEM_ID), eq(COMPONENT_ID), eq(TENANT_ID), eq(USER_ID), any()))
                .thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> removeHandler.handle(new RemoveWorkItemComponentCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                COMPONENT_ID,
                TENANT_ID,
                USER_ID,
                Set.of()
        )));
    }

    @Test
    void listHandlerShouldCheckReadableAccessAndReturnViews() {
        ProjectComponentEntity backend = component(COMPONENT_ID, "Backend");
        when(workItemReadPort.getActiveComponentsByWorkItemId(WORK_ITEM_ID, TENANT_ID))
                .thenReturn(List.of(backend));

        List<WorkItemComponentView> result = listHandler.handle(new ListWorkItemComponentsQuery(
                PROJECT_ID,
                WORK_ITEM_ID,
                TENANT_ID,
                USER_ID,
                Set.of("devs")
        ));

        verify(accessHelper).requireReadableWorkItem(PROJECT_ID, WORK_ITEM_ID, TENANT_ID, USER_ID, Set.of("devs"));
        assertEquals(1, result.size());
        assertEquals(COMPONENT_ID, result.getFirst().id());
    }

    private ProjectComponentEntity component(Long id, String name) {
        return ProjectComponentEntity.builder()
                .id(id)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .name(name)
                .description(name + " component")
                .assigneeType("PROJECT_DEFAULT")
                .build();
    }
}
