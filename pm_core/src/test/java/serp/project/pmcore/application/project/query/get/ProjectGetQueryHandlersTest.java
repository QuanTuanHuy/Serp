/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.get;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.IProjectCategoryPort;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectGetQueryHandlersTest {

    private static final Long TENANT_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long CATEGORY_ID = 20L;
    private static final String PROJECT_KEY = "SERP";

    @Mock
    private IProjectReadPort projectPort;
    @Mock
    private IProjectCategoryPort projectCategoryPort;

    private GetProjectByIdQueryHandler getProjectByIdQueryHandler;
    private GetProjectByKeyQueryHandler getProjectByKeyQueryHandler;

    @BeforeEach
    void setUp() {
        ProjectDetailViewFactory projectDetailViewFactory = new ProjectDetailViewFactory(projectCategoryPort);
        getProjectByIdQueryHandler = new GetProjectByIdQueryHandler(projectPort, projectDetailViewFactory);
        getProjectByKeyQueryHandler = new GetProjectByKeyQueryHandler(projectPort, projectDetailViewFactory);
    }

    @Test
    void handleByIdShouldExpandCategoryWhenRequested() {
        when(projectPort.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(Optional.of(project()));
        when(projectCategoryPort.getCategoryById(CATEGORY_ID, TENANT_ID)).thenReturn(Optional.of(
                ProjectCategoryEntity.builder()
                        .id(CATEGORY_ID)
                        .name("Software")
                        .build()
        ));

        ProjectDetailView response = getProjectByIdQueryHandler.handle(
                new GetProjectByIdQuery(PROJECT_ID, TENANT_ID, Set.of(ProjectExpandOption.CATEGORY))
        );

        assertEquals(PROJECT_ID, response.getId());
        assertEquals(PROJECT_KEY, response.getKey());
        assertNotNull(response.getCategory());
        assertEquals(CATEGORY_ID, response.getCategory().id());
        assertEquals("Software", response.getCategory().name());
        verify(projectCategoryPort).getCategoryById(CATEGORY_ID, TENANT_ID);
    }

    @Test
    void handleByKeyShouldSkipCategoryLookupWhenExpandIsEmpty() {
        when(projectPort.getProjectByKey(PROJECT_KEY, TENANT_ID)).thenReturn(Optional.of(project()));

        ProjectDetailView response = getProjectByKeyQueryHandler.handle(
                new GetProjectByKeyQuery(PROJECT_KEY, TENANT_ID)
        );

        assertEquals(PROJECT_ID, response.getId());
        assertEquals(PROJECT_KEY, response.getKey());
        assertNull(response.getCategory());
        verifyNoInteractions(projectCategoryPort);
    }

    private ProjectEntity project() {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .categoryId(CATEGORY_ID)
                .key(PROJECT_KEY)
                .name("SERP Platform")
                .projectTypeKey("software")
                .build();
    }
}
