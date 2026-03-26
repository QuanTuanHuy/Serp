/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.provisioning;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.dto.project.ProjectProvisioningRequest;
import serp.project.pmcore.domain.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.entity.CustomFieldContextEntity;
import serp.project.pmcore.domain.entity.CustomFieldContextIssueTypeEntity;
import serp.project.pmcore.domain.entity.CustomFieldContextProjectEntity;
import serp.project.pmcore.domain.entity.CustomFieldEntity;
import serp.project.pmcore.domain.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.entity.FieldConfigItemEntity;
import serp.project.pmcore.domain.entity.FieldConfigSchemeEntity;
import serp.project.pmcore.domain.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.domain.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.entity.ScreenSchemeEntity;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.project.ProjectSchemeBindings;
import serp.project.pmcore.domain.entity.workitem.IssueTypeEntity;
import serp.project.pmcore.domain.enums.ProvisioningMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.port.store.ICustomFieldContextDefaultValuePort;
import serp.project.pmcore.domain.port.store.ICustomFieldContextIssueTypePort;
import serp.project.pmcore.domain.port.store.ICustomFieldContextPort;
import serp.project.pmcore.domain.port.store.ICustomFieldContextProjectPort;
import serp.project.pmcore.domain.port.store.ICustomFieldOptionPort;
import serp.project.pmcore.domain.port.store.ICustomFieldPort;
import serp.project.pmcore.domain.port.store.IFieldConfigItemPort;
import serp.project.pmcore.domain.port.store.IFieldConfigSchemeItemPort;
import serp.project.pmcore.domain.port.store.IFieldConfigSchemePort;
import serp.project.pmcore.domain.port.store.IIssueTypePort;
import serp.project.pmcore.domain.port.store.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.domain.port.store.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.port.store.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.port.store.IScreenSchemeItemPort;
import serp.project.pmcore.domain.port.store.IScreenSchemePort;
import serp.project.pmcore.domain.port.store.IScreenTabFieldPort;
import serp.project.pmcore.domain.port.store.IScreenTabPort;
import serp.project.pmcore.domain.service.provisioning.materializer.CustomFieldMaterializer;
import serp.project.pmcore.domain.service.provisioning.materializer.IssueTypeMaterializer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectCustomFieldContextProvisioningServiceTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 99L;
    private static final Long PROJECT_ID = 10L;
    private static final String PROJECT_KEY = "SERP";
    private static final Long ISSUE_TYPE_SCHEME_ID = 100L;
    private static final Long ISSUE_TYPE_ID = 101L;
    private static final Long FIELD_CONFIG_SCHEME_ID = 200L;
    private static final Long FIELD_CONFIG_ID = 201L;
    private static final Long SCREEN_SCHEME_ROOT_ID = 300L;
    private static final Long SCREEN_SCHEME_ID = 301L;
    private static final Long SOURCE_CUSTOM_FIELD_ID = 400L;
    private static final Long TARGET_CUSTOM_FIELD_ID = 401L;
    private static final Long SOURCE_CONTEXT_ID = 500L;
    private static final Long TARGET_CONTEXT_ID = 501L;
    private static final Long SOURCE_ISSUE_TYPE_ID = 600L;
    private static final Long SOURCE_OPTION_ID = 700L;
    private static final Long TARGET_OPTION_ID = 701L;
    private static final String CUSTOM_FIELD_KEY = "customfield_10001";

    @Mock
    private IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    @Mock
    private IIssueTypePort issueTypePort;
    @Mock
    private IFieldConfigSchemePort fieldConfigSchemePort;
    @Mock
    private IFieldConfigSchemeItemPort fieldConfigSchemeItemPort;
    @Mock
    private IFieldConfigItemPort fieldConfigItemPort;
    @Mock
    private IIssueTypeScreenSchemePort issueTypeScreenSchemePort;
    @Mock
    private IIssueTypeScreenSchemeItemPort issueTypeScreenSchemeItemPort;
    @Mock
    private IScreenSchemePort screenSchemePort;
    @Mock
    private IScreenSchemeItemPort screenSchemeItemPort;
    @Mock
    private IScreenTabPort screenTabPort;
    @Mock
    private IScreenTabFieldPort screenTabFieldPort;
    @Mock
    private ICustomFieldPort customFieldPort;
    @Mock
    private ICustomFieldContextPort customFieldContextPort;
    @Mock
    private ICustomFieldContextProjectPort customFieldContextProjectPort;
    @Mock
    private ICustomFieldContextIssueTypePort customFieldContextIssueTypePort;
    @Mock
    private ICustomFieldOptionPort customFieldOptionPort;
    @Mock
    private ICustomFieldContextDefaultValuePort customFieldContextDefaultValuePort;
    @Mock
    private CustomFieldMaterializer customFieldMaterializer;
    @Mock
    private IssueTypeMaterializer issueTypeMaterializer;

    @Test
    void provisionShouldCloneRelevantContextTreeForTemplateDefault() {
        ProjectCustomFieldContextProvisioningService service = new ProjectCustomFieldContextProvisioningService(
                issueTypeSchemeItemPort,
                issueTypePort,
                fieldConfigSchemePort,
                fieldConfigSchemeItemPort,
                fieldConfigItemPort,
                issueTypeScreenSchemePort,
                issueTypeScreenSchemeItemPort,
                screenSchemePort,
                screenSchemeItemPort,
                screenTabPort,
                screenTabFieldPort,
                customFieldPort,
                customFieldContextPort,
                customFieldContextProjectPort,
                customFieldContextIssueTypePort,
                customFieldOptionPort,
                customFieldContextDefaultValuePort,
                customFieldMaterializer,
                issueTypeMaterializer
        );

        ProjectEntity project = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .key(PROJECT_KEY)
                .build();
        ProjectProvisioningRequest request = ProjectProvisioningRequest.builder()
                .tenantId(TENANT_ID)
                .userId(USER_ID)
                .projectId(PROJECT_ID)
                .projectKey(PROJECT_KEY)
                .provisioningMode(ProvisioningMode.TEMPLATE_DEFAULT)
                .requestedSchemeBindings(ProjectSchemeBindings.builder().build())
                .build();

        Map<SchemeType, Long> effectiveBindings = new EnumMap<>(SchemeType.class);
        effectiveBindings.put(SchemeType.ISSUE_TYPE, ISSUE_TYPE_SCHEME_ID);
        effectiveBindings.put(SchemeType.FIELD_CONFIG, FIELD_CONFIG_SCHEME_ID);
        effectiveBindings.put(SchemeType.SCREEN, SCREEN_SCHEME_ROOT_ID);

        when(issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(ISSUE_TYPE_SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(IssueTypeSchemeItemEntity.builder().issueTypeId(ISSUE_TYPE_ID).build()));
        when(issueTypePort.getIssueTypeById(ISSUE_TYPE_ID, TENANT_ID))
                .thenReturn(Optional.of(IssueTypeEntity.builder().id(ISSUE_TYPE_ID).tenantId(TENANT_ID).build()));

        when(fieldConfigSchemePort.getFieldConfigSchemeById(FIELD_CONFIG_SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(FieldConfigSchemeEntity.builder()
                        .id(FIELD_CONFIG_SCHEME_ID)
                        .tenantId(TENANT_ID)
                        .defaultFieldConfigId(FIELD_CONFIG_ID)
                        .build()));
        when(fieldConfigSchemeItemPort.getFieldConfigSchemeItemsBySchemeId(FIELD_CONFIG_SCHEME_ID, TENANT_ID))
                .thenReturn(List.of());
        when(fieldConfigItemPort.getFieldConfigItemsByFieldConfigId(FIELD_CONFIG_ID, TENANT_ID))
                .thenReturn(List.of(FieldConfigItemEntity.builder()
                        .fieldRefType("CUSTOM")
                        .fieldRef(CUSTOM_FIELD_KEY)
                        .build()));

        when(issueTypeScreenSchemePort.getIssueTypeScreenSchemeById(SCREEN_SCHEME_ROOT_ID, TENANT_ID))
                .thenReturn(Optional.of(IssueTypeScreenSchemeEntity.builder()
                        .id(SCREEN_SCHEME_ROOT_ID)
                        .tenantId(TENANT_ID)
                        .defaultScreenSchemeId(SCREEN_SCHEME_ID)
                        .build()));
        when(issueTypeScreenSchemeItemPort.getIssueTypeScreenSchemeItemsBySchemeId(SCREEN_SCHEME_ROOT_ID, TENANT_ID))
                .thenReturn(List.of());
        when(screenSchemePort.getScreenSchemeById(SCREEN_SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(ScreenSchemeEntity.builder()
                        .id(SCREEN_SCHEME_ID)
                        .tenantId(TENANT_ID)
                        .build()));
        when(screenSchemeItemPort.getScreenSchemeItemsByScreenSchemeId(SCREEN_SCHEME_ID, TENANT_ID))
                .thenReturn(List.of());

        CustomFieldEntity sourceField = CustomFieldEntity.builder()
                .id(SOURCE_CUSTOM_FIELD_ID)
                .tenantId(0L)
                .fieldKey(CUSTOM_FIELD_KEY)
                .name("Environment")
                .typeKey("select")
                .build();
        when(customFieldPort.getCustomFieldsByFieldKeysIncludingSystem(List.of(CUSTOM_FIELD_KEY), TENANT_ID))
                .thenReturn(List.of(sourceField));
        when(customFieldMaterializer.materialize(SOURCE_CUSTOM_FIELD_ID, TENANT_ID, USER_ID))
                .thenReturn(TARGET_CUSTOM_FIELD_ID);

        CustomFieldContextEntity sourceContext = CustomFieldContextEntity.builder()
                .id(SOURCE_CONTEXT_ID)
                .tenantId(0L)
                .customFieldId(SOURCE_CUSTOM_FIELD_ID)
                .name("Environment context")
                .appliesToAllProjects(false)
                .appliesToAllIssueTypes(false)
                .build();
        when(customFieldContextPort.getCustomFieldContextsByCustomFieldIdIncludingSystem(SOURCE_CUSTOM_FIELD_ID, TENANT_ID))
                .thenReturn(List.of(sourceContext));
        when(customFieldContextIssueTypePort.getCustomFieldContextIssueTypesByContextIdIncludingSystem(SOURCE_CONTEXT_ID, TENANT_ID))
                .thenReturn(List.of(CustomFieldContextIssueTypeEntity.builder()
                        .contextId(SOURCE_CONTEXT_ID)
                        .issueTypeId(SOURCE_ISSUE_TYPE_ID)
                        .build()));
        when(issueTypeMaterializer.materialize(SOURCE_ISSUE_TYPE_ID, TENANT_ID, USER_ID)).thenReturn(ISSUE_TYPE_ID);

        when(customFieldContextPort.getCustomFieldContextByName(TARGET_CUSTOM_FIELD_ID, "Environment context (SERP)", TENANT_ID))
                .thenReturn(Optional.empty());
        CustomFieldContextEntity targetContext = CustomFieldContextEntity.builder()
                .id(TARGET_CONTEXT_ID)
                .tenantId(TENANT_ID)
                .customFieldId(TARGET_CUSTOM_FIELD_ID)
                .name("Environment context (SERP)")
                .appliesToAllProjects(false)
                .appliesToAllIssueTypes(false)
                .build();
        when(customFieldContextPort.createCustomFieldContexts(anyList())).thenReturn(List.of(targetContext));
        when(customFieldContextPort.getCustomFieldContextsByCustomFieldIdIncludingSystem(TARGET_CUSTOM_FIELD_ID, TENANT_ID))
                .thenReturn(List.of(targetContext));

        CustomFieldContextProjectEntity targetProjectLink = CustomFieldContextProjectEntity.builder()
                .contextId(TARGET_CONTEXT_ID)
                .projectId(PROJECT_ID)
                .build();
        when(customFieldContextProjectPort.getCustomFieldContextProjectsByContextId(TARGET_CONTEXT_ID, TENANT_ID))
                .thenReturn(List.of(), List.of(targetProjectLink));

        CustomFieldContextIssueTypeEntity targetIssueTypeLink = CustomFieldContextIssueTypeEntity.builder()
                .contextId(TARGET_CONTEXT_ID)
                .issueTypeId(ISSUE_TYPE_ID)
                .build();
        when(customFieldContextIssueTypePort.getCustomFieldContextIssueTypesByContextId(TARGET_CONTEXT_ID, TENANT_ID))
                .thenReturn(List.of(), List.of(targetIssueTypeLink));

        CustomFieldOptionEntity sourceOption = CustomFieldOptionEntity.builder()
                .id(SOURCE_OPTION_ID)
                .customFieldContextId(SOURCE_CONTEXT_ID)
                .optionKey("prod")
                .value("prod")
                .sequence(0)
                .build();
        when(customFieldOptionPort.getCustomFieldOptionsByContextId(TARGET_CONTEXT_ID, TENANT_ID))
                .thenReturn(List.of());
        when(customFieldOptionPort.getCustomFieldOptionsByContextIdIncludingSystem(SOURCE_CONTEXT_ID, TENANT_ID))
                .thenReturn(List.of(sourceOption));
        when(customFieldOptionPort.createCustomFieldOptions(anyList()))
                .thenReturn(List.of(CustomFieldOptionEntity.builder()
                        .id(TARGET_OPTION_ID)
                        .customFieldContextId(TARGET_CONTEXT_ID)
                        .optionKey("prod")
                        .value("prod")
                        .sequence(0)
                        .build()));

        when(customFieldContextDefaultValuePort.getCustomFieldContextDefaultValuesByContextId(TARGET_CONTEXT_ID, TENANT_ID))
                .thenReturn(List.of());
        when(customFieldContextDefaultValuePort.getCustomFieldContextDefaultValuesByContextIdIncludingSystem(SOURCE_CONTEXT_ID, TENANT_ID))
                .thenReturn(List.of(CustomFieldContextDefaultValueEntity.builder()
                        .contextId(SOURCE_CONTEXT_ID)
                        .valueType("OPTION")
                        .optionValueId(SOURCE_OPTION_ID)
                        .sortOrder(0)
                        .build()));

        service.provision(project, request, effectiveBindings);

        ArgumentCaptor<List<CustomFieldContextEntity>> contextCaptor = ArgumentCaptor.forClass(List.class);
        verify(customFieldContextPort).createCustomFieldContexts(contextCaptor.capture());
        assertEquals("Environment context (SERP)", contextCaptor.getValue().getFirst().getName());
        assertEquals(TARGET_CUSTOM_FIELD_ID, contextCaptor.getValue().getFirst().getCustomFieldId());

        ArgumentCaptor<List<CustomFieldContextProjectEntity>> projectCaptor = ArgumentCaptor.forClass(List.class);
        verify(customFieldContextProjectPort).createCustomFieldContextProjects(projectCaptor.capture());
        assertEquals(PROJECT_ID, projectCaptor.getValue().getFirst().getProjectId());

        ArgumentCaptor<List<CustomFieldContextIssueTypeEntity>> issueTypeCaptor = ArgumentCaptor.forClass(List.class);
        verify(customFieldContextIssueTypePort).createCustomFieldContextIssueTypes(issueTypeCaptor.capture());
        assertEquals(ISSUE_TYPE_ID, issueTypeCaptor.getValue().getFirst().getIssueTypeId());

        ArgumentCaptor<List<CustomFieldContextDefaultValueEntity>> defaultCaptor = ArgumentCaptor.forClass(List.class);
        verify(customFieldContextDefaultValuePort).createCustomFieldContextDefaultValues(defaultCaptor.capture());
        assertEquals(TARGET_CONTEXT_ID, defaultCaptor.getValue().getFirst().getContextId());
        assertEquals(TARGET_OPTION_ID, defaultCaptor.getValue().getFirst().getOptionValueId());
    }

    @Test
    void provisionShouldSkipSharedFromExistingMode() {
        ProjectCustomFieldContextProvisioningService service = new ProjectCustomFieldContextProvisioningService(
                issueTypeSchemeItemPort,
                issueTypePort,
                fieldConfigSchemePort,
                fieldConfigSchemeItemPort,
                fieldConfigItemPort,
                issueTypeScreenSchemePort,
                issueTypeScreenSchemeItemPort,
                screenSchemePort,
                screenSchemeItemPort,
                screenTabPort,
                screenTabFieldPort,
                customFieldPort,
                customFieldContextPort,
                customFieldContextProjectPort,
                customFieldContextIssueTypePort,
                customFieldOptionPort,
                customFieldContextDefaultValuePort,
                customFieldMaterializer,
                issueTypeMaterializer
        );

        ProjectEntity project = ProjectEntity.builder().id(PROJECT_ID).tenantId(TENANT_ID).key(PROJECT_KEY).build();
        ProjectProvisioningRequest request = ProjectProvisioningRequest.builder()
                .tenantId(TENANT_ID)
                .userId(USER_ID)
                .projectId(PROJECT_ID)
                .projectKey(PROJECT_KEY)
                .provisioningMode(ProvisioningMode.SHARED_FROM_EXISTING)
                .build();

        Map<SchemeType, Long> effectiveBindings = new EnumMap<>(SchemeType.class);
        effectiveBindings.put(SchemeType.ISSUE_TYPE, ISSUE_TYPE_SCHEME_ID);
        effectiveBindings.put(SchemeType.FIELD_CONFIG, FIELD_CONFIG_SCHEME_ID);
        effectiveBindings.put(SchemeType.SCREEN, SCREEN_SCHEME_ROOT_ID);

        service.provision(project, request, effectiveBindings);

        verify(issueTypeSchemeItemPort, never()).getIssueTypeSchemeItemsBySchemeId(eq(ISSUE_TYPE_SCHEME_ID), eq(TENANT_ID));
    }
}
