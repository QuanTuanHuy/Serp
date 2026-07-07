package serp.project.school_bus_service.dto.response;

public record SchoolRegistrySummaryResponse(
        long totalSchools,
        long totalPickupPoints,
        long linkedPickupPoints,
        long missingCoordinates) {
}
