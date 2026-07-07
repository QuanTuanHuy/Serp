package serp.project.school_bus_service.dto.response;

public record FleetSummaryResponse(
        long totalBuses,
        long availableBuses,
        long totalDrivers,
        long availableDrivers,
        long unavailableDrivers,
        long totalAttendants,
        long availableAttendants,
        long unavailableAttendants,
        long totalDepots,
        long depotsWithCoordinates) {
}
