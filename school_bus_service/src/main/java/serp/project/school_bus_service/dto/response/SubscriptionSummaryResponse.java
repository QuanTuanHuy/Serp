package serp.project.school_bus_service.dto.response;

public record SubscriptionSummaryResponse(
        long totalSubscriptions,
        long activeSubscriptions,
        long inactiveSubscriptions) {
}
