/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.workitem.create;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.dto.request.CreateWorkItemRequest;
import serp.project.pmcore.domain.dto.workitem.create.ResolvedWorkItemCreateConfiguration;
import serp.project.pmcore.domain.entity.workflow.WorkflowStepEntity;
import serp.project.pmcore.domain.entity.workitem.IssueTypeEntity;
import serp.project.pmcore.domain.entity.workitem.WorkItemEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WorkItemDraftFactoryTest {

    private WorkItemDraftFactory factory;

    @BeforeEach
    void setUp() {
        factory = new WorkItemDraftFactory();
    }

    @Test
    void buildDraftShouldPopulateDerivedFieldsAndDefaults() {
        ResolvedWorkItemCreateConfiguration configuration = new ResolvedWorkItemCreateConfiguration(
                IssueTypeEntity.builder().id(101L).hierarchyLevel(1).build(),
                WorkflowStepEntity.builder().id(201L).statusId(301L).build(),
                401L
        );

        WorkItemEntity draft = factory.buildDraft(
                10L,
                CreateWorkItemRequest.builder()
                        .issueTypeId(101L)
                        .summary("Create task")
                        .description("Task details")
                        .parentId(501L)
                        .dueDate(1_700_000_000_000L)
                        .timeOriginalEstimate(3600L)
                        .build(),
                configuration,
                1L,
                "SERP-1",
                "0|hzzzzz:",
                601L,
                701L,
                801L
        );

        assertEquals(10L, draft.getProjectId());
        assertEquals(101L, draft.getIssueTypeId());
        assertEquals(1L, draft.getIssueNo());
        assertEquals("SERP-1", draft.getKey());
        assertEquals(201L, draft.getWorkflowStepId());
        assertEquals(301L, draft.getStatusId());
        assertEquals(401L, draft.getPriorityId());
        assertEquals(601L, draft.getAssigneeId());
        assertEquals(701L, draft.getReporterId());
        assertEquals(801L, draft.getSecurityLevelId());
        assertEquals(3600L, draft.getTimeOriginalEstimate());
        assertEquals(3600L, draft.getTimeRemainingEstimate());
        assertEquals(0L, draft.getTimeSpent());
        assertNull(draft.getResolutionId());
    }
}
