/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryActivityProjection;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemScheduleCalendarCriteria;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemReadAdapterTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private WorkItemReadAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WorkItemReadAdapter(
                null,
                null,
                null,
                null,
                jdbcTemplate,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void listProjectSummaryActivitiesShouldGenerateValidCteQueries() {
        ProjectSummaryCriteria criteria = ProjectSummaryCriteria.builder()
                .projectId(10L)
                .activityPage(0)
                .activitySize(20)
                .build();
        when(jdbcTemplate.query(
                anyString(),
                any(MapSqlParameterSource.class),
                nullable(RowMapper.class)
        )).thenReturn(List.of());
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);

        PageResult<ProjectSummaryActivityProjection> result = adapter.listProjectSummaryActivities(1L, criteria);

        ArgumentCaptor<String> dataSqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> countSqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(
                dataSqlCaptor.capture(),
                any(MapSqlParameterSource.class),
                nullable(RowMapper.class)
        );
        verify(jdbcTemplate).queryForObject(
                countSqlCaptor.capture(),
                any(MapSqlParameterSource.class),
                eq(Long.class)
        );

        String dataSql = normalizeSql(dataSqlCaptor.getValue());
        String countSql = normalizeSql(countSqlCaptor.getValue());

        assertEquals(0L, result.total());
        assertTrue(dataSql.startsWith("WITH filtered_items AS"));
        assertTrue(dataSql.contains(", activity AS ( SELECT"));
        assertTrue(dataSql.contains("SELECT * FROM activity ORDER BY"));
        assertFalse(dataSql.startsWith("SELECT * WITH"));
        assertTrue(countSql.startsWith("WITH filtered_items AS"));
        assertTrue(countSql.contains("SELECT COUNT(*) FROM activity"));
        assertFalse(countSql.startsWith("SELECT COUNT(*) WITH"));
    }

    @Test
    void listScheduleAllocationCalendarItemsShouldFilterByAllocationOverlapAndAssignee() {
        WorkItemScheduleCalendarCriteria criteria = WorkItemScheduleCalendarCriteria.builder()
                .projectId(10L)
                .viewportStart(1000L)
                .viewportEnd(2000L)
                .assigneeIds(List.of(5L))
                .build();
        when(jdbcTemplate.query(
                anyString(),
                any(MapSqlParameterSource.class),
                nullable(RowMapper.class)
        )).thenReturn(List.of());
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);

        adapter.listScheduleAllocationCalendarItems(1L, criteria);

        ArgumentCaptor<String> dataSqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(
                dataSqlCaptor.capture(),
                paramsCaptor.capture(),
                nullable(RowMapper.class)
        );

        String dataSql = normalizeSql(dataSqlCaptor.getValue());
        MapSqlParameterSource params = paramsCaptor.getValue();

        assertTrue(dataSql.contains("FROM work_item_plan_allocations a"));
        assertTrue(dataSql.contains("a.end_time > :scheduleViewportStart"));
        assertTrue(dataSql.contains("a.start_time < :scheduleViewportEnd"));
        assertTrue(dataSql.contains("a.assignee_id IN (:assigneeIds)"));
        assertFalse(dataSql.contains("w.assignee_id IN (:assigneeIds)"));
        assertEquals(new Timestamp(1000L), params.getValue("scheduleViewportStart"));
        assertEquals(new Timestamp(2000L), params.getValue("scheduleViewportEnd"));
    }

    private String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}
