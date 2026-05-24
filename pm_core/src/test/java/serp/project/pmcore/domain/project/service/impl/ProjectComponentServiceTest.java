/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.project.dto.ProjectComponentUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.IProjectComponentPort;
import serp.project.pmcore.domain.project.query.ProjectComponentListCriteria;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectComponentServiceTest {

    private static final Long COMPONENT_ID = 10L;
    private static final Long PROJECT_ID = 20L;
    private static final Long TENANT_ID = 30L;
    private static final Long USER_ID = 40L;

    @Mock
    private IProjectComponentPort projectComponentPort;
    @Mock
    private IProjectService projectService;
    @Mock
    private ProjectComponentLeadValidator projectComponentLeadValidator;

    private ProjectComponentService service;

    @BeforeEach
    void setUp() {
        service = new ProjectComponentService(projectComponentPort, projectService, projectComponentLeadValidator);
    }

    @Test
    void createComponentShouldPersistNormalizedProjectScopedComponent() {
        ProjectComponentEntity draft = ProjectComponentEntity.builder()
                .projectId(PROJECT_ID)
                .name(" Backend ")
                .description(" API ")
                .leadUserId(99L)
                .build();
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(activeProject());
        when(projectComponentPort.existsByProjectIdAndName(PROJECT_ID, TENANT_ID, "Backend")).thenReturn(false);
        when(projectComponentPort.createComponent(any(ProjectComponentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectComponentEntity created = service.createComponent(draft, TENANT_ID, USER_ID);

        ArgumentCaptor<ProjectComponentEntity> captor = ArgumentCaptor.forClass(ProjectComponentEntity.class);
        verify(projectComponentPort).createComponent(captor.capture());
        verify(projectComponentLeadValidator).validateLeadUserExists(99L);
        ProjectComponentEntity persisted = captor.getValue();
        assertEquals(TENANT_ID, persisted.getTenantId());
        assertEquals(PROJECT_ID, persisted.getProjectId());
        assertEquals("Backend", persisted.getName());
        assertEquals("API", persisted.getDescription());
        assertEquals("PROJECT_DEFAULT", persisted.getAssigneeType());
        assertNotNull(persisted.getCreatedAt());
        assertSame(persisted, created);
    }

    @Test
    void createComponentShouldRejectArchivedProject() {
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(archivedProject());

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.createComponent(ProjectComponentEntity.builder()
                        .projectId(PROJECT_ID)
                        .name("Backend")
                        .build(), TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.PROJECT_ARCHIVED, exception.getErrorCode());
    }

    @Test
    void createComponentShouldRejectDuplicateName() {
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(activeProject());
        when(projectComponentPort.existsByProjectIdAndName(PROJECT_ID, TENANT_ID, "Backend")).thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.createComponent(ProjectComponentEntity.builder()
                        .projectId(PROJECT_ID)
                        .name("Backend")
                        .build(), TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.COMPONENT_NAME_ALREADY_EXISTS, exception.getErrorCode());
        verify(projectComponentPort, never()).createComponent(any(ProjectComponentEntity.class));
    }

    @Test
    void updateComponentShouldApplyMutableFieldsAndValidateLead() {
        ProjectComponentEntity existing = existingComponent();
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(activeProject());
        when(projectComponentPort.getComponentById(COMPONENT_ID, PROJECT_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(projectComponentPort.existsByProjectIdAndName(PROJECT_ID, TENANT_ID, "Frontend")).thenReturn(false);

        ProjectComponentEntity updated = service.updateComponent(
                COMPONENT_ID,
                PROJECT_ID,
                new ProjectComponentUpdateData("Frontend", true, null, true, 88L, true, "COMPONENT_LEAD", true),
                TENANT_ID,
                USER_ID
        );

        verify(projectComponentLeadValidator).validateLeadUserExists(88L);
        verify(projectComponentPort).updateComponent(existing);
        assertEquals("Frontend", updated.getName());
        assertNull(updated.getDescription());
        assertEquals(88L, updated.getLeadUserId());
        assertEquals("COMPONENT_LEAD", updated.getAssigneeType());
    }

    @Test
    void listComponentsShouldDelegateToPort() {
        ProjectComponentListCriteria criteria = ProjectComponentListCriteria.builder().search("back").build();
        PageResult<ProjectComponentEntity> expected = new PageResult<>(List.of(existingComponent()), 1L);
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(activeProject());
        when(projectComponentPort.listComponents(PROJECT_ID, TENANT_ID, criteria)).thenReturn(expected);
        when(projectComponentPort.countActiveIssuesByComponentIds(PROJECT_ID, TENANT_ID, List.of(COMPONENT_ID)))
                .thenReturn(Map.of(COMPONENT_ID, 5L));

        PageResult<ProjectComponentEntity> result = service.listComponents(PROJECT_ID, TENANT_ID, criteria);

        assertSame(expected, result);
        assertEquals(5L, result.items().getFirst().getIssueCount());
    }

    @Test
    void deleteComponentShouldSoftDeleteAndRemoveLinks() {
        ProjectComponentEntity existing = existingComponent();
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(activeProject());
        when(projectComponentPort.getComponentById(COMPONENT_ID, PROJECT_ID, TENANT_ID)).thenReturn(Optional.of(existing));

        ProjectComponentEntity deleted = service.deleteComponent(COMPONENT_ID, PROJECT_ID, TENANT_ID, USER_ID);

        verify(projectComponentPort).updateComponent(existing);
        verify(projectComponentPort).deleteComponentLinks(COMPONENT_ID, TENANT_ID);
        assertEquals(USER_ID, deleted.getUpdatedBy());
        assertNotNull(deleted.getDeletedAt());
    }

    private ProjectEntity activeProject() {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .isArchived(false)
                .build();
    }

    private ProjectEntity archivedProject() {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .isArchived(true)
                .build();
    }

    private ProjectComponentEntity existingComponent() {
        return ProjectComponentEntity.builder()
                .id(COMPONENT_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .name("Backend")
                .description("API")
                .leadUserId(77L)
                .assigneeType("PROJECT_DEFAULT")
                .build();
    }
}
