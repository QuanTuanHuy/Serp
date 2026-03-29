/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.application.workitem.command.create.model.CreateWorkItemData;
import serp.project.pmcore.application.workitem.command.create.model.CreateFieldRules;
import serp.project.pmcore.application.workitem.command.create.model.FieldPolicy;

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
        CreateFieldRules rules = new CreateFieldRules(
                Map.of(
                        WorkItemFieldConstants.DUE_DATE,
                        new FieldPolicy(WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM,
                                WorkItemFieldConstants.DUE_DATE,
                                false,
                                false,
                                true)
                ),
                Map.of(
                        "customfield_10001",
                        new FieldPolicy(WorkItemFieldConstants.FIELD_REF_TYPE_CUSTOM,
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
                        .dueDate(1_700_000_000_000L)
                        .customFields(Map.of("customfield_10001", "value"))
                        .build(),
                rules
        ));
    }

    @Test
    void validateClientSuppliedWritableFieldsShouldRejectHiddenSystemField() {
        CreateFieldRules rules = new CreateFieldRules(
                Map.of(
                        WorkItemFieldConstants.DUE_DATE,
                        new FieldPolicy(WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM,
                                WorkItemFieldConstants.DUE_DATE,
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
                                .dueDate(1_700_000_000_000L)
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
                        CreateFieldRules.empty()
                )
        );

        assertEquals(DomainErrorCode.FIELD_NOT_WRITABLE_ON_CREATE, exception.getErrorCode());
    }
}
