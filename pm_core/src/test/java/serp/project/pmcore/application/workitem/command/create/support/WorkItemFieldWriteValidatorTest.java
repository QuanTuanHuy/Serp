/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import serp.project.pmcore.application.workitem.command.create.internal.CreateWorkItemData;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkItemFieldWriteValidatorTest {

    private WorkItemFieldWriteValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WorkItemFieldWriteValidator();
    }

    @Test
    void validateClientSuppliedWritableFieldsShouldAllowWritableSystemAndCustomFields() {
        WorkItemFieldRules rules = new WorkItemFieldRules(
                Map.of(
                        WorkItemFieldConstants.START_DATE,
                        new WorkItemFieldPolicy(WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM,
                                WorkItemFieldConstants.START_DATE,
                                false,
                                false,
                                true),
                        WorkItemFieldConstants.DUE_DATE,
                        new WorkItemFieldPolicy(WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM,
                                WorkItemFieldConstants.DUE_DATE,
                                false,
                                false,
                                true)
                ),
                Map.of(
                        "customfield_10001",
                        new WorkItemFieldPolicy(WorkItemFieldConstants.FIELD_REF_TYPE_CUSTOM,
                                "customfield_10001",
                                false,
                                false,
                                true)
                )
        );

        assertDoesNotThrow(() -> validator.validateClientSuppliedWritableFields(
                CreateWorkItemData.builder()
                        .issueTypeId(1L)
                        .summary("Create task")
                        .startDate(1_699_000_000_000L)
                        .dueDate(1_700_000_000_000L)
                        .customFields(Map.of("customfield_10001", "value"))
                        .build(),
                rules
        ));
    }

    @Test
    void validateClientSuppliedWritableFieldsShouldRejectHiddenSystemField() {
        WorkItemFieldRules rules = new WorkItemFieldRules(
                Map.of(
                        WorkItemFieldConstants.START_DATE,
                        new WorkItemFieldPolicy(WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM,
                                WorkItemFieldConstants.START_DATE,
                                false,
                                true,
                                true)
                ),
                Map.of()
        );

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> validator.validateClientSuppliedWritableFields(
                        CreateWorkItemData.builder()
                                .issueTypeId(1L)
                                .summary("Create task")
                                .startDate(1_699_000_000_000L)
                                .build(),
                        rules
                )
        );

        assertEquals(DomainErrorCode.FIELD_NOT_WRITABLE_ON_CREATE, exception.getErrorCode());
    }

    @Test
    void validateClientSuppliedWritableFieldsShouldRejectMissingCustomFieldPolicy() {
        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> validator.validateClientSuppliedWritableFields(
                        CreateWorkItemData.builder()
                                .issueTypeId(1L)
                                .summary("Create task")
                                .customFields(Map.of("customfield_10001", "value"))
                                .build(),
                        WorkItemFieldRules.empty()
                )
        );

        assertEquals(DomainErrorCode.FIELD_NOT_WRITABLE_ON_CREATE, exception.getErrorCode());
    }
}
