/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.query.PriorityListCriteria;
import serp.project.pmcore.domain.priority.query.PrioritySchemeListCriteria;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;
import serp.project.pmcore.domain.priority.service.IPriorityService;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPrioritySettingsOverviewQueryHandlerTest {

    private static final Long TENANT_ID = 20L;
    private static final Long HIGH_ID = 100L;
    private static final Long MEDIUM_ID = 101L;
    private static final Long SCHEME_ID = 200L;

    @Mock
    private IPriorityService priorityService;
    @Mock
    private IPrioritySchemeService prioritySchemeService;
    @Mock
    private IProjectReadPort projectReadPort;

    private GetPrioritySettingsOverviewQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetPrioritySettingsOverviewQueryHandler(
                priorityService,
                prioritySchemeService,
                projectReadPort
        );
    }

    @Test
    void handleShouldReturnPrioritiesSchemesAndBoundProjects() {
        PriorityEntity high = priority(HIGH_ID, "high", "High", false, 1);
        PriorityEntity medium = priority(MEDIUM_ID, "medium", "Medium", true, 2);
        PrioritySchemeEntity scheme = scheme(List.of(
                item(HIGH_ID, 1),
                item(MEDIUM_ID, 2)
        ));

        when(priorityService.listVisiblePriorities(eq(TENANT_ID), any(PriorityListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(high, medium), 2));
        when(prioritySchemeService.listVisiblePrioritySchemes(eq(TENANT_ID), any(PrioritySchemeListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(scheme), 1));
        when(prioritySchemeService.getVisiblePrioritySchemeDetailById(SCHEME_ID, TENANT_ID)).thenReturn(scheme);
        when(projectReadPort.getActiveProjectsByPrioritySchemeIds(List.of(SCHEME_ID), TENANT_ID))
                .thenReturn(List.of(project()));

        PrioritySettingsOverviewView result = handler.handle(new GetPrioritySettingsOverviewQuery(TENANT_ID));

        assertEquals(1, result.priorities().size());
        assertEquals(1, result.priorities().getFirst().relatedSchemes().size());
        assertFalse(result.priorities().getFirst().readOnly());

        PrioritySettingsOverviewView.PrioritySchemeView schemeView = result.prioritySchemes().getFirst();
        assertEquals(SCHEME_ID, schemeView.id());
        assertNull(schemeView.defaultPriorityId());
        assertEquals(1, schemeView.priorities().size());
        assertFalse(schemeView.priorities().getFirst().isDefault());
        assertEquals("SCRUM", schemeView.spaces().getFirst().key());

        verify(priorityService).listVisiblePriorities(eq(TENANT_ID),
                argThat(criteria -> Boolean.FALSE.equals(criteria.getIsSystem())));
        verify(prioritySchemeService).listVisiblePrioritySchemes(eq(TENANT_ID),
                argThat(criteria -> Boolean.FALSE.equals(criteria.getIsSystem())));
    }

    private PriorityEntity priority(Long id, String key, String name, boolean system, Integer sequence) {
        return PriorityEntity.builder()
                .id(id)
                .tenantId(system ? 0L : TENANT_ID)
                .priorityKey(key)
                .name(name)
                .description(name + " description")
                .color("#E34935")
                .sequence(sequence)
                .isSystem(system)
                .build();
    }

    private PrioritySchemeEntity scheme(List<PrioritySchemeItemEntity> items) {
        return PrioritySchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(TENANT_ID)
                .name("Default priority scheme")
                .description("Default scheme")
                .defaultPriorityId(MEDIUM_ID)
                .items(items)
                .build();
    }

    private PrioritySchemeItemEntity item(Long priorityId, Integer sequence) {
        return PrioritySchemeItemEntity.builder()
                .schemeId(SCHEME_ID)
                .priorityId(priorityId)
                .sequence(sequence)
                .build();
    }

    private ProjectEntity project() {
        return ProjectEntity.builder()
                .id(300L)
                .tenantId(TENANT_ID)
                .key("SCRUM")
                .name("Scrum 1")
                .prioritySchemeId(SCHEME_ID)
                .isArchived(false)
                .build();
    }
}
