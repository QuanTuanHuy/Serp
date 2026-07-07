package serp.project.school_bus_service.dto.response;

public record StudentSummaryResponse(
        long totalStudents,
        long linkedSchools,
        long linkedParents,
        long activeStudents) {
}
