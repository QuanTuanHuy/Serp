/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.project.dto.ProjectUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.port.write.IProjectWritePort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    private static final Long PROJECT_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IProjectReadPort projectReadPort;
    @Mock
    private IProjectWritePort projectWritePort;

    private ProjectService service;

    @BeforeEach
    void setUp() {
        service = new ProjectService(projectReadPort, projectWritePort);
    }

    @Test
    void updateProjectShouldApplyProvidedFieldsAndAllowClearingOptionalFields() {
        ProjectEntity existing = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .key("SERP")
                .name("SERP Platform")
                .description("Old description")
                .url("https://serp.local/project")
                .leadUserId(40L)
                .avatarId(50L)
                .categoryId(60L)
                .projectTypeKey("software")
                .isArchived(false)
                .createdAt(100L)
                .createdBy(USER_ID)
                .build();

        when(projectReadPort.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(projectWritePort.saveProject(existing)).thenReturn(existing);

        ProjectEntity updated = service.updateProject(
                PROJECT_ID,
                new ProjectUpdateData(
                        "SERP Platform Updated",
                        true,
                        "SPM",
                        true,
                        null,
                        true,
                        41L,
                        true,
                        null,
                        true,
                        null,
                        true,
                        null,
                        true
                ),
                TENANT_ID,
                USER_ID
        );

        verify(projectWritePort).saveProject(existing);
        assertEquals("SERP Platform Updated", updated.getName());
        assertEquals("SPM", updated.getKey());
        assertNull(updated.getDescription());
        assertEquals(41L, updated.getLeadUserId());
        assertNull(updated.getCategoryId());
        assertNull(updated.getUrl());
        assertNull(updated.getAvatarId());
        assertEquals(USER_ID, updated.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void updateProjectShouldRejectArchivedProject() {
        ProjectEntity archived = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .key("SERP")
                .name("SERP Platform")
                .isArchived(true)
                .build();

        when(projectReadPort.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(Optional.of(archived));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.updateProject(
                        PROJECT_ID,
                        new ProjectUpdateData("New Name", true, null, false, null, false, null, false, null, false, null, false, null, false),
                        TENANT_ID,
                        USER_ID
                )
        );

        assertEquals(DomainErrorCode.PROJECT_ARCHIVED, exception.getErrorCode());
    }
}
