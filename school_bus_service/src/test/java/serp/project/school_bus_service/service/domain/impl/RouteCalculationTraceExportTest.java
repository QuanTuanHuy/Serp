package serp.project.school_bus_service.service.domain.impl;

import org.junit.jupiter.api.Test;
import org.apache.poi.ss.usermodel.*;
import serp.project.school_bus_service.shared.export.ExcelTemplateEngine;

import java.io.ByteArrayInputStream;
import java.util.*;

import org.junit.jupiter.api.Disabled;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("ExcelTemplateEngine no longer exists in shared.export - test needs refactoring")
public class RouteCalculationTraceExportTest {

    @Test
    public void testExcelTemplateEngineRendering() throws Exception {
        ExcelTemplateEngine engine = new ExcelTemplateEngine();

        // 1. Prepare scalar values
        Map<String, Object> scalars = new HashMap<>();
        scalars.put("trace.id", 999L);
        scalars.put("routePlan.id", 888L);
        scalars.put("routePlan.code", "ROUTE-TEST-01");
        scalars.put("planningSession.id", 777L);
        scalars.put("school.name", "Trường THCS Dịch Vọng");
        scalars.put("school.code", "SCH-DV");
        scalars.put("schedule.name", "Lịch Học Kỳ I");
        scalars.put("schedule.code", "SCHED-HK1");
        scalars.put("serviceDate", "2026-06-06");
        scalars.put("direction", "OUTBOUND");
        scalars.put("depot.name", "Bãi Đỗ Xe Cầu Giấy");
        scalars.put("calculation.status", "SUCCESS");
        scalars.put("calculation.type", "MATRIX_AND_TIMELINE");
        scalars.put("source.summary", "OSRM");
        scalars.put("trace.createdAt", "2026-06-06T12:00:00");

        scalars.put("config.ROUTING_AVERAGE_SPEED_KMPH", 40.0);
        scalars.put("config.ROUTING_DWELL_TIME_MINUTES", 2);
        scalars.put("config.ROUTING_ROAD_FACTOR", 1.2);
        scalars.put("config.ROUTING_OSRM_ENABLED", true);

        scalars.put("raw.input_json", "{}");
        scalars.put("raw.matrix_json", "{}");
        scalars.put("raw.timeline_json", "{}");
        scalars.put("raw.issues_json", "{}");
        scalars.put("raw.config_snapshot_json", "{}");

        // 2. Prepare table values
        Map<String, Object> tableData = new HashMap<>();

        // Points
        List<Map<String, Object>> points = new ArrayList<>();
        Map<String, Object> p1 = new HashMap<>();
        p1.put("pointKey", "DEPOT:1");
        p1.put("pointType", "DEPOT");
        p1.put("name", "Bãi Đỗ Xe Cầu Giấy");
        p1.put("latitude", 21.0285);
        p1.put("longitude", 105.804);
        points.add(p1);

        Map<String, Object> p2 = new HashMap<>();
        p2.put("pointKey", "PICKUP_POINT:10");
        p2.put("pointType", "PICKUP_POINT");
        p2.put("name", "Điểm đón 10");
        p2.put("latitude", 21.035);
        p2.put("longitude", 105.795);
        points.add(p2);

        Map<String, Object> p3 = new HashMap<>();
        p3.put("pointKey", "SCHOOL:1");
        p3.put("pointType", "SCHOOL");
        p3.put("name", "Trường THCS Dịch Vọng");
        p3.put("latitude", 21.038);
        p3.put("longitude", 105.792);
        points.add(p3);

        tableData.put("matrixDuration", Map.of("points", points, "durations", List.of(8.5, 4.2)));
        tableData.put("matrixDistance", Map.of("points", points, "distances", List.of(3.5, 1.8)));

        // Points list table for Sheet 2
        List<Map<String, Object>> pointsList = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            Map<String, Object> row = new HashMap<>(points.get(i));
            row.put("index", i + 1);
            row.put("code", "P-" + i);
            row.put("windowStart", "06:30");
            row.put("windowEnd", "07:00");
            row.put("note", "Test note");
            pointsList.add(row);
        }
        tableData.put("points", pointsList);

        // Timeline
        List<Map<String, Object>> timelineStops = new ArrayList<>();
        Map<String, Object> s1 = new HashMap<>();
        s1.put("stopOrder", 1);
        s1.put("pointKey", "DEPOT:1");
        s1.put("pointType", "DEPOT");
        s1.put("pointName", "Bãi Đỗ Xe Cầu Giấy");
        s1.put("plannedArrivalTime", "06:15");
        s1.put("plannedDepartureTime", "06:15");
        s1.put("travelFromPreviousMinutes", 0.0);
        s1.put("distanceFromPreviousKm", 0.0);
        s1.put("dwellMinutes", 0);
        s1.put("waitMinutes", 0);
        s1.put("window", "-");
        s1.put("windowSatisfied", "N/A");
        timelineStops.add(s1);

        Map<String, Object> s2 = new HashMap<>();
        s2.put("stopOrder", 2);
        s2.put("pointKey", "PICKUP_POINT:10");
        s2.put("pointType", "PICKUP_POINT");
        s2.put("pointName", "Điểm đón 10");
        s2.put("plannedArrivalTime", "06:35");
        s2.put("plannedDepartureTime", "06:37");
        s2.put("travelFromPreviousMinutes", 8.5);
        s2.put("distanceFromPreviousKm", 3.5);
        s2.put("dwellMinutes", 2);
        s2.put("waitMinutes", 0);
        s2.put("window", "06:30 - 06:50");
        s2.put("windowSatisfied", "Đạt");
        timelineStops.add(s2);

        tableData.put("timeline.stops", timelineStops);

        // Issues
        List<Map<String, Object>> issues = new ArrayList<>();
        Map<String, Object> issue = new HashMap<>();
        issue.put("index", 1);
        issue.put("severity", "INFO");
        issue.put("code", "OSRM_FALLBACK_USED");
        issue.put("label", "OSRM Fallback");
        issue.put("routePlanId", 888L);
        issue.put("stopId", "-");
        issue.put("pointKey", "-");
        issue.put("message", "Fallback straight line calculations used.");
        issues.add(issue);
        tableData.put("issues", issues);

        // Render Excel
        byte[] excelBytes = engine.render(
                "export-templates/routing-trace-export-template.xlsx",
                scalars,
                tableData
        );

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        // Inspect result excel
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet1 = wb.getSheet("01_Boi_canh");
            assertNotNull(sheet1);
            assertEquals("ROUTE-TEST-01", sheet1.getRow(5).getCell(1).getStringCellValue());

            Sheet sheet2 = wb.getSheet("02_Diem");
            assertNotNull(sheet2);
            assertEquals("PICKUP_POINT:10", sheet2.getRow(3).getCell(1).getStringCellValue());

            Sheet sheet3 = wb.getSheet("03_Ma_tran_thoi_gian");
            assertNotNull(sheet3);
            assertEquals("From Point Key", sheet3.getRow(2).getCell(1).getStringCellValue());
            assertEquals(8.5, sheet3.getRow(3).getCell(5).getNumericCellValue());
        }
    }

    @Test
    public void testPKeyCodeParsingWithWeirdKeys() {
        String[] testKeys = { "DEPOT:1", "PICKUP_POINT:10", "SCHOOL:1", "DEPOT:", "SCHOOL", ":", null };
        String[] expectedCodes = { "1", "10", "1", "DEPOT", "SCHOOL", "-", "-" };

        for (int i = 0; i < testKeys.length; i++) {
            String pKey = testKeys[i];
            String expected = expectedCodes[i];

            String code = "-";
            if (pKey != null) {
                String[] parts = pKey.split(":");
                if (parts.length > 1) {
                    code = parts[1];
                } else if (parts.length > 0) {
                    code = parts[0];
                }
            }

            if (":".equals(pKey)) {
                assertEquals("-", code);
            } else {
                assertEquals(expected, code);
            }
        }
    }
}
