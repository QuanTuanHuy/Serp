/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.customfield.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.customfield.dto.ResolvedCustomFields;
import serp.project.pmcore.domain.customfield.dto.WorkItemCustomFieldMutationPlan;
import serp.project.pmcore.domain.customfield.entity.CustomFieldEntity;
import serp.project.pmcore.domain.customfield.service.IWorkItemCustomFieldResolver;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.domain.workitem.port.IWorkItemCustomFieldValuePort;
import serp.project.pmcore.domain.customfield.port.ICustomFieldPort;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemCustomFieldMutationServiceTest {

    private static final Long WORK_ITEM_ID = 20L;
    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 99L;
    private static final Long CUSTOM_FIELD_ID = 500L;

    @Mock
    private ICustomFieldPort customFieldPort;
    @Mock
    private IWorkItemCustomFieldValuePort workItemCustomFieldValuePort;
    @Mock
    private IWorkItemCustomFieldResolver workItemCustomFieldResolver;

    private WorkItemCustomFieldMutationService service;

    @BeforeEach
    void setUp() {
        service = new WorkItemCustomFieldMutationService(
                customFieldPort,
                workItemCustomFieldValuePort,
                workItemCustomFieldResolver
        );
    }

    @Test
    void planUpdateShouldMarkRequiredFieldMissingWhenRequestedNullClearsExistingValue() {
        when(customFieldPort.getCustomFieldsByFieldKeys(List.of("cf_text")))
                .thenReturn(List.of(CustomFieldEntity.builder()
                        .id(CUSTOM_FIELD_ID)
                        .fieldKey("cf_text")
                        .typeKey("text")
                        .build()));
        when(workItemCustomFieldValuePort.getActiveValuesByWorkItemId(WORK_ITEM_ID, TENANT_ID))
                .thenReturn(List.of(WorkItemCustomFieldValueEntity.builder()
                        .customFieldId(CUSTOM_FIELD_ID)
                        .textValue("old")
                        .build()));

        Map<String, Object> requestedFields = new LinkedHashMap<>();
        requestedFields.put("cf_text", null);

        WorkItemCustomFieldMutationPlan plan = service.planUpdate(
                "task",
                WORK_ITEM_ID,
                TENANT_ID,
                requestedFields,
                Map.of("cf_text", true)
        );

        assertEquals(List.of("cf_text"), plan.missingRequiredFields());
        assertEquals(List.of(CUSTOM_FIELD_ID), plan.customFieldIdsToReplace());
        assertEquals(List.of("cf_text"), plan.changedFieldKeys());
    }

    @Test
    void planUpdateShouldResolveProvidedValuesAndKeepRequestedChangeKeys() {
        when(customFieldPort.getCustomFieldsByFieldKeys(List.of("cf_text")))
                .thenReturn(List.of(CustomFieldEntity.builder()
                        .id(CUSTOM_FIELD_ID)
                        .fieldKey("cf_text")
                        .typeKey("text")
                        .build()));
        when(workItemCustomFieldValuePort.getActiveValuesByWorkItemId(WORK_ITEM_ID, TENANT_ID))
                .thenReturn(List.of());
        when(workItemCustomFieldResolver.resolveCustomFields(eq("task"), eq(Map.of("cf_text", "new value")), eq(Map.of("cf_text", false))))
                .thenReturn(new ResolvedCustomFields(List.of(WorkItemCustomFieldValueEntity.builder()
                        .customFieldId(CUSTOM_FIELD_ID)
                        .textValue("new value")
                        .build()), List.of()));

        WorkItemCustomFieldMutationPlan plan = service.planUpdate(
                "task",
                WORK_ITEM_ID,
                TENANT_ID,
                Map.of("cf_text", "new value"),
                Map.of("cf_text", false)
        );

        assertEquals(1, plan.resolvedValues().size());
        assertEquals(List.of(CUSTOM_FIELD_ID), plan.customFieldIdsToReplace());
        assertEquals(List.of("cf_text"), plan.changedFieldKeys());
    }

    @Test
    void applyPlanShouldReplaceAndPersistResolvedValues() {
        WorkItemCustomFieldValueEntity resolvedValue = WorkItemCustomFieldValueEntity.builder()
                .customFieldId(CUSTOM_FIELD_ID)
                .textValue("new value")
                .build();

        service.applyPlan(
                WORK_ITEM_ID,
                TENANT_ID,
                USER_ID,
                new WorkItemCustomFieldMutationPlan(
                        List.of(resolvedValue),
                        List.of(),
                        List.of(CUSTOM_FIELD_ID),
                        List.of("cf_text")
                )
        );

        verify(workItemCustomFieldValuePort).softDeleteByWorkItemIdAndCustomFieldIds(eq(WORK_ITEM_ID), eq(List.of(CUSTOM_FIELD_ID)), eq(USER_ID), any());
        ArgumentCaptor<List<WorkItemCustomFieldValueEntity>> valuesCaptor = ArgumentCaptor.forClass(List.class);
        verify(workItemCustomFieldValuePort).saveAll(valuesCaptor.capture());
        assertEquals(WORK_ITEM_ID, valuesCaptor.getValue().getFirst().getWorkItemId());
        assertEquals(TENANT_ID, valuesCaptor.getValue().getFirst().getTenantId());
    }
}
