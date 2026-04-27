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
import serp.project.pmcore.application.workitem.command.create.internal.CreateWorkItemData;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;

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
        WorkItemFieldRules rules = new WorkItemFieldRules(
                Map.of(
                        WorkItemFieldConstants.PRIORITY_ID,
                        new WorkItemFieldPolicy(
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
                List.of()
        ));
    }

    @Test
    void validateShouldRejectMissingRequiredSystemField() {
        WorkItemFieldRules rules = new WorkItemFieldRules(
                Map.of(
                        WorkItemFieldConstants.PRIORITY_ID,
                        new WorkItemFieldPolicy(
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
                        List.of()
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
                        WorkItemFieldRules.empty(),
                        List.of("customfield_10001")
                )
        );

        assertEquals(DomainErrorCode.REQUIRED_FIELDS_MISSING, exception.getErrorCode());
    }
}
