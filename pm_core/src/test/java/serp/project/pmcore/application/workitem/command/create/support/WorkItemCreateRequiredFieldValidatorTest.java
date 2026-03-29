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
import serp.project.pmcore.application.workitem.command.create.internal.CreateFieldRules;
import serp.project.pmcore.application.workitem.command.create.internal.CreateWorkItemData;
import serp.project.pmcore.application.workitem.command.create.internal.FieldPolicy;
import serp.project.pmcore.application.workitem.command.create.internal.ResolvedCustomFields;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkItemCreateRequiredFieldValidatorTest {

    private WorkItemCreateRequiredFieldValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WorkItemCreateRequiredFieldValidator();
    }

    @Test
    void validateShouldPassWhenRequiredSystemFieldHasEffectiveValue() {
        CreateFieldRules rules = new CreateFieldRules(
                Map.of(
                        WorkItemFieldConstants.PRIORITY_ID,
                        new FieldPolicy(
                                WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM,
                                WorkItemFieldConstants.PRIORITY_ID,
                                true,
                                false,
                                true
                        )
                ),
                Map.of()
        );

        assertDoesNotThrow(() -> validator.validate(
                CreateWorkItemData.builder()
                        .issueTypeId(1L)
                        .summary("Create task")
                        .build(),
                100L,
                null,
                null,
                rules,
                ResolvedCustomFields.empty()
        ));
    }

    @Test
    void validateShouldRejectMissingRequiredSystemField() {
        CreateFieldRules rules = new CreateFieldRules(
                Map.of(
                        WorkItemFieldConstants.PRIORITY_ID,
                        new FieldPolicy(
                                WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM,
                                WorkItemFieldConstants.PRIORITY_ID,
                                true,
                                false,
                                true
                        )
                ),
                Map.of()
        );

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> validator.validate(
                        CreateWorkItemData.builder()
                                .issueTypeId(1L)
                                .summary("Create task")
                                .build(),
                        null,
                        null,
                        null,
                        rules,
                        ResolvedCustomFields.empty()
                )
        );

        assertEquals(DomainErrorCode.REQUIRED_FIELDS_MISSING, exception.getErrorCode());
    }

    @Test
    void validateShouldRejectMissingRequiredCustomFieldsCollectedByResolver() {
        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> validator.validate(
                        CreateWorkItemData.builder()
                                .issueTypeId(1L)
                                .summary("Create task")
                                .build(),
                        null,
                        null,
                        null,
                        CreateFieldRules.empty(),
                        new ResolvedCustomFields(List.of(), List.of("customfield_10001"))
                )
        );

        assertEquals(DomainErrorCode.REQUIRED_FIELDS_MISSING, exception.getErrorCode());
    }
}
