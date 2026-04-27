package serp.project.school_bus_service.application.dto.response;

public record CapacityUtilizationReportResponse(
        Long tripId,
        String tripCode,
        String routeCode,
        Integer plannedStudents,
        Integer busCapacity,
        double utilizationPercent
) {
}

