package serp.project.school_bus_service.service.impl;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.entity.DepotEntity;
import serp.project.school_bus_service.entity.RouteCalculationTraceEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.repository.RouteCalculationTraceRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.export.ExportCode;
import serp.project.school_bus_service.shared.export.ExportHandler;
import serp.project.school_bus_service.shared.export.ExportRequest;
import serp.project.school_bus_service.shared.export.ExportResult;
import serp.project.school_bus_service.shared.export.ExcelTemplateEngine;

import java.util.*;

/**
 * TODO Phase 5/7:
 * Persist and export planning-context full N x N matrix before route generation.
 * This will be used by greedy route generation and experiment benchmark.
 *
 * Tiếng Việt:
 * TODO Phase 5/7:
 * Lưu và export ma trận N x N theo planning context trước khi tạo route.
 * Ma trận này phục vụ thuật toán greedy và benchmark thực nghiệm.
 */
@Component
public class RoutingTraceExportHandler implements ExportHandler {

    private final RouteCalculationTraceRepository traceRepository;
    private final ExcelTemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    public RoutingTraceExportHandler(
            RouteCalculationTraceRepository traceRepository,
            ExcelTemplateEngine templateEngine,
            ObjectMapper objectMapper) {
        this.traceRepository = traceRepository;
        this.templateEngine = templateEngine;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getExportCode() {
        return ExportCode.ROUTING_TRACE;
    }

    @Override
    @Transactional(readOnly = true)
    public ExportResult export(ExportRequest request) {
        RouteCalculationTraceEntity trace = null;
        if (request.getTraceId() != null) {
            trace = traceRepository.findById(request.getTraceId()).orElse(null);
        } else if (request.getRoutePlanId() != null) {
            trace = traceRepository.findFirstByRoutePlanIdAndCalculationTypeAndIsDeletedFalseOrderByCreatedAtDesc(
                    request.getRoutePlanId(),
                    serp.project.school_bus_service.enums.RouteCalculationType.MATRIX_AND_TIMELINE
            ).orElse(null);
        }

        if (trace == null) {
            throw new AppException(AppErrorCode.Export.TRACE_NOT_FOUND,
                    "No routing calculation trace found. Please compute route first.");
        }

        RoutePlanEntity routePlan = trace.getRoutePlan();
        RoutePlanningSessionEntity session = trace.getPlanningSession();
        SchoolEntity school = routePlan.getSchool();
        SchoolScheduleEntity schedule = session != null ? session.getSchoolSchedule() : routePlan.getSchoolSchedule();

        // Determine Depot
        DepotEntity depot = (routePlan.getRouteDirection() == RouteDirection.OUTBOUND)
                ? routePlan.getStartDepot()
                : routePlan.getEndDepot();
        String depotName = depot != null ? depot.getName() : "-";

        // Parse JSON strings to Maps/Lists
        Map<String, Object> inputJson = parseJsonToMap(trace.getInputJson());
        Map<String, Object> matrixJson = parseJsonToMap(trace.getMatrixJson());
        Map<String, Object> timelineJson = parseJsonToMap(trace.getTimelineJson());
        Map<String, Object> issuesJson = parseJsonToMap(trace.getIssuesJson());
        Map<String, Object> configSnapshotJson = parseJsonToMap(trace.getConfigSnapshotJson());

        // Prepare scalar values
        Map<String, Object> scalars = new HashMap<>();
        scalars.put("trace.id", trace.getId());
        scalars.put("routePlan.id", routePlan.getId());
        scalars.put("routePlan.code", routePlan.getRouteCode());
        scalars.put("planningSession.id", session != null ? session.getId() : "-");
        scalars.put("school.name", school != null ? school.getName() : "-");
        scalars.put("school.code", school != null ? school.getCode() : "-");
        scalars.put("schedule.name", schedule != null ? schedule.getScheduleName() : "-");
        scalars.put("schedule.code", schedule != null ? schedule.getScheduleCode() : "-");
        scalars.put("serviceDate", routePlan.getServiceDate() != null ? routePlan.getServiceDate().toString() : "-");
        scalars.put("direction", routePlan.getRouteDirection() != null ? routePlan.getRouteDirection().name() : "-");
        scalars.put("depot.name", depotName);
        scalars.put("calculation.status", trace.getCalculationStatus().name());
        scalars.put("calculation.type", trace.getCalculationType().name());
        scalars.put("source.summary", trace.getSourceSummary() != null ? trace.getSourceSummary() : "-");
        scalars.put("trace.createdAt", trace.getCreatedAt() != null ? trace.getCreatedAt().toString() : "-");

        // Fill config snapshot values
        if (configSnapshotJson != null) {
            scalars.put("config.ROUTING_AVERAGE_SPEED_KMPH", configSnapshotJson.get("ROUTING_AVERAGE_SPEED_KMPH"));
            scalars.put("config.ROUTING_DWELL_TIME_MINUTES", configSnapshotJson.get("ROUTING_DWELL_TIME_MINUTES"));
            scalars.put("config.ROUTING_ROAD_FACTOR", configSnapshotJson.get("ROUTING_ROAD_FACTOR"));
            scalars.put("config.ROUTING_OSRM_ENABLED", configSnapshotJson.get("ROUTING_OSRM_ENABLED"));
        }

        // Fill prettified JSONs
        scalars.put("raw.input_json", getPrettyJson(trace.getInputJson()));
        scalars.put("raw.matrix_json", getPrettyJson(trace.getMatrixJson()));
        scalars.put("raw.timeline_json", getPrettyJson(trace.getTimelineJson()));
        scalars.put("raw.issues_json", getPrettyJson(trace.getIssuesJson()));
        scalars.put("raw.config_snapshot_json", getPrettyJson(trace.getConfigSnapshotJson()));

        // Prepare table values
        Map<String, Object> tableData = new HashMap<>();

        // 1. matrixDuration and matrixDistance
        tableData.put("matrixDuration", matrixJson);
        tableData.put("matrixDistance", matrixJson);

        // 2. points table (Sheet 02_Diem)
        List<Map<String, Object>> matrixPoints = (List<Map<String, Object>>) (matrixJson != null ? matrixJson.get("points") : null);
        List<Map<String, Object>> pointsTable = new ArrayList<>();
        if (matrixPoints != null) {
            for (int i = 0; i < matrixPoints.size(); i++) {
                Map<String, Object> p = matrixPoints.get(i);
                Map<String, Object> pointRow = new HashMap<>(p);
                pointRow.put("index", i + 1);

                String pKey = (String) p.get("pointKey");
                String pType = (String) p.get("pointType");

                // Read window properties directly from trace if they exist
                // TODO Phase 3.1 should persist window/wait/windowSatisfied in timeline_json if export needs those fields.
                String windowStart = p.containsKey("windowStart") ? String.valueOf(p.get("windowStart")) : "Không có trong trace";
                String windowEnd = p.containsKey("windowEnd") ? String.valueOf(p.get("windowEnd")) : "Không có trong trace";

                String code = "-";
                if (pKey != null) {
                    String[] parts = pKey.split(":");
                    if (parts.length > 1) {
                        code = parts[1];
                    } else if (parts.length > 0) {
                        code = parts[0];
                    }
                }
                pointRow.put("code", code);
                pointRow.put("windowStart", windowStart);
                pointRow.put("windowEnd", windowEnd);
                pointRow.put("note", "-");
                pointsTable.add(pointRow);
            }
        }
        tableData.put("points", pointsTable);

        // 3. timeline stops table (Sheet 05_Timeline)
        List<Map<String, Object>> timelineStops = (List<Map<String, Object>>) (timelineJson != null ? timelineJson.get("stops") : null);
        List<Map<String, Object>> timelineTable = new ArrayList<>();
        if (timelineStops != null) {
            for (Map<String, Object> stop : timelineStops) {
                Map<String, Object> stopRow = new HashMap<>(stop);

                String pKey = (String) stop.get("pointKey");

                // Derive Point Type and Name from points list
                String pType = "-";
                String pName = "-";
                if (matrixPoints != null) {
                    for (Map<String, Object> p : matrixPoints) {
                        if (Objects.equals(p.get("pointKey"), pKey)) {
                            pType = (String) p.get("pointType");
                            pName = (String) p.get("name");
                            break;
                        }
                    }
                }
                stopRow.put("pointType", pType);
                stopRow.put("pointName", pName);

                // Read window, waitMinutes, windowSatisfied directly from timeline stop properties if they exist
                // TODO Phase 3.1 should persist window/wait/windowSatisfied in timeline_json if export needs those fields.
                String windowStr = stop.containsKey("window") ? String.valueOf(stop.get("window")) : "Không có trong trace";
                Object waitMinVal = stop.get("waitMinutes");
                String waitMinutesStr = waitMinVal != null ? String.valueOf(waitMinVal) : "Không có trong trace";
                String windowSatisfied = stop.containsKey("windowSatisfied") ? String.valueOf(stop.get("windowSatisfied")) : "Không có trong trace";

                stopRow.put("window", windowStr);
                stopRow.put("waitMinutes", waitMinutesStr);
                stopRow.put("windowSatisfied", windowSatisfied);

                timelineTable.add(stopRow);
            }
        }
        tableData.put("timeline.stops", timelineTable);

        // 4. issues table (Sheet 06_Van_de)
        List<Map<String, Object>> rawIssues = (List<Map<String, Object>>) (issuesJson != null ? issuesJson.get("issues") : null);
        List<Map<String, Object>> issuesTable = new ArrayList<>();
        if (rawIssues != null) {
            for (int i = 0; i < rawIssues.size(); i++) {
                Map<String, Object> issue = rawIssues.get(i);
                Map<String, Object> issueRow = new HashMap<>(issue);
                issueRow.put("index", i + 1);
                issueRow.put("routePlanId", routePlan.getId());
                issuesTable.add(issueRow);
            }
        }
        tableData.put("issues", issuesTable);

        // Render template Excel
        byte[] content = templateEngine.render(
                "export-templates/routing-trace-export-template.xlsx",
                scalars,
                tableData
        );

        String fileName = String.format("routing-trace-route-%d-%d.xlsx", routePlan.getId(), trace.getId());
        return ExportResult.builder()
                .fileName(fileName)
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .content(content)
                .build();
    }

    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private String getPrettyJson(String json) {
        if (json == null || json.isBlank()) return "-";
        try {
            Object parsed = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);
        } catch (Exception e) {
            return json;
        }
    }
}
