/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.workitem.dto.WorkItemSearchCriteria;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkItemQueryBuilderTest {

    private WorkItemQueryBuilder queryBuilder;

    @BeforeEach
    void setUp() {
        queryBuilder = new WorkItemQueryBuilder(new BaseQueryBuilder());
    }

    @Test
    void buildShouldFilterWorkItemsWithoutActivePlan() {
        WorkItemSearchCriteria criteria = WorkItemSearchCriteria.builder()
                .projectId(10L)
                .hasActivePlan(false)
                .build();

        QueryResult result = queryBuilder.build(1L, criteria);
        String dataSql = normalizeSql(result.dataSql());
        String countSql = normalizeSql(result.countSql());

        assertTrue(dataSql.contains("NOT EXISTS (SELECT 1 FROM work_item_plans plan"));
        assertTrue(dataSql.contains("plan.tenant_id = w.tenant_id"));
        assertTrue(dataSql.contains("plan.project_id = w.project_id"));
        assertTrue(dataSql.contains("plan.work_item_id = w.id"));
        assertTrue(dataSql.contains("plan.deleted_at IS NULL"));
        assertTrue(countSql.contains("NOT EXISTS (SELECT 1 FROM work_item_plans plan"));
    }

    @Test
    void buildShouldFilterWorkItemsWithActivePlan() {
        WorkItemSearchCriteria criteria = WorkItemSearchCriteria.builder()
                .projectId(10L)
                .hasActivePlan(true)
                .build();

        QueryResult result = queryBuilder.build(1L, criteria);
        String dataSql = normalizeSql(result.dataSql());

        assertTrue(dataSql.contains("EXISTS (SELECT 1 FROM work_item_plans plan"));
        assertFalse(dataSql.contains("NOT EXISTS (SELECT 1 FROM work_item_plans plan"));
    }

    @Test
    void buildShouldPreserveDefaultSearchWhenActivePlanFilterIsNull() {
        WorkItemSearchCriteria criteria = WorkItemSearchCriteria.builder()
                .projectId(10L)
                .build();

        QueryResult result = queryBuilder.build(1L, criteria);
        String dataSql = normalizeSql(result.dataSql());
        String countSql = normalizeSql(result.countSql());

        assertFalse(dataSql.contains("work_item_plans plan"));
        assertFalse(countSql.contains("work_item_plans plan"));
    }

    private String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}
