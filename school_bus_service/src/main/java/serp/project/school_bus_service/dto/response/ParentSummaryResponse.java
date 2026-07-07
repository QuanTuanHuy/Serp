package serp.project.school_bus_service.dto.response;

public record ParentSummaryResponse(
        long totalParents,
        long withEmail,
        long withPhone,
        long activeParents) {
}
