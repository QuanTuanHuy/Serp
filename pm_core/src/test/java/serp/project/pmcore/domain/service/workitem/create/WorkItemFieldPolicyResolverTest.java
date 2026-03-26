/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.workitem.create;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.dto.workitem.create.CreateFieldRules;
import serp.project.pmcore.domain.dto.workitem.create.FieldPolicy;
import serp.project.pmcore.domain.entity.FieldConfigEntity;
import serp.project.pmcore.domain.entity.FieldConfigItemEntity;
import serp.project.pmcore.domain.entity.FieldConfigSchemeEntity;
import serp.project.pmcore.domain.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.domain.entity.ScreenEntity;
import serp.project.pmcore.domain.entity.ScreenSchemeEntity;
import serp.project.pmcore.domain.entity.ScreenSchemeItemEntity;
import serp.project.pmcore.domain.entity.ScreenTabEntity;
import serp.project.pmcore.domain.entity.ScreenTabFieldEntity;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.port.store.IFieldConfigItemPort;
import serp.project.pmcore.domain.port.store.IFieldConfigPort;
import serp.project.pmcore.domain.port.store.IFieldConfigSchemeItemPort;
import serp.project.pmcore.domain.port.store.IFieldConfigSchemePort;
import serp.project.pmcore.domain.port.store.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.domain.port.store.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.port.store.IScreenPort;
import serp.project.pmcore.domain.port.store.IScreenSchemeItemPort;
import serp.project.pmcore.domain.port.store.IScreenSchemePort;
import serp.project.pmcore.domain.port.store.IScreenTabFieldPort;
import serp.project.pmcore.domain.port.store.IScreenTabPort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemFieldPolicyResolverTest {

    private static final Long TENANT_ID = 1L;
    private static final Long ISSUE_TYPE_ID = 101L;
    private static final Long FIELD_CONFIG_SCHEME_ID = 201L;
    private static final Long FIELD_CONFIG_ID = 202L;
    private static final Long ISSUE_TYPE_SCREEN_SCHEME_ID = 301L;
    private static final Long SCREEN_SCHEME_ID = 302L;
    private static final Long SCREEN_ID = 303L;
    private static final Long SCREEN_TAB_ID = 304L;

    @Mock
    private IFieldConfigSchemePort fieldConfigSchemePort;
    @Mock
    private IFieldConfigSchemeItemPort fieldConfigSchemeItemPort;
    @Mock
    private IFieldConfigPort fieldConfigPort;
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
    private IScreenPort screenPort;
    @Mock
    private IScreenTabPort screenTabPort;
    @Mock
    private IScreenTabFieldPort screenTabFieldPort;

    private WorkItemFieldPolicyResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new WorkItemFieldPolicyResolver(
                fieldConfigSchemePort,
                fieldConfigSchemeItemPort,
                fieldConfigPort,
                fieldConfigItemPort,
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
    void resolveCreateFieldRulesShouldMergeFieldConfigAndCreateScreenPolicies() {
        ProjectEntity project = ProjectEntity.builder()
                .id(10L)
                .fieldConfigSchemeId(FIELD_CONFIG_SCHEME_ID)
                .issueTypeScreenSchemeId(ISSUE_TYPE_SCREEN_SCHEME_ID)
                .build();

        when(fieldConfigSchemePort.getFieldConfigSchemeById(FIELD_CONFIG_SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(FieldConfigSchemeEntity.builder()
                        .id(FIELD_CONFIG_SCHEME_ID)
                        .defaultFieldConfigId(FIELD_CONFIG_ID)
                        .build()));
        when(fieldConfigSchemeItemPort.getFieldConfigSchemeItemsBySchemeId(FIELD_CONFIG_SCHEME_ID, TENANT_ID))
                .thenReturn(List.of());
        when(fieldConfigPort.getFieldConfigById(FIELD_CONFIG_ID, TENANT_ID))
                .thenReturn(Optional.of(FieldConfigEntity.builder().id(FIELD_CONFIG_ID).build()));
        when(fieldConfigItemPort.getFieldConfigItemsByFieldConfigId(FIELD_CONFIG_ID, TENANT_ID))
                .thenReturn(List.of(
                        FieldConfigItemEntity.builder()
                                .fieldRefType("SYSTEM")
                                .fieldRef("dueDate")
                                .isHidden(true)
                                .build(),
                        FieldConfigItemEntity.builder()
                                .fieldRefType("CUSTOM")
                                .fieldRef("customfield_10001")
                                .isRequired(true)
                                .isHidden(false)
                                .build()
                ));

        when(issueTypeScreenSchemePort.getIssueTypeScreenSchemeById(ISSUE_TYPE_SCREEN_SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(IssueTypeScreenSchemeEntity.builder()
                        .id(ISSUE_TYPE_SCREEN_SCHEME_ID)
                        .defaultScreenSchemeId(SCREEN_SCHEME_ID)
                        .build()));
        when(issueTypeScreenSchemeItemPort.getIssueTypeScreenSchemeItemsBySchemeId(ISSUE_TYPE_SCREEN_SCHEME_ID, TENANT_ID))
                .thenReturn(List.of());
        when(screenSchemePort.getScreenSchemeById(SCREEN_SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(ScreenSchemeEntity.builder()
                        .id(SCREEN_SCHEME_ID)
                        .defaultScreenId(SCREEN_ID)
                        .build()));
        when(screenSchemeItemPort.getScreenSchemeItemsByScreenSchemeId(SCREEN_SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(ScreenSchemeItemEntity.builder()
                        .operationKey("CREATE")
                        .screenId(SCREEN_ID)
                        .build()));
        when(screenPort.getScreenById(SCREEN_ID, TENANT_ID))
                .thenReturn(Optional.of(ScreenEntity.builder().id(SCREEN_ID).build()));
        when(screenTabPort.getScreenTabsByScreenId(SCREEN_ID, TENANT_ID))
                .thenReturn(List.of(ScreenTabEntity.builder().id(SCREEN_TAB_ID).screenId(SCREEN_ID).build()));
        when(screenTabFieldPort.getScreenTabFieldsByScreenTabId(SCREEN_TAB_ID, TENANT_ID))
                .thenReturn(List.of(
                        ScreenTabFieldEntity.builder().fieldRefType("SYSTEM").fieldRef("summary").build(),
                        ScreenTabFieldEntity.builder().fieldRefType("CUSTOM").fieldRef("customfield_10001").build()
                ));

        CreateFieldRules rules = resolver.resolveCreateFieldRules(project, ISSUE_TYPE_ID, TENANT_ID);

        FieldPolicy summaryPolicy = rules.getSystemFieldPolicy(WorkItemFieldConstants.SUMMARY);
        FieldPolicy dueDatePolicy = rules.getSystemFieldPolicy(WorkItemFieldConstants.DUE_DATE);
        FieldPolicy customPolicy = rules.getCustomFieldPolicy("customfield_10001");
        FieldPolicy issueTypePolicy = rules.getSystemFieldPolicy(WorkItemFieldConstants.ISSUE_TYPE_ID);

        assertNotNull(summaryPolicy);
        assertTrue(summaryPolicy.required());
        assertTrue(summaryPolicy.onCreateScreen());

        assertNotNull(issueTypePolicy);
        assertTrue(issueTypePolicy.onCreateScreen());

        assertNotNull(dueDatePolicy);
        assertEquals(WorkItemFieldConstants.DUE_DATE, dueDatePolicy.fieldRef());
        assertTrue(dueDatePolicy.hidden());

        assertNotNull(customPolicy);
        assertTrue(customPolicy.required());
        assertTrue(customPolicy.onCreateScreen());
    }
}
