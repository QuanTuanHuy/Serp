/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.screen.service.IScreenService;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;
import serp.project.pmcore.domain.workitem.service.IWorkItemFieldResolver;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateWorkItemFieldRulesResolverTest {

    private static final Long TENANT_ID = 1L;
    private static final Long ISSUE_TYPE_ID = 101L;
    private static final Long SCREEN_ID = 303L;
    private static final Long FIELD_CONFIG_SCHEME_ID = 401L;

    @Mock
    private IScreenService screenService;
    @Mock
    private IWorkItemFieldResolver workItemFieldResolver;

    private CreateWorkItemFieldRulesResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CreateWorkItemFieldRulesResolver(
                screenService,
                workItemFieldResolver
        );
    }

    @Test
    void resolveCreateFieldRulesShouldDelegateAndApplyCreateOverrides() {
        ProjectEntity project = ProjectEntity.builder()
                .id(10L)
                .fieldConfigSchemeId(FIELD_CONFIG_SCHEME_ID)
                .build();

        when(screenService.resolveScreenIdForOperation(project, ISSUE_TYPE_ID, WorkItemFieldConstants.CREATE_OPERATION_KEY, TENANT_ID))
                .thenReturn(SCREEN_ID);
        when(workItemFieldResolver.resolveFieldRules(project, ISSUE_TYPE_ID, SCREEN_ID, TENANT_ID))
                .thenReturn(new WorkItemFieldRules(
                        Map.of(
                                WorkItemFieldConstants.DUE_DATE,
                                new WorkItemFieldPolicy(
                                        WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM,
                                        WorkItemFieldConstants.DUE_DATE,
                                        false,
                                        true,
                                        false
                                )
                        ),
                        Map.of(
                                "customfield_10001",
                                new WorkItemFieldPolicy(
                                        WorkItemFieldConstants.FIELD_REF_TYPE_CUSTOM,
                                        "customfield_10001",
                                        true,
                                        false,
                                        true
                                )
                        )
                ));

        WorkItemFieldRules rules = resolver.resolveCreateFieldRules(project, ISSUE_TYPE_ID, TENANT_ID);

        WorkItemFieldPolicy summaryPolicy = rules.getSystemFieldPolicy(WorkItemFieldConstants.SUMMARY);
        WorkItemFieldPolicy dueDatePolicy = rules.getSystemFieldPolicy(WorkItemFieldConstants.DUE_DATE);
        WorkItemFieldPolicy customPolicy = rules.getCustomFieldPolicy("customfield_10001");
        WorkItemFieldPolicy issueTypePolicy = rules.getSystemFieldPolicy(WorkItemFieldConstants.ISSUE_TYPE_ID);

        assertNotNull(summaryPolicy);
        assertTrue(summaryPolicy.required());
        assertTrue(summaryPolicy.onScreen());
        assertTrue(summaryPolicy.isClientWritable());

        assertNotNull(issueTypePolicy);
        assertEquals(WorkItemFieldConstants.ISSUE_TYPE_ID, issueTypePolicy.fieldRef());
        assertTrue(issueTypePolicy.onScreen());
        assertTrue(issueTypePolicy.isClientWritable());

        assertNotNull(dueDatePolicy);
        assertEquals(WorkItemFieldConstants.DUE_DATE, dueDatePolicy.fieldRef());
        assertTrue(dueDatePolicy.hidden());

        assertNotNull(customPolicy);
        assertTrue(customPolicy.required());
        assertTrue(customPolicy.onScreen());
    }
}
