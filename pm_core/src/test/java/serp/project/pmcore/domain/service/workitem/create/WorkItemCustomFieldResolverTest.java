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
import serp.project.pmcore.domain.dto.workitem.create.ResolvedCustomFields;
import serp.project.pmcore.domain.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.entity.CustomFieldContextEntity;
import serp.project.pmcore.domain.entity.CustomFieldEntity;
import serp.project.pmcore.domain.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainValidationException;
import serp.project.pmcore.domain.port.store.ICustomFieldContextDefaultValuePort;
import serp.project.pmcore.domain.port.store.ICustomFieldContextPort;
import serp.project.pmcore.domain.port.store.ICustomFieldOptionPort;
import serp.project.pmcore.domain.port.store.ICustomFieldPort;
import serp.project.pmcore.domain.service.workitem.create.handler.DateCustomFieldValueHandler;
import serp.project.pmcore.domain.service.workitem.create.handler.DateTimeCustomFieldValueHandler;
import serp.project.pmcore.domain.service.workitem.create.handler.GroupCustomFieldValueHandler;
import serp.project.pmcore.domain.service.workitem.create.handler.JsonCustomFieldValueHandler;
import serp.project.pmcore.domain.service.workitem.create.handler.MultiSelectCustomFieldValueHandler;
import serp.project.pmcore.domain.service.workitem.create.handler.NumberCustomFieldValueHandler;
import serp.project.pmcore.domain.service.workitem.create.handler.SelectCustomFieldValueHandler;
import serp.project.pmcore.domain.service.workitem.create.handler.TextCustomFieldValueHandler;
import serp.project.pmcore.domain.service.workitem.create.handler.UserCustomFieldValueHandler;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemCustomFieldResolverTest {

    private static final String ISSUE_TYPE_KEY = "task";
    private static final Long CUSTOM_FIELD_ID = 1000L;
    private static final Long CONTEXT_ID = 1001L;

    @Mock
    private ICustomFieldPort customFieldPort;
    @Mock
    private ICustomFieldContextPort customFieldContextPort;
    @Mock
    private ICustomFieldOptionPort customFieldOptionPort;
    @Mock
    private ICustomFieldContextDefaultValuePort customFieldContextDefaultValuePort;
    @Mock
    private JsonUtils jsonUtils;

    private WorkItemCustomFieldResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new WorkItemCustomFieldResolver(
                customFieldPort,
                customFieldContextPort,
                customFieldOptionPort,
                customFieldContextDefaultValuePort,
                List.of(
                        new TextCustomFieldValueHandler(),
                        new NumberCustomFieldValueHandler(),
                        new DateCustomFieldValueHandler(),
                        new DateTimeCustomFieldValueHandler(),
                        new UserCustomFieldValueHandler(),
                        new GroupCustomFieldValueHandler(),
                        new SelectCustomFieldValueHandler(),
                        new MultiSelectCustomFieldValueHandler(),
                        new JsonCustomFieldValueHandler(jsonUtils)
                )
        );
    }

    @Test
    void resolveCustomFieldsShouldApplyDefaultTextValue() {
        stubCustomFieldDefinition("customfield_10001", "text");
        when(customFieldContextDefaultValuePort.getCustomFieldContextDefaultValuesByContextId(CONTEXT_ID))
                .thenReturn(List.of(CustomFieldContextDefaultValueEntity.builder()
                        .contextId(CONTEXT_ID)
                        .textValue("Default environment")
                        .build()));
        when(customFieldOptionPort.getCustomFieldOptionsByContextId(CONTEXT_ID)).thenReturn(List.of());

        ResolvedCustomFields resolvedCustomFields = resolver.resolveCustomFields(
                ISSUE_TYPE_KEY,
                Map.of(),
                customFieldRules(false)
        );

        assertEquals(1, resolvedCustomFields.values().size());
        assertEquals("TEXT", resolvedCustomFields.values().getFirst().getValueType());
        assertEquals("Default environment", resolvedCustomFields.values().getFirst().getTextValue());
    }

    @Test
    void resolveCustomFieldsShouldResolveProvidedMultiselectValuesInOrder() {
        stubCustomFieldDefinition("customfield_10001", "multiselect");
        when(customFieldContextDefaultValuePort.getCustomFieldContextDefaultValuesByContextId(CONTEXT_ID))
                .thenReturn(List.of());
        when(customFieldOptionPort.getCustomFieldOptionsByContextId(CONTEXT_ID))
                .thenReturn(List.of(
                        option(2001L, "backend"),
                        option(2002L, "frontend")
                ));

        ResolvedCustomFields resolvedCustomFields = resolver.resolveCustomFields(
                ISSUE_TYPE_KEY,
                Map.of("customfield_10001", List.of("backend", "frontend")),
                customFieldRules(false)
        );

        assertEquals(2, resolvedCustomFields.values().size());
        assertEquals(2001L, resolvedCustomFields.values().get(0).getOptionValueId());
        assertEquals(0, resolvedCustomFields.values().get(0).getSortOrder());
        assertEquals(2002L, resolvedCustomFields.values().get(1).getOptionValueId());
        assertEquals(1, resolvedCustomFields.values().get(1).getSortOrder());
    }

    @Test
    void resolveCustomFieldsShouldRejectAmbiguousContext() {
        when(customFieldPort.getCustomFieldsByFieldKeys(List.of("customfield_10001")))
                .thenReturn(List.of(customField("customfield_10001", "text")));
        when(customFieldContextPort.getApplicableCustomFieldContexts(CUSTOM_FIELD_ID, ISSUE_TYPE_KEY))
                .thenReturn(List.of(
                        issueTypeContext(CONTEXT_ID, ISSUE_TYPE_KEY),
                        issueTypeContext(CONTEXT_ID + 1, ISSUE_TYPE_KEY)
                ));

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> resolver.resolveCustomFields(
                        ISSUE_TYPE_KEY,
                        Map.of("customfield_10001", "value"),
                        customFieldRules(false)
                )
        );

        assertEquals(DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE, exception.getErrorCode());
    }

    @Test
    void resolveCustomFieldsShouldRejectInvalidSelectOption() {
        stubCustomFieldDefinition("customfield_10001", "select");
        when(customFieldContextDefaultValuePort.getCustomFieldContextDefaultValuesByContextId(CONTEXT_ID))
                .thenReturn(List.of());
        when(customFieldOptionPort.getCustomFieldOptionsByContextId(CONTEXT_ID))
                .thenReturn(List.of(option(2001L, "backend")));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> resolver.resolveCustomFields(
                        ISSUE_TYPE_KEY,
                        Map.of("customfield_10001", "unknown"),
                        customFieldRules(false)
                )
        );

        assertEquals(DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID, exception.getErrorCode());
    }

    @Test
    void resolveCustomFieldsShouldPreferExactIssueTypeContextOverGlobalFallback() {
        when(customFieldPort.getCustomFieldsByFieldKeys(List.of("customfield_10001")))
                .thenReturn(List.of(customField("customfield_10001", "text")));
        when(customFieldContextPort.getApplicableCustomFieldContexts(CUSTOM_FIELD_ID, ISSUE_TYPE_KEY))
                .thenReturn(List.of(
                        issueTypeContext(CONTEXT_ID, ISSUE_TYPE_KEY),
                        globalContext(CONTEXT_ID + 1)
                ));
        when(customFieldContextDefaultValuePort.getCustomFieldContextDefaultValuesByContextId(CONTEXT_ID))
                .thenReturn(List.of(CustomFieldContextDefaultValueEntity.builder()
                        .contextId(CONTEXT_ID)
                        .textValue("Issue type default")
                        .build()));
        when(customFieldOptionPort.getCustomFieldOptionsByContextId(CONTEXT_ID)).thenReturn(List.of());

        ResolvedCustomFields resolvedCustomFields = resolver.resolveCustomFields(
                ISSUE_TYPE_KEY,
                Map.of(),
                customFieldRules(false)
        );

        assertEquals(1, resolvedCustomFields.values().size());
        assertEquals("Issue type default", resolvedCustomFields.values().getFirst().getTextValue());
    }

    private void stubCustomFieldDefinition(String fieldKey, String typeKey) {
        when(customFieldPort.getCustomFieldsByFieldKeys(List.of(fieldKey)))
                .thenReturn(List.of(customField(fieldKey, typeKey)));
        when(customFieldContextPort.getApplicableCustomFieldContexts(CUSTOM_FIELD_ID, ISSUE_TYPE_KEY))
                .thenReturn(List.of(globalContext(CONTEXT_ID)));
        when(customFieldContextDefaultValuePort.getCustomFieldContextDefaultValuesByContextId(CONTEXT_ID))
                .thenReturn(List.of());
        when(customFieldOptionPort.getCustomFieldOptionsByContextId(CONTEXT_ID))
                .thenReturn(List.of());
    }

    private CreateFieldRules customFieldRules(boolean required) {
        return new CreateFieldRules(
                Map.of(),
                Map.of(
                        "customfield_10001",
                        new FieldPolicy(WorkItemFieldConstants.FIELD_REF_TYPE_CUSTOM, "customfield_10001", required, false, true)
                )
        );
    }

    private CustomFieldEntity customField(String fieldKey, String typeKey) {
        return CustomFieldEntity.builder()
                .id(CUSTOM_FIELD_ID)
                .fieldKey(fieldKey)
                .typeKey(typeKey)
                .build();
    }

    private CustomFieldContextEntity globalContext(Long contextId) {
        return CustomFieldContextEntity.builder()
                .id(contextId)
                .customFieldId(CUSTOM_FIELD_ID)
                .build();
    }

    private CustomFieldContextEntity issueTypeContext(Long contextId, String issueTypeKey) {
        return CustomFieldContextEntity.builder()
                .id(contextId)
                .customFieldId(CUSTOM_FIELD_ID)
                .issueTypeKey(issueTypeKey)
                .build();
    }

    private CustomFieldOptionEntity option(Long optionId, String optionKey) {
        return CustomFieldOptionEntity.builder()
                .id(optionId)
                .customFieldContextId(CONTEXT_ID)
                .optionKey(optionKey)
                .value(optionKey)
                .isDisabled(false)
                .build();
    }
}
