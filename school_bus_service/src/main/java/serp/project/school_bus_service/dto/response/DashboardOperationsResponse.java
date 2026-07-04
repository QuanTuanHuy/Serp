package serp.project.school_bus_service.dto.response;

import java.util.List;

public record DashboardOperationsResponse(
    DashboardSummaryResponse summary,
    List<ChartItemDto> tripStatusChart,
    List<ChartItemDto> attendanceChart,
    List<ChartItemDto> routeReadinessChart,
    List<ChartItemDto> requestStatusChart,
    List<ChartItemDto> tripsByDate,
    List<ChartItemDto> directionSplit,
    List<RoutePlanResponse> activeRoutes,
    List<TransportRequestResponse> pendingApprovalQueue,
    List<AttendanceResponse> recentAttendanceActivity
) {}
